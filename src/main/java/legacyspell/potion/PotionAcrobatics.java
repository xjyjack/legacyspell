package legacyspell.potion;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import legacyspell.LegacySpell;
import legacyspell.network.AeropicFaceMessage;
import legacyspell.spell.AeropicChain;
import legacyspell.util.MotionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PotionAcrobatics extends Potion {

    private static final double CHAIN_PUSH = 2.5;
    private static final double LAUNCH_Y = 0.9;
    private static final double BURST_RADIUS = 4.0;
    private static final double BURST_SHARE = 0.5;

    private static final double AIM_RADIUS = 32.0;

    private static final int IFRAME_TICKS = 10;

    public PotionAcrobatics() {
        super(false, 0x99ccff);
        this.setRegistryName(LegacySpell.MODID, "acrobatics");
        this.setPotionName("potion." + LegacySpell.MODID + ":acrobatics");
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase living, int amplifier) {
        World world = living.world;
        if (world.isRemote) return;

        living.fallDistance = 0.0f;

        AxisAlignedBB swept = sweptBox(living);
        EntityLivingBase target = firstTarget(world, living, swept);

        if (target == null) return;

        boolean connected = target.attackEntityFrom(
                MagicDamage.causeIndirectMagicDamage(living, living, DamageType.SHOCK), (float) amplifier);
        if (!connected) return;

        target.hurtResistantTime = 20;
        target.motionY += 0.2;
        target.velocityChanged = true;
        applyBurst(world, living, target, amplifier);
        world.playSound(null, living.posX, living.posY + 1.0, living.posZ,
                SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, living.getSoundCategory(), 4.0f, 1.2f);

        int hitIndex = AeropicChain.hitIndex(living) + 1;
        AeropicChain.setHitIndex(living, hitIndex);
        AeropicChain.rememberChainTarget(living, nearestHostile(living, null));

        int remaining = AeropicChain.remaining(living);
        if (remaining <= 0) return;

        if (hitIndex >= 2) {
            EntityLivingBase next = AeropicChain.chainTarget(living);
            if (next != null && next.isEntityAlive()
                    && living.getDistanceSq(next) < AIM_RADIUS * AIM_RADIUS) {
                faceEntity(living, next);
            }
        }

        AeropicChain.setRemaining(living, remaining - 1);

        if (remaining > 1) {
            Vec3d forward = living.getLookVec();
            MotionHelper.setMotion(living, forward.x * CHAIN_PUSH, LAUNCH_Y, forward.z * CHAIN_PUSH);
        } else {
            MotionHelper.setMotion(living, 0.0, LAUNCH_Y, 0.0);
        }
    }

    private static AxisAlignedBB sweptBox(EntityLivingBase living) {
        Vec3d from = living.getPositionVector();
        Vec3d to = new Vec3d(from.x + living.motionX * 2.0, from.y + living.motionY * 2.0, from.z + living.motionZ * 2.0);

        AxisAlignedBB box = new AxisAlignedBB(
                Math.min(from.x, to.x), Math.min(from.y, to.y), Math.min(from.z, to.z),
                Math.max(from.x, to.x), Math.max(from.y, to.y), Math.max(from.z, to.z));
        AxisAlignedBB swept = new AxisAlignedBB(
                box.minX - 0.5, box.minY - 0.5, box.minZ - 0.5,
                box.maxX + 0.5, box.maxY + 0.5, box.maxZ + 0.5);

        AxisAlignedBB eb = living.getEntityBoundingBox();
        AxisAlignedBB self = new AxisAlignedBB(
                eb.minX - 0.5, eb.minY - 0.5, eb.minZ - 0.5,
                eb.maxX + 0.5, eb.maxY + 0.5, eb.maxZ + 0.5);
        return swept.union(self);
    }

    private static EntityLivingBase firstTarget(World world, EntityLivingBase caster, AxisAlignedBB box) {
        EntityLivingBase fallback = null;
        for (Entity e : world.getEntitiesWithinAABBExcludingEntity(caster, box)) {
            if (!(e instanceof EntityLivingBase) || !e.isEntityAlive() || caster.isOnSameTeam(e)) continue;
            EntityLivingBase living = (EntityLivingBase) e;
            if (living.hurtResistantTime <= IFRAME_TICKS) return living;
            if (fallback == null) fallback = living;
        }
        return fallback;
    }

    private static void applyBurst(World world, EntityLivingBase caster, EntityLivingBase primary, int amplifier) {
        double radiusSqr = BURST_RADIUS * BURST_RADIUS;
        AxisAlignedBB eb = caster.getEntityBoundingBox();
        AxisAlignedBB area = new AxisAlignedBB(
                eb.minX - BURST_RADIUS, eb.minY - BURST_RADIUS, eb.minZ - BURST_RADIUS,
                eb.maxX + BURST_RADIUS, eb.maxY + BURST_RADIUS, eb.maxZ + BURST_RADIUS);

        for (Entity e : world.getEntitiesWithinAABBExcludingEntity(caster, area)) {
            if (e == primary || !(e instanceof EntityLivingBase) || caster.isOnSameTeam(e)) continue;
            double distSqr = caster.getDistanceSq(e.posX, e.posY, e.posZ);
            if (distSqr < radiusSqr) {
                float aoe = (float) ((double) amplifier * (1.0 - distSqr / radiusSqr) * BURST_SHARE);
                if (aoe > 0.0f) {
                    ((EntityLivingBase) e).attackEntityFrom(
                            MagicDamage.causeIndirectMagicDamage(caster, caster, DamageType.SHOCK), aoe);
                }
            }
        }
    }

    private static EntityLivingBase nearestHostile(EntityLivingBase caster, Entity exclude) {
        double limitSqr = AIM_RADIUS * AIM_RADIUS;
        EntityLivingBase best = null;
        EntityLivingBase bestHittable = null;
        double bestDistSqr = Double.MAX_VALUE;
        double bestHittableDistSqr = Double.MAX_VALUE;

        AxisAlignedBB eb = caster.getEntityBoundingBox();
        AxisAlignedBB area = new AxisAlignedBB(
                eb.minX - AIM_RADIUS, eb.minY - AIM_RADIUS, eb.minZ - AIM_RADIUS,
                eb.maxX + AIM_RADIUS, eb.maxY + AIM_RADIUS, eb.maxZ + AIM_RADIUS);

        for (Entity e : caster.world.getEntitiesWithinAABBExcludingEntity(caster, area)) {
            if (e == exclude || !(e instanceof EntityLivingBase) || !e.isEntityAlive()) continue;
            if (caster.isOnSameTeam(e)) continue;

            double distSqr = caster.getDistanceSq(e);
            if (distSqr > limitSqr) continue;

            EntityLivingBase candidate = (EntityLivingBase) e;
            if (distSqr < bestDistSqr) {
                bestDistSqr = distSqr;
                best = candidate;
            }
            if (candidate.hurtResistantTime <= IFRAME_TICKS && distSqr < bestHittableDistSqr) {
                bestHittableDistSqr = distSqr;
                bestHittable = candidate;
            }
        }
        return bestHittable != null ? bestHittable : best;
    }

    private static void faceEntity(EntityLivingBase living, EntityLivingBase target) {
        double dx = target.posX - living.posX;
        double dy = (target.posY + target.height * 0.5) - (living.posY + living.getEyeHeight());
        double dz = target.posZ - living.posZ;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz < 1.0E-6 && Math.abs(dy) < 1.0E-6) return;

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(horiz, 1.0E-6)));

        living.rotationYaw = yaw;
        living.prevRotationYaw = yaw;
        living.rotationYawHead = yaw;
        living.prevRotationYawHead = yaw;
        living.rotationPitch = pitch;
        living.prevRotationPitch = pitch;

        if (living instanceof EntityPlayerMP) {
            LegacySpell.NETWORK.sendTo(new AeropicFaceMessage(yaw, pitch), (EntityPlayerMP) living);
        }
    }
}
