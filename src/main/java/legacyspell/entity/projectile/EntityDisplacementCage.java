package legacyspell.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityDisplacementCage extends Entity {

    private static final double MAX_HEIGHT = 256;
    private static final double TELEPORT_MARGIN = 0.99;
    private static final int RING_COUNT = 20;
    private static final int RING_INTERVAL = 2;
    private static final double[] RING_HEIGHTS = {0.1, 0.7, 1.3, 1.9};
    private static final int RADIAL_LAYERS = 2;
    private static final double RADIAL_GAP = 0.06;
    private static final double PILLAR_SPREAD = 0.5;
    private static final int MAX_RISE = 32;

    private int ownerId = -1;
    private float radius = 6f;
    private int duration = 35;

    private final List<EntityLivingBase> trackedTargets = new ArrayList<>();

    public EntityDisplacementCage(World world) {
        super(world);
        this.setSize(0.5f, 0.5f);
        this.noClip = true;
        this.setNoGravity(true);
        this.ignoreFrustumCheck = true;
    }

    public EntityDisplacementCage(World world, EntityLivingBase owner, double x, double y, double z,
                                  float radius, int duration) {
        this(world);
        if (owner != null) this.ownerId = owner.getEntityId();
        this.radius = radius;
        this.duration = duration;
        this.setPosition(x, y, z);
    }

    public float getRadius() {
        return this.radius;
    }

    public int getDuration() {
        return this.duration;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {

        super.onUpdate();

        if (this.world.isRemote) {
            this.spawnRingParticles();
            return;
        }

        EntityLivingBase owner = this.getOwnerEntity();

        AxisAlignedBB search = new AxisAlignedBB(
                this.posX - this.radius, this.posY - MAX_HEIGHT, this.posZ - this.radius,
                this.posX + this.radius, this.posY + MAX_HEIGHT, this.posZ + this.radius);

        for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, search)) {
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase target = (EntityLivingBase) entity;
            if (!target.isEntityAlive()) continue;
            if (target instanceof EntityPlayer && (((EntityPlayer) target).isSpectator()
                    || ((EntityPlayer) target).isCreative())) continue;
            if (owner != null && (target == owner || owner.isOnSameTeam(target))) continue;
            if (this.trackedTargets.contains(target)) continue;

            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            if (dx * dx + dz * dz < this.radius * this.radius) {
                this.trackedTargets.add(target);
                this.world.playSound(null, target.posX, target.posY, target.posZ,
                        SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 2.0f, 1.0f);
            }
        }

        for (int i = 0; i < this.trackedTargets.size(); i++) {
            EntityLivingBase tracked = this.trackedTargets.get(i);
            if (tracked == null || !tracked.isEntityAlive()) {
                this.trackedTargets.remove(i);
                i--;
                continue;
            }

            double dx = tracked.posX - this.posX;
            double dz = tracked.posZ - this.posZ;
            if (dx * dx + dz * dz <= this.radius * this.radius) continue;

            Vec3d dest = this.mirrorAcrossCentre(tracked);
            this.trackedTargets.remove(i);
            i--;

            if (dest == null) continue;

            double fromX = tracked.posX;
            double fromY = tracked.posY;
            double fromZ = tracked.posZ;

            tracked.setPositionAndUpdate(dest.x, dest.y, dest.z);
            tracked.fallDistance = 0.0f;

            this.spawnTeleportParticles(fromX, fromY + tracked.height * 0.5, fromZ);
            this.spawnTeleportParticles(dest.x, dest.y + tracked.height * 0.5, dest.z);

            this.world.playSound(null, dest.x, dest.y, dest.z,
                    SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 2.0f, 1.0f);
        }

        if (this.ticksExisted > this.duration) {
            this.trackedTargets.clear();
            this.setDead();
        }
    }

    private Vec3d mirrorAcrossCentre(EntityLivingBase tracked) {

        double dx = this.posX - tracked.posX;
        double dz = this.posZ - tracked.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0E-4) return null;

        Vec3d dest = new Vec3d(
                this.posX + dx / dist * this.radius * TELEPORT_MARGIN,
                tracked.posY,
                this.posZ + dz / dist * this.radius * TELEPORT_MARGIN);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < MAX_RISE; i++) {
            pos.setPos((int) Math.floor(dest.x), (int) Math.floor(dest.y), (int) Math.floor(dest.z));
            if (this.world.isAirBlock(pos)) {
                return dest;
            }
            dest = dest.add(0, 1, 0);
        }
        return null;
    }

    private void spawnTeleportParticles(double x, double y, double z) {
        this.spawnTeleportParticles(x, y, z, 16, 1.2, 0.4);
    }

    private void spawnTeleportParticles(double x, double y, double z, int count, double spread, double speed) {
        for (int i = 0; i < count; i++) {
            this.world.spawnParticle(EnumParticleTypes.PORTAL,
                    x + (this.rand.nextDouble() - 0.5) * spread,
                    y + (this.rand.nextDouble() - 0.5) * spread,
                    z + (this.rand.nextDouble() - 0.5) * spread,
                    (this.rand.nextDouble() - 0.5) * speed,
                    (this.rand.nextDouble() - 0.5) * speed,
                    (this.rand.nextDouble() - 0.5) * speed);
        }
    }

    private void spawnRingParticles() {
        if (this.ticksExisted % RING_INTERVAL != 0) return;

        for (int i = 0; i < RING_COUNT; i++) {
            double angle = 2.0 * Math.PI * i / RING_COUNT;
            for (int layer = 0; layer < RADIAL_LAYERS; layer++) {
                double layerRadius = this.radius * (1.0 - RADIAL_GAP * layer);
                double px = this.posX + Math.cos(angle) * layerRadius;
                double pz = this.posZ + Math.sin(angle) * layerRadius;
                for (double height : RING_HEIGHTS) {
                    this.spawnTeleportParticles(px, this.posY + height, pz, 1, PILLAR_SPREAD, 0.02);
                }
            }
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
        this.radius = compound.getFloat("Radius");
        this.duration = compound.getInteger("Duration");
        this.ticksExisted = compound.getInteger("Age");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("OwnerId", this.ownerId);
        compound.setFloat("Radius", this.radius);
        compound.setInteger("Duration", this.duration);
        compound.setInteger("Age", this.ticksExisted);
    }
}
