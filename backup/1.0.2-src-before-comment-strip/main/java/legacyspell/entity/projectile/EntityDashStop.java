package legacyspell.entity.projectile;

import legacyspell.util.MotionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Invisible helper spawned by 飞刃袭 (Blade Dash). It rides along with the caster's high-speed dash and
 * steers their velocity toward the destination every tick so the traversal is one continuous, smooth
 * movement that lands exactly on the aimed spot.
 *
 * <p>An earlier build yanked the caster onto the destination with {@code setPositionAndUpdate} once
 * they got within a block and a half — that hard position set is an instantaneous jump (it also kills
 * render interpolation), which read as a teleport-like snap on the final frame. This version never
 * force-sets a position: it only ever writes velocity, so the whole dash is movement the client
 * interpolates, and the caster eases to a halt instead of popping onto the endpoint.</p>
 */
public class EntityDashStop extends Entity {

    /** Hard cap on the steering speed, in blocks/tick. */
    private static final double MAX_SPEED = 5.0;
    /** Below this remaining gap the caster is considered arrived and is brought fully to rest. */
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
            // Dash window over: bleed the motion off. No position is ever force-set here, so there is
            // no teleport-like snap — the caster simply glides to a halt where the velocity left them.
            MotionHelper.setMotion(this.target, 0.0, 0.0, 0.0);
            this.target.fallDistance = 0.0f;
            this.setDead();
            return;
        }

        // Steer toward the destination: each tick spend the remaining gap across the remaining time.
        // Re-issuing the velocity every tick keeps air drag from stranding the caster short, and the
        // per-tick displacement stays even (a straight, constant-speed glide) rather than dumping the
        // whole leftover distance into one final frame.
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
