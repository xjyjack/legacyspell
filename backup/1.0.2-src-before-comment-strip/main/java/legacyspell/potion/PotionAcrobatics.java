package legacyspell.potion;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import legacyspell.LegacySpell;
import legacyspell.spell.AeropicChain;
import legacyspell.util.MotionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Drives the 航天腾跃 (Aeropic) lunge. Kept as a potion so the same per-tick hook runs on both sides and
 * the render layer can key the visual spin off it.
 *
 * <p>Each tick it sweeps the movement since the last tick and, on the first fresh connect, deals the
 * spell damage to that single target (the burst hits the rest), launches the caster straight up, and
 * chains the lunge forward — until {@link #HITS_TAG} runs out ("只要击中目标就可以重复进行").</p>
 */
public class PotionAcrobatics extends Potion {

    /** Forward strength of each chained lunge, in blocks/tick. */
    private static final double CHAIN_PUSH = 2.5;
    /** Upward kick applied on every connect ("抛向空中"); eased down from 1.2 so the leap is gentler. */
    private static final double LAUNCH_Y = 0.9;
    /** Falloff burst radius and damage share around a connect. */
    private static final double BURST_RADIUS = 4.0;
    private static final double BURST_SHARE = 0.5;

    public PotionAcrobatics() {
        super(false, 0x99ccff);
        this.setRegistryName(LegacySpell.MODID, "acrobatics");
        this.setPotionName("potion." + LegacySpell.MODID + ".acrobatics");
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase living, int amplifier) {
        World world = living.world;
        if (world.isRemote) return; // visuals live in ClientEventHandler

        AxisAlignedBB swept = sweptBox(living);
        EntityLivingBase target = firstTarget(world, living, swept);

        boolean connected = false;
        if (target != null) {
            // attackEntityFrom returns false while the victim is still in i-frames, so re-touching the
            // same enemy does not re-trigger the chain — only a genuinely fresh victim does.
            connected = target.attackEntityFrom(
                    MagicDamage.causeIndirectMagicDamage(living, living, DamageType.SHOCK), (float) amplifier);
            if (connected) {
                target.hurtResistantTime = 20;
                target.motionY += 0.2;
                target.velocityChanged = true;
                applyBurst(world, living, target, amplifier);
                world.playSound(null, living.posX, living.posY + 1.0, living.posZ,
                        SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, living.getSoundCategory(), 4.0f, 1.2f);
            }
        }

        if (connected) {
            int remaining = AeropicChain.remaining(living);
            if (remaining > 1) {
                // Chain onwards: keep the lift and drive forward along the current look direction.
                AeropicChain.setRemaining(living, remaining - 1);
                Vec3d forward = living.getLookVec();
                MotionHelper.setMotion(living, forward.x * CHAIN_PUSH, LAUNCH_Y, forward.z * CHAIN_PUSH);
            } else {
                AeropicChain.setRemaining(living, 0);
                MotionHelper.setMotion(living, 0.0, LAUNCH_Y, 0.0);
            }
        } else {
            living.fallDistance = 0.0f;
        }
    }

    /** The volume swept by the caster's movement since the previous tick, unioned with their own box. */
    private static AxisAlignedBB sweptBox(EntityLivingBase living) {
        Vec3d from = living.getPositionVector();
        Vec3d to = new Vec3d(from.x + living.motionX * 2.0, from.y + living.motionY * 2.0, from.z + living.motionZ * 2.0);

        AxisAlignedBB box = new AxisAlignedBB(
                Math.min(from.x, to.x), Math.min(from.y, to.y), Math.min(from.z, to.z),
                Math.max(from.x, to.x), Math.max(from.y, to.y), Math.max(from.z, to.z));
        // Build enlarged boxes explicitly: grow()'s in-place semantics differ between mappings, and we
        // must never mutate the entity's real bounding box.
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
        for (Entity e : world.getEntitiesWithinAABBExcludingEntity(caster, box)) {
            if (e instanceof EntityLivingBase && e.isEntityAlive() && !caster.isOnSameTeam(e)) {
                return (EntityLivingBase) e;
            }
        }
        return null;
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
}
