package legacyspell.entity.projectile;

import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import legacyspell.registry.LegacySpells;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityJadeLoong extends EntityMagicProjectile {

    public static final int LIFETIME = 80;

    private static final int JADE_COLOR = 0x128575;

    private static final float TRAIL_RADIUS = 0.3f;
    private static final int TRAIL_POINTS = 8;
    private static final int IMPACT_SPARKS = 12;
    private static final int IMPACT_SPARKLES = 6;
    private static final int IMPACT_ARCS = 2;

    private final List<Entity> victims = new ArrayList<>();

    private boolean impactSpawned = false;

    public EntityJadeLoong(World world) {
        super(world);
        this.setSize(1.0f, 1.2f);
        this.ignoreFrustumCheck = true;
    }

    public float lastYaw;
    public float lastPitch;

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
    public void setDead() {
        if (!this.isDead && this.ticksExisted > 1 && this.world.isRemote && !this.impactSpawned) {
            this.impactSpawned = true;
            this.spawnImpactParticles(this.posX, this.posY + this.height * 0.5, this.posZ);
        }
        super.setDead();
    }

    @Override
    public void onUpdate() {

        super.onUpdate();

        if (this.isDead) return;

        if (!this.world.isRemote && this.ticksExisted > LIFETIME) {
            this.setDead();
            return;
        }

        for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this,
                this.getEntityBoundingBox().grow(0.25))) {
            if (this.canHitEntity(entity)) this.hit(entity);
        }

        if (this.world.isRemote) {
            this.spawnTrailParticles();
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

        if (result.typeOfHit == RayTraceResult.Type.BLOCK && this.world.isRemote && !this.impactSpawned) {
            this.impactSpawned = true;
            Vec3d hit = result.hitVec;
            this.spawnImpactParticles(hit.x, hit.y, hit.z);
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

        if (this.world.isRemote) {
            this.spawnImpactParticles(target.posX, target.posY + target.height * 0.5, target.posZ);
            return;
        }

        float multiplier = this.damageMultiplier > 0.0f ? this.damageMultiplier : 1.0f;
        float damage = LegacySpells.dragon_glide.getProperty(Spell.DAMAGE).floatValue() * multiplier;

        double motionX = target.motionX;
        double motionY = target.motionY;
        double motionZ = target.motionZ;

        target.attackEntityFrom(
                MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK),
                damage);

        target.motionX = motionX;
        target.motionY = motionY;
        target.motionZ = motionZ;
    }

    private void spawnTrailParticles() {
        Vec3d forward = new Vec3d(this.motionX, this.motionY, this.motionZ).normalize();
        Vec3d ref = Math.abs(forward.y) < 0.99 ? new Vec3d(0, 1, 0) : new Vec3d(1, 0, 0);
        Vec3d side = forward.crossProduct(ref).normalize();
        Vec3d top = side.crossProduct(forward).normalize();

        for (int j = 0; j < TRAIL_POINTS; j++) {
            float t = (float) j / TRAIL_POINTS;
            double radians = ((this.ticksExisted + t) / 5.0f) * 360.0 * (Math.PI / 180.0);
            double offsetX = Math.cos(radians) * TRAIL_RADIUS;
            double offsetY = Math.sin(radians) * TRAIL_RADIUS;
            double cx = this.posX + offsetX * side.x + offsetY * top.x;
            double cy = this.posY + this.height * 0.5 + offsetX * side.y + offsetY * top.y;
            double cz = this.posZ + offsetX * side.z + offsetY * top.z;

            ParticleBuilder.create(Type.SPARKLE).pos(cx, cy, cz)
                    .clr(JADE_COLOR).time(16).spawn(this.world);
        }
    }

    private void spawnImpactParticles(double x, double y, double z) {
        for (int i = 0; i < IMPACT_SPARKS; i++) {
            ParticleBuilder.create(Type.SPARK, this.rand, x, y, z, 0.6, true)
                    .clr(JADE_COLOR).spawn(this.world);
        }
        for (int i = 0; i < IMPACT_SPARKLES; i++) {
            ParticleBuilder.create(Type.SPARKLE, this.rand, x, y, z, 0.4, false)
                    .vel(0, 0.06, 0).clr(JADE_COLOR)
                    .time(20 + this.rand.nextInt(10)).spawn(this.world);
        }
        for (int i = 0; i < IMPACT_ARCS; i++) {
            double ax = x + (this.rand.nextDouble() - 0.5) * 1.4;
            double ay = y + (this.rand.nextDouble() - 0.5) * 1.4;
            double az = z + (this.rand.nextDouble() - 0.5) * 1.4;
            ParticleBuilder.create(Type.LIGHTNING).pos(x, y, z).target(ax, ay, az)
                    .clr(JADE_COLOR).spawn(this.world);
        }
        ParticleBuilder.create(Type.FLASH).pos(x, y, z)
                .clr(JADE_COLOR).scale(1.6f).spawn(this.world);
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
