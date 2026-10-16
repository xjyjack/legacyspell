package legacyspell.entity.projectile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityEnderChain extends Entity implements IEntityAdditionalSpawnData {

    private static final double BREAK_DISTANCE_SQ = 12.0 * 12.0;
    private static final int DAMAGE_COOLDOWN = 10;

    private int ownerId = -1;
    private int victimId = -1;

    private float chainHealth = 15.0f;
    private int lifetime = 200;
    private float restraintStrength = 0.015f;
    private int invulnerableTicks = 0;

    public EntityEnderChain(World world) {
        super(world);
        this.setSize(0.4f, 0.4f);
        this.noClip = true;
        this.setNoGravity(true);
        this.ignoreFrustumCheck = true;
    }

    public EntityEnderChain(World world, EntityLivingBase owner, EntityLivingBase victim,
                           double x, double y, double z) {
        this(world);
        if (owner != null) this.ownerId = owner.getEntityId();
        if (victim != null) this.victimId = victim.getEntityId();
        this.setPosition(x, y, z);
    }

    public void setChainHealth(float health) {
        this.chainHealth = health;
    }

    public float getChainHealth() {
        return this.chainHealth;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public void setRestraintStrength(float strength) {
        this.restraintStrength = strength;
    }

    public Entity getOwnerEntity() {
        return this.ownerId == -1 ? null : this.world.getEntityByID(this.ownerId);
    }

    public EntityLivingBase getVictim() {
        Entity entity = this.victimId == -1 ? null : this.world.getEntityByID(this.victimId);
        return entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {

        super.onUpdate();

        if (this.invulnerableTicks > 0) --this.invulnerableTicks;

        EntityLivingBase victim = this.getVictim();

        if (this.world.isRemote) {
            this.spawnParticles();
            return;
        }

        if (victim == null || victim.isDead
                || victim.getDistanceSq(this) > BREAK_DISTANCE_SQ
                || this.ticksExisted > this.lifetime
                || this.chainHealth <= 0.0f) {
            this.breakChain();
            return;
        }

        Vec3d displacement = victim.getPositionVector().subtract(this.getPositionVector());
        if (displacement.lengthSquared() > 1.0E-8) {
            Vec3d delta = displacement.scale(-this.restraintStrength);
            victim.motionX += delta.x;
            victim.motionY += delta.y;
            victim.motionZ += delta.z;
            victim.velocityChanged = true;
            if (victim.motionY >= 0.0) victim.fallDistance = 0.0f;
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {

        if (this.world.isRemote || this.isDead || this.invulnerableTicks > 0) return false;

        Entity attacker = source.getTrueSource();
        if (attacker != null && attacker.getEntityId() == this.ownerId) return false;

        this.chainHealth -= amount;

        if (this.chainHealth <= 0.0f) {
            this.breakChain();
        } else {
            this.world.playSound(null, this.posX, this.posY, this.posZ,
                    SoundEvents.BLOCK_STONE_HIT, SoundCategory.NEUTRAL, 0.8f, 1.2f);
            this.invulnerableTicks = DAMAGE_COOLDOWN;
        }
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    public void breakChain() {
        if (!this.world.isRemote) {
            this.world.playSound(null, this.posX, this.posY, this.posZ,
                    SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.NEUTRAL, 1.0f, 0.8f);
        }
        this.setDead();
    }

    private void spawnParticles() {
        EntityLivingBase victim = this.getVictim();
        if (victim == null) return;

        Vec3d start = this.getPositionVector();
        Vec3d end = victim.getPositionVector().add(0, victim.height * 0.5, 0);

        for (int i = 0; i <= 4; i++) {
            double t = i / 4.0;
            this.world.spawnParticle(EnumParticleTypes.PORTAL,
                    start.x + (end.x - start.x) * t + (this.rand.nextDouble() - 0.5) * 0.2,
                    start.y + (end.y - start.y) * t + (this.rand.nextDouble() - 0.5) * 0.2,
                    start.z + (end.z - start.z) * t + (this.rand.nextDouble() - 0.5) * 0.2,
                    0, 0, 0);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.ownerId = compound.getInteger("OwnerId");
        this.victimId = compound.getInteger("VictimId");
        this.chainHealth = compound.getFloat("ChainHealth");
        this.lifetime = compound.getInteger("Lifetime");
        this.restraintStrength = compound.getFloat("Restraint");
        this.ticksExisted = compound.getInteger("Age");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("OwnerId", this.ownerId);
        compound.setInteger("VictimId", this.victimId);
        compound.setFloat("ChainHealth", this.chainHealth);
        compound.setInteger("Lifetime", this.lifetime);
        compound.setFloat("Restraint", this.restraintStrength);
        compound.setInteger("Age", this.ticksExisted);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeInt(this.ownerId);
        buffer.writeInt(this.victimId);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.ownerId = additionalData.readInt();
        this.victimId = additionalData.readInt();
    }
}
