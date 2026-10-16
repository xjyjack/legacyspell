package legacyspell.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class EntitySlashEffect extends Entity {

    private float yRot;
    private float randomRotOffset;
    private boolean iceMode;

    public EntitySlashEffect(World world) {
        super(world);
        this.noClip = true;
        this.setSize(0.1f, 0.1f);
    }

    public EntitySlashEffect(World world, double x, double y, double z, float yRot, boolean iceMode) {
        this(world);
        this.yRot = yRot;
        this.iceMode = iceMode;
        this.randomRotOffset = (float) (Math.random() * 60.0 - 30.0);
        this.setPosition(x, y, z);
    }

    public float getSlashYRot() {
        return this.yRot;
    }

    public float getRandomRotOffset() {
        return this.randomRotOffset;
    }

    public boolean isIceMode() {
        return this.iceMode;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote && this.ticksExisted >= 10) {
            this.setDead();
        }
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
        this.yRot = compound.getFloat("YRot");
        this.randomRotOffset = compound.getFloat("RotOffset");
        this.iceMode = compound.getBoolean("Ice");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setFloat("YRot", this.yRot);
        compound.setFloat("RotOffset", this.randomRotOffset);
        compound.setBoolean("Ice", this.iceMode);
    }
}
