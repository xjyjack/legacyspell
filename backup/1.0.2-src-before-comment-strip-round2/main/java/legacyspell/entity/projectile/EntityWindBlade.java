package legacyspell.entity.projectile;

import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.registry.WizardrySounds;
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
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityWindBlade extends EntityMagicProjectile {

    public static final int EXPIRE_TIME = 50;
    public static final float INITIAL_RADIUS = 0.6f;
    public static final float RADIUS_GROWTH = 0.06f;
    public static final float MAX_RADIUS = 1.5f;
    public static final int BLOCK_HIT_INTERVAL = 20;
    public static final double DRAG = 0.99;
    public static final float VELOCITY_MULTIPLIER = 1.2f;

    private float radius = INITIAL_RADIUS;
    private float prevRadius = INITIAL_RADIUS;
    private int blockHitCooldown = 0;

    private final List<Entity> victims = new ArrayList<>();

    public EntityWindBlade(World world) {
        super(world);
        this.setSize(0.5f, 0.5f);
    }

    @Override
    public int getLifetime() {
        return EXPIRE_TIME;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    public float getRadius() {
        return radius;
    }

    public float getPrevRadius() {
        return prevRadius;
    }

    @Override
    public void onUpdate() {

        if (this.blockHitCooldown > 0) --this.blockHitCooldown;

        this.prevRadius = this.radius;
        this.radius = Math.min(MAX_RADIUS, this.radius + RADIUS_GROWTH);

        super.onUpdate();

        this.motionX /= DRAG;
        this.motionY /= DRAG;
        this.motionZ /= DRAG;

        if (this.isDead) return;

        if (this.world.isRemote) {
            this.spawnTrailParticles();
            this.spawnHitFeedback();
        } else {
            for (Entity target : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBladeBox())) {
                if (this.canCut(target)) this.cut(target);
            }
        }
    }

    private AxisAlignedBB getBladeBox() {
        double halfWidth = this.radius * 0.7;
        return new AxisAlignedBB(
                this.posX - halfWidth, this.posY - 0.5, this.posZ - halfWidth,
                this.posX + halfWidth, this.posY + 0.5, this.posZ + halfWidth);
    }

    private boolean canCut(Entity entity) {
        return entity != this.getThrower()
                && entity instanceof EntityLivingBase
                && !entity.isDead
                && !this.victims.contains(entity);
    }

    private void cut(Entity target) {

        this.victims.add(target);

        float damage = LegacySpells.wind_blade.getProperty(Spell.DAMAGE).floatValue() * this.damageMultiplier;

        target.attackEntityFrom(
                MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK),
                damage);
    }

    private void spawnTrailParticles() {

        float width = this.radius * 1.4f;
        int count = Math.max(1, Math.round(width / 0.8f));
        float radians = 0.017453292f * this.rotationYaw;

        for (int i = 0; i < count; i++) {

            double offset = ((i + 0.5) / count - 0.5) * width;
            double px = this.posX + offset * Math.cos(radians);
            double py = this.posY;
            double pz = this.posZ - offset * Math.sin(radians);

            double dx = this.rand.nextDouble() * 0.2 - 0.1;
            double dy = this.rand.nextDouble() * 0.2 - 0.1;
            double dz = this.rand.nextDouble() * 0.2 - 0.1;

            this.world.spawnParticle(EnumParticleTypes.CLOUD, px + dx, py + dy, pz + dz, dx, dy, dz);
        }
    }

    private void spawnHitFeedback() {
        for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBladeBox())) {
            if (entity == this.getThrower() || !(entity instanceof EntityLivingBase)) continue;

            for (int i = 0; i < 4; i++) {
                this.world.spawnParticle(EnumParticleTypes.CLOUD,
                        entity.posX + (this.rand.nextDouble() - 0.5) * entity.width * 2,
                        entity.posY + this.rand.nextDouble() * entity.height,
                        entity.posZ + (this.rand.nextDouble() - 0.5) * entity.width * 2,
                        (this.rand.nextDouble() - 0.5) * 0.3,
                        (this.rand.nextDouble() - 0.5) * 0.3,
                        (this.rand.nextDouble() - 0.5) * 0.3);
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {

        if (result == null) return;

        if (result.entityHit != null) {
            if (this.canCut(result.entityHit) && !this.world.isRemote) this.cut(result.entityHit);
            return;
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {

            if (this.blockHitCooldown > 0) return;
            this.blockHitCooldown = BLOCK_HIT_INTERVAL;

            if (this.world.isRemote) {
                for (int i = 0; i < 25; i++) {
                    this.world.spawnParticle(EnumParticleTypes.CLOUD,
                            this.posX + (this.rand.nextDouble() - 0.5) * 1.5,
                            this.posY + (this.rand.nextDouble() - 0.5) * 1.5,
                            this.posZ + (this.rand.nextDouble() - 0.5) * 1.5,
                            (this.rand.nextDouble() - 0.5) * 0.3,
                            (this.rand.nextDouble() - 0.5) * 0.3,
                            (this.rand.nextDouble() - 0.5) * 0.3);
                }
            } else {
                this.playSound(WizardrySounds.ENTITY_ICE_SPIKE_EXTEND, 0.5f, 1.2f + this.rand.nextFloat() * 0.2f);
            }
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.radius = compound.getFloat("BladeRadius");
        this.prevRadius = this.radius;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setFloat("BladeRadius", this.radius);
    }
}
