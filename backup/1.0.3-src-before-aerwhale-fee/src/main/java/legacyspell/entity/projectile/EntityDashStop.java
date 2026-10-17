package legacyspell.entity.projectile;

import legacyspell.util.MotionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityDashStop extends Entity {

    private static final double MAX_SPEED = 5.0;
    private static final double ARRIVE_GAP = 0.05;

    private EntityLivingBase target;
    private double destX;
    private double destY;
    private double destZ;
    private int lifetime;

    public EntityDashStop(World world) {
        super(world);
        this.noClip = true;
        this.setSize(0.1f, 0.1f);
    }

    public EntityDashStop(World world, EntityLivingBase target, Vec3d dest, int lifetime) {
        this(world);
        this.target = target;
        this.destX = dest.x;
        this.destY = dest.y;
        this.destZ = dest.z;
        this.lifetime = lifetime;
        this.setPosition(dest.x, dest.y, dest.z);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) return;

        if (this.target == null || !this.target.isEntityAlive()) {
            this.setDead();
            return;
        }

        int remaining = this.lifetime - this.ticksExisted;
        if (remaining <= 0) {
            MotionHelper.setMotion(this.target, 0.0, 0.0, 0.0);
            this.target.fallDistance = 0.0f;
            this.setDead();
            return;
        }

        double dx = this.destX - this.target.posX;
        double dy = this.destY - this.target.posY;
        double dz = this.destZ - this.target.posZ;
        double gap = Math.sqrt(dx * dx + dy * dy + dz * dz);

        Vec3d velocity;
        if (gap < ARRIVE_GAP) {
            velocity = new Vec3d(0.0, 0.0, 0.0);
        } else {
            velocity = new Vec3d(dx / remaining, dy / remaining, dz / remaining);
            if (velocity.length() > MAX_SPEED) {
                velocity = velocity.normalize().scale(MAX_SPEED);
            }
        }
        MotionHelper.setMotion(this.target, velocity);
        this.target.fallDistance = 0.0f;
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
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }
}
