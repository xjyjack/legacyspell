package legacyspell.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityGaiaPortal extends Entity {

    private static final int MAX_AGE = 20;
    private static final int PORTAL_COLOR = 0x55FF55;

    private int ownerId = -1;
    private int delay = 5;
    private float damage = 0f;
    private boolean bladeHoming = false;

    private Vec3d aim = new Vec3d(0, 1, 0);

    public EntityGaiaPortal(World world) {
        super(world);
        this.setSize(0.5f, 0.5f);
        this.noClip = true;
        this.setNoGravity(true);
        this.ignoreFrustumCheck = true;
    }

    public EntityGaiaPortal(World world, EntityLivingBase owner, int delay) {
        this(world);
        if (owner != null) this.ownerId = owner.getEntityId();
        this.delay = delay;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setBladeHoming(boolean homing) {
        this.bladeHoming = homing;
    }

    public void setAim(Vec3d aim) {
        this.aim = aim.lengthSquared() < 1.0E-8 ? new Vec3d(0, 1, 0) : aim.normalize();
    }

    public Vec3d getAim() {
        return this.aim;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {

        super.onUpdate();

        if (this.world.isRemote) {
            this.spawnPortalParticles();
            return;
        }

        if (this.ticksExisted == this.delay) {
            this.shootBlade();
        }

        if (this.ticksExisted > MAX_AGE) {
            this.setDead();
        }
    }

    private void shootBlade() {

        EntityLivingBase owner = this.getOwnerEntity();

        EntityEarthEdge blade = new EntityEarthEdge(this.world);
        blade.setPosition(this.posX, this.posY, this.posZ);
        if (owner != null) blade.setCaster(owner);
        blade.setDamage(this.damage);
        blade.setHoming(this.bladeHoming);
        blade.shoot(this.aim.x, this.aim.y, this.aim.z, EntityEarthEdge.SPEED, 0f);

        this.world.spawnEntity(blade);

        this.world.playSound(null, this.posX, this.posY, this.posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.1f, 1.0f);
    }

    private void spawnPortalParticles() {
        for (int i = 0; i < 2; i++) {
            ParticleBuilder.create(ParticleBuilder.Type.LEAF)
                    .pos(this.posX + (this.rand.nextDouble() - 0.5) * 0.6,
                            this.posY + (this.rand.nextDouble() - 0.5) * 0.6,
                            this.posZ + (this.rand.nextDouble() - 0.5) * 0.6)
                    .vel((this.rand.nextDouble() - 0.5) * 0.1,
                            (this.rand.nextDouble() - 0.5) * 0.1,
                            (this.rand.nextDouble() - 0.5) * 0.1)
                    .clr(PORTAL_COLOR).time(12).spawn(this.world);
        }
    }

    public EntityLivingBase getOwnerEntity() {
        Entity entity = this.ownerId == -1 ? null : this.world.getEntityByID(this.ownerId);
        return entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.ownerId = compound.getInteger("OwnerId");
        this.delay = compound.getInteger("Delay");
        this.damage = compound.getFloat("Damage");
        this.bladeHoming = compound.getBoolean("Homing");
        this.ticksExisted = compound.getInteger("Age");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("OwnerId", this.ownerId);
        compound.setInteger("Delay", this.delay);
        compound.setFloat("Damage", this.damage);
        compound.setBoolean("Homing", this.bladeHoming);
        compound.setInteger("Age", this.ticksExisted);
    }
}
