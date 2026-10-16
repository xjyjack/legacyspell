package legacyspell.potion;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import legacyspell.LegacySpell;
import legacyspell.spell.AeropicChain;
import legacyspell.util.MotionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Set;

/**
 * 航天腾跃的连击驱动药水。
 *
 * <p>除了原有的"命中→击退→继续冲刺"逻辑外，从第二击开始会自动把施法者的
 * <b>视觉方向与运动方向</b>一起掰向「上一击命中那一刻，距离玩家最近的非友善实体」，
 * 让连击像闪电链一样在敌人之间自动串起来。
 */
public class PotionAcrobatics extends Potion {

    private static final double CHAIN_PUSH = 2.5;
    private static final double LAUNCH_Y = 0.9;
    private static final double BURST_RADIUS = 4.0;
    private static final double BURST_SHARE = 0.5;

    /** 只有目标进入这个半径才自动转向，避免把玩家猛地甩向天边的敌人。 */
    private static final double AIM_RADIUS = 32.0;

    /** 无敌帧阈值：1.12.2 默认 maxHurtResistantTime=20，超过一半就吃不到伤害。 */
    private static final int IFRAME_TICKS = 10;

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
        if (world.isRemote) return;

        living.fallDistance = 0.0f;

        AxisAlignedBB swept = sweptBox(living);
        EntityLivingBase target = firstTarget(world, living, swept);

        boolean connected = false;
        if (target != null) {
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

        int hitIndex = AeropicChain.hitIndex(living);
        int remaining = AeropicChain.remaining(living);
        if (remaining <= 0) return;

        if (connected) {
            hitIndex = hitIndex + 1;
            AeropicChain.setHitIndex(living, hitIndex);
            AeropicChain.rememberChainTarget(living, nearestHostile(living, target));
        }

        if (hitIndex >= 2) {
            EntityLivingBase next = AeropicChain.chainTarget(living);
            if (next != null && next.isEntityAlive() && next != target
                    && living.getDistanceSq(next) < AIM_RADIUS * AIM_RADIUS) {
                faceEntity(living, next);
            }
        }

        if (remaining > 1) {
            AeropicChain.setRemaining(living, remaining - 1);
        } else {
            AeropicChain.setRemaining(living, 0);
        }

        Vec3d forward = living.getLookVec();
        double push = remaining > 1 ? CHAIN_PUSH : 0.0;
        MotionHelper.setMotion(living, forward.x * push, LAUNCH_Y, forward.z * push);
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

    /**
     * 找出以施法者为中心、{@value #AIM_RADIUS} 格内最近的非友善实体。
     *
     * <p>优先挑当前「吃得到伤害」的目标（不在无敌帧里的），这样连击不会一头撞在
     * 刚刚才被打过的敌人身上；附近完全没有可打目标时，才退而求其次选任意一个。
     */
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

    /**
     * 把施法者（尤其是玩家）的脑袋拧向目标，并同步给客户端，让「看到的」和
     * 「冲出去的」是同一个方向。
     */
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

        // 玩家客户端会不断用自己的视角覆盖服务端，必须再推一个坐标包把视角钉住。
        // X/Y/Z 带相对标记且增量为 0，所以只改朝向、不产生位移。
        // teleportId 用一个服务端不可能达到的值：客户端的 CPacketConfirmTeleport 回执
        // 永远对不上 NetHandlerPlayServer.teleportId，从而不会触发 setPositionAndRotation 吸附。
        if (living instanceof EntityPlayerMP) {
            Set<SPacketPlayerPosLook.EnumFlags> flags = EnumSet.of(
                    SPacketPlayerPosLook.EnumFlags.X,
                    SPacketPlayerPosLook.EnumFlags.Y,
                    SPacketPlayerPosLook.EnumFlags.Z);
            ((EntityPlayerMP) living).connection.sendPacket(
                    new SPacketPlayerPosLook(living.posX, living.posY, living.posZ, yaw, pitch, flags,
                            Integer.MAX_VALUE));
        }
    }
}
