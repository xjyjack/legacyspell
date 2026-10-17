package legacyspell.entity.projectile;

import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import legacyspell.registry.LegacyPotions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityObsidianRod extends EntityMagicProjectile {

    public static final float SPEED = 3.0f;
    public static final int LIFETIME = 100;
    private static final double INACCURACY = 0.05;

    private float damage = 0f;
    private int effectLength = 0;

    public EntityObsidianRod(World world) {
        super(world);
        this.setSize(0.25f, 0.25f);
    }

    public EntityObsidianRod(World world, EntityLivingBase shooter, int effectLength) {
        this(world);
        if (shooter != null) this.setCaster(shooter);
        this.effectLength = effectLength;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setEffectLength(int effectLength) {
        this.effectLength = effectLength;
    }

    public int getEffectLength() {
        return this.effectLength;
    }

    public void shootWithInaccuracy(Vec3d look) {

        Vec3d offset = new Vec3d(
                (this.rand.nextDouble() - 0.5) * INACCURACY,
                (this.rand.nextDouble() - 0.5) * INACCURACY,
                (this.rand.nextDouble() - 0.5) * INACCURACY);

        Vec3d motion = look.normalize().add(offset).normalize().scale(SPEED);
        this.shoot(motion.x, motion.y, motion.z, 1.0f, 0f);
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

        double length = Math.sqrt(this.motionX * this.motionX
                + this.motionY * this.motionY
                + this.motionZ * this.motionZ);
        if (length > 1.0E-4) {
            this.motionX = this.motionX / length * SPEED;
            this.motionY = this.motionY / length * SPEED;
            this.motionZ = this.motionZ / length * SPEED;
        }

        if (this.world.isRemote) {
            this.spawnTrailParticles();
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {

        if (result == null) return;

        if (result.entityHit instanceof EntityLivingBase) {

            if (!this.world.isRemote) {
                EntityLivingBase target = (EntityLivingBase) result.entityHit;
                if (this.canHitEntity(target)) {
                    if (this.effectLength > 0) {
                        target.addPotionEffect(new PotionEffect(LegacyPotions.anchored, this.effectLength, 0));
                    }
                    float before = target.getHealth();
                    boolean hurt = target.attackEntityFrom(
                            MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FORCE),
                            this.damage);
                    if (!hurt || target.getHealth() >= before) {
                        target.attackEntityFrom(
                                MagicDamage.causeDirectMagicDamage(this, DamageType.FORCE), this.damage);
                    }
                }
            }

            this.setDead();
            return;
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            if (!this.world.isRemote) {
                this.world.playSound(null, this.posX, this.posY, this.posZ,
                        SoundEvents.BLOCK_STONE_HIT, this.getSoundCategory(), 0.6f, 1.2f);
            }
            this.setDead();
        }
    }

    private boolean canHitEntity(Entity entity) {
        Entity thrower = this.getThrower();
        if (entity == thrower) return false;
        if (!(entity instanceof EntityLivingBase)) return false;
        EntityLivingBase living = (EntityLivingBase) entity;
        if (!living.isEntityAlive() || !living.canBeCollidedWith()) return false;
        return thrower == null || !thrower.isOnSameTeam(living);
    }

    private void spawnTrailParticles() {
        for (int i = 0; i < 2; i++) {
            this.world.spawnParticle(EnumParticleTypes.PORTAL,
                    this.posX + (this.rand.nextDouble() - 0.5) * 0.2,
                    this.posY + (this.rand.nextDouble() - 0.5) * 0.2,
                    this.posZ + (this.rand.nextDouble() - 0.5) * 0.2,
                    0, 0, 0);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.damage = compound.getFloat("Damage");
        this.effectLength = compound.getInteger("EffectLength");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setFloat("Damage", this.damage);
        compound.setInteger("EffectLength", this.effectLength);
    }
}
