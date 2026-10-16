package legacyspell.entity.projectile;

import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import legacyspell.registry.LegacySpells;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;

/**
 * 游龙（Jade Loong）—— 御龙滑翔（Dragon Glide）的投射物，行为移植自 ISS: Magic From The East 的
 * {@code JadeLoong.java}。
 *
 * <p>要点：
 * <ul>
 *   <li>无重力，速度 0.6/tick，寿命 80 tick，到期时播放碎裂特效并消失；</li>
 *   <li>原版用 {@code noPhysics} + 空实现的 {@code onHitBlock} 让游龙<b>直接穿过方块</b>，
 *       这里通过"不把命中写回 {@code inGround}"达到同样效果；</li>
 *   <li>穿透路径上所有敌人，造成 shock 伤害，并抵消命中带来的击退
 *       （原版 {@code DamageSources.ignoreNextKnockback}）；</li>
 *   <li>尾迹为绕运动方向螺旋的玉色粒子（原版 {@code trailParticles()}）。</li>
 * </ul>
 */
public class EntityJadeLoong extends EntityMagicProjectile {

    public static final int LIFETIME = 80;

    private final List<Entity> victims = new ArrayList<>();

    public EntityJadeLoong(World world) {
        super(world);
        this.setSize(1.0f, 1.2f);
        // 模型比碰撞箱长得多（约 3 格），关掉视锥剔除避免出屏Pop
        this.ignoreFrustumCheck = true;
    }

    @Override
    public int getLifetime() {
        return LIFETIME;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public void onUpdate() {

        super.onUpdate();

        if (this.isDead) return;

        if (this.world.isRemote) {
            this.spawnTrailParticles();
            return;
        }

        if (this.ticksExisted > LIFETIME) {
            this.spawnImpactParticles(this.posX, this.posY, this.posZ);
            this.setDead();
            return;
        }

        // 原版 handleHitDetection：对碰撞箱周围 0.25 格内的所有敌人结算伤害（穿透）
        for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this,
                this.getEntityBoundingBox().grow(0.25))) {
            if (this.canHitEntity(entity)) this.hit(entity);
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {

        if (result == null || this.isDead) return;

        if (result.entityHit != null) {
            if (!this.world.isRemote && this.canHitEntity(result.entityHit)) {
                this.hit(result.entityHit);
            }
            return;
        }

        // 原版 onHitBlock 为空实现：游龙穿过方块，只留一点粒子提示
        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            if (this.world instanceof WorldServer) {
                ((WorldServer) this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC,
                        result.hitVec.x, result.hitVec.y, result.hitVec.z, 4, 0.1, 0.1, 0.1, 0.05);
            }
        }
    }

    private boolean canHitEntity(Entity entity) {
        if (entity == this || entity == this.getThrower()) return false;
        if (!(entity instanceof EntityLivingBase)) return false;
        EntityLivingBase living = (EntityLivingBase) entity;
        if (!living.isEntityAlive() || !living.canBeCollidedWith()) return false;
        if (this.victims.contains(entity)) return false;
        Entity thrower = this.getThrower();
        return thrower == null || !thrower.isOnSameTeam(living);
    }

    private void hit(Entity target) {

        this.victims.add(target);

        float multiplier = this.damageMultiplier > 0.0f ? this.damageMultiplier : 1.0f;
        float damage = LegacySpells.dragon_glide.getProperty(Spell.DAMAGE).floatValue() * multiplier;

        // 原版 DamageSources.ignoreNextKnockback：保存并恢复 motion 以抵消击退
        double motionX = target.motionX;
        double motionY = target.motionY;
        double motionZ = target.motionZ;

        target.attackEntityFrom(
                MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK),
                damage);

        target.motionX = motionX;
        target.motionY = motionY;
        target.motionZ = motionZ;

        if (!this.world.isRemote) {
            this.spawnImpactParticles(target.posX, target.posY + target.height * 0.5, target.posZ);
        }
    }

    /** 原版 {@code impactParticles()}：玉色碎裂。 */
    private void spawnImpactParticles(double x, double y, double z) {
        if (this.world instanceof WorldServer) {
            WorldServer server = (WorldServer) this.world;
            server.spawnParticle(EnumParticleTypes.CRIT, x, y, z, 5, 0.4, 0.4, 0.4, 0.08);
            server.spawnParticle(EnumParticleTypes.CRIT_MAGIC, x, y, z, 2, 0.1, 0.1, 0.1, 0.05);
        }
    }

    /** 原版 {@code trailParticles()}：绕运动方向螺旋的玉色尾迹。 */
    private void spawnTrailParticles() {
        Vec3d vec = new Vec3d(this.motionX, this.motionY, this.motionZ);
        float yHeading = -((float) (Math.atan2(vec.z, vec.x) * (180.0 / Math.PI)) + 90.0f);
        float radius = 0.25f;
        int steps = 2;

        double x2 = this.posX;
        double x1 = x2 - vec.x;
        double y2 = this.posY;
        double y1 = y2 - vec.y;
        double z2 = this.posZ;
        double z1 = z2 - vec.z;

        for (int j = 0; j < steps; j++) {
            float offset = (1f / steps) * j;
            double radians = ((this.ticksExisted + offset) / 7.5f) * 360.0 * (Math.PI / 180.0);
            Vec3d swirl = new Vec3d(Math.cos(radians) * radius, Math.sin(radians) * radius, 0.0)
                    .rotateYaw((float) Math.toRadians(yHeading));

            double x = x1 + (x2 - x1) * offset + swirl.x;
            double y = y1 + (y2 - y1) * offset + swirl.y + this.height * 0.5;
            double z = z1 + (z2 - z1) * offset + swirl.z;

            this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
    }
}
