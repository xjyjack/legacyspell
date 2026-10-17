package legacyspell.entity.projectile;

import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.RayTracer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityEarthEdge extends EntityMagicProjectile {

    public static final float SPEED = 2.0f;
    public static final int LIFETIME = 100;
    private static final double HOMING_STRENGTH = 0.22;
    private static final double HOMING_RANGE = 48;

    private float damage = 0f;
    private boolean homing = false;

    public float lastYaw;
    public float lastPitch;

    public EntityEarthEdge(World world) {
        super(world);
        this.setSize(0.4f, 0.4f);
        this.ignoreFrustumCheck = true;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setHoming(boolean homing) {
        this.homing = homing;
    }

    public void updateOrientation(double mx, double my, double mz) {
        double speed = Math.sqrt(mx * mx + my * my + mz * mz);
        if (speed < 1.0E-3) return;
        double horiz = Math.sqrt(mx * mx + mz * mz);
        this.lastYaw = (float) Math.toDegrees(Math.atan2(mx, mz));
        this.lastPitch = (float) Math.toDegrees(Math.atan2(my, Math.max(horiz, 1.0E-6)));
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

        if (!this.world.isRemote) {

            if (this.homing) {
                this.homeTowardsTarget();
            }

            double length = Math.sqrt(this.motionX * this.motionX
                    + this.motionY * this.motionY
                    + this.motionZ * this.motionZ);
            if (length > 1.0E-4) {
                this.motionX = this.motionX / length * SPEED;
                this.motionY = this.motionY / length * SPEED;
                this.motionZ = this.motionZ / length * SPEED;
            }

            for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this,
                    this.getEntityBoundingBox().grow(0.2))) {
                if (this.canHitEntity(entity)) {
                    this.hit(entity);
                }
            }

            if (this.ticksExisted > LIFETIME) {
                this.setDead();
            }
        }
    }

    private void homeTowardsTarget() {

        EntityLivingBase owner = this.getThrower() instanceof EntityLivingBase
                ? (EntityLivingBase) this.getThrower() : null;
        if (owner == null) return;

        RayTraceResult result = RayTracer.standardEntityRayTrace(this.world, owner, HOMING_RANGE, false);
        Vec3d target;
        if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY
                && result.entityHit instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) result.entityHit;
            target = new Vec3d(living.posX, living.posY + living.height * 0.5, living.posZ);
        } else if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.hitVec != null) {
            target = result.hitVec;
        } else {
            return;
        }

        Vec3d motion = new Vec3d(this.motionX, this.motionY, this.motionZ);
        Vec3d wanted = target.subtract(this.getPositionVector());
        if (wanted.lengthSquared() < 1.0E-8) return;

        Vec3d blended = motion.add(wanted.normalize().subtract(motion.normalize()).scale(HOMING_STRENGTH));
        double speed = Math.max(motion.length(), 1.0E-4);
        this.motionX = blended.x / Math.max(blended.length(), 1.0E-4) * speed;
        this.motionY = blended.y / Math.max(blended.length(), 1.0E-4) * speed;
        this.motionZ = blended.z / Math.max(blended.length(), 1.0E-4) * speed;
        this.velocityChanged = true;
    }

    private boolean canHitEntity(Entity entity) {
        if (entity == this) return false;
        Entity thrower = this.getThrower();
        if (entity == thrower) return false;
        if (!(entity instanceof EntityLivingBase)) return false;
        EntityLivingBase living = (EntityLivingBase) entity;
        if (!living.isEntityAlive() || !living.canBeCollidedWith()) return false;
        return thrower == null || !thrower.isOnSameTeam(living);
    }

    private void hit(Entity target) {

        if (this.world.isRemote) {
            this.spawnImpactParticles(target.posX, target.posY + target.height * 0.5, target.posZ);
            return;
        }

        EntityLivingBase living = (EntityLivingBase) target;
        float before = living.getHealth();
        boolean hurt = living.attackEntityFrom(
                MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FORCE), this.damage);
        if (!hurt || living.getHealth() >= before) {
            living.attackEntityFrom(MagicDamage.causeDirectMagicDamage(this, DamageType.FORCE), this.damage);
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {

        if (result == null) return;

        if (result.entityHit != null) {
            if (this.canHitEntity(result.entityHit)) {
                this.hit(result.entityHit);
            }
            return;
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK && this.homing) {
            this.setDead();
        }
    }

    private void spawnImpactParticles(double x, double y, double z) {
        for (int i = 0; i < 8; i++) {
            this.world.spawnParticle(EnumParticleTypes.PORTAL,
                    x + (this.rand.nextDouble() - 0.5) * 0.8,
                    y + (this.rand.nextDouble() - 0.5) * 0.8,
                    z + (this.rand.nextDouble() - 0.5) * 0.8,
                    (this.rand.nextDouble() - 0.5) * 0.2,
                    (this.rand.nextDouble() - 0.5) * 0.2,
                    (this.rand.nextDouble() - 0.5) * 0.2);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.damage = compound.getFloat("Damage");
        this.homing = compound.getBoolean("Homing");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setFloat("Damage", this.damage);
        compound.setBoolean("Homing", this.homing);
    }
}
