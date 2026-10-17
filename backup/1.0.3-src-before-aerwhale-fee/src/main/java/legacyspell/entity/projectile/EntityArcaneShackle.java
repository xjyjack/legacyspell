package legacyspell.entity.projectile;

import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityArcaneShackle extends EntityMagicProjectile {

    private static final int CHAIN_COUNT = 3;
    private static final int PROJECTILE_LIFETIME = 60;

    private float chainHealth = 15.0f;
    private int chainLifetime = 200;
    private double lashRadius = 5.0;
    private float restraintStrength = 0.015f;

    public EntityArcaneShackle(World world) {
        super(world);
        this.setSize(0.6f, 0.6f);
    }

    public void setChainHealth(float chainHealth) {
        this.chainHealth = chainHealth;
    }

    public void setChainLifetime(int chainLifetime) {
        this.chainLifetime = chainLifetime;
    }

    public void setLashRadius(double lashRadius) {
        this.lashRadius = lashRadius;
    }

    public void setRestraintStrength(float restraintStrength) {
        this.restraintStrength = restraintStrength;
    }

    @Override
    public int getLifetime() {
        return PROJECTILE_LIFETIME;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.world.isRemote) {
            for (int i = 0; i < 2; i++) {
                this.world.spawnParticle(EnumParticleTypes.PORTAL,
                        this.posX + (this.rand.nextDouble() - 0.5) * 0.6,
                        this.posY + (this.rand.nextDouble() - 0.5) * 0.6,
                        this.posZ + (this.rand.nextDouble() - 0.5) * 0.6,
                        (this.rand.nextDouble() - 0.5) * 0.2,
                        (this.rand.nextDouble() - 0.5) * 0.2,
                        (this.rand.nextDouble() - 0.5) * 0.2);
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {

        if (result == null || this.world.isRemote) return;

        if (result.entityHit instanceof EntityLivingBase) {
            this.spawnChainsOnEntity((EntityLivingBase) result.entityHit);
            this.setDead();
            return;
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK && result.hitVec != null) {
            this.spawnChainsFromBlock(result.hitVec);
            this.setDead();
        }
    }

    private void spawnChainsOnEntity(EntityLivingBase victim) {

        Vec3d origin = victim.getEntityBoundingBox().getCenter();
        double radius = this.lashRadius * 0.5 + victim.width * 0.4;

        for (int i = 0; i < CHAIN_COUNT; i++) {
            double angle = (2.0 * Math.PI / CHAIN_COUNT) * i
                    + Math.PI / 4.0
                    - Math.toRadians(this.rotationYaw);

            double x = origin.x + Math.cos(angle) * radius;
            double z = origin.z + Math.sin(angle) * radius;
            double y = findGroundY(this.world, x, origin.y, z) + 1.0;

            this.spawnChain(victim, x, y, z);
        }
    }

    private void spawnChainsFromBlock(Vec3d impactPos) {

        AxisAlignedBB box = new AxisAlignedBB(
                impactPos.x - this.lashRadius, impactPos.y - this.lashRadius, impactPos.z - this.lashRadius,
                impactPos.x + this.lashRadius, impactPos.y + this.lashRadius, impactPos.z + this.lashRadius);

        List<EntityLivingBase> found = new ArrayList<>();
        for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, box)) {
            if (entity instanceof EntityLivingBase && entity != this.getThrower()) {
                found.add((EntityLivingBase) entity);
            }
        }

        found.sort(Comparator.comparingDouble(
                e -> e.getDistanceSq(impactPos.x, impactPos.y, impactPos.z)));

        int count = Math.min(CHAIN_COUNT, found.size());
        for (int i = 0; i < count; i++) {
            this.spawnChain(found.get(i), impactPos.x, impactPos.y, impactPos.z);
        }
    }

    private void spawnChain(EntityLivingBase victim, double x, double y, double z) {

        Entity owner = this.getThrower();

        EntityEnderChain chain = new EntityEnderChain(this.world,
                owner instanceof EntityLivingBase ? (EntityLivingBase) owner : null,
                victim, x, y, z);

        chain.setChainHealth(this.chainHealth);
        chain.setLifetime(this.chainLifetime);
        chain.setRestraintStrength(this.restraintStrength);

        this.world.spawnEntity(chain);
    }

    private static double findGroundY(World world, double x, double y, double z) {

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int startY = (int) Math.floor(y + 2);

        for (int i = 0; i < 10; i++) {
            int cy = startY - i;
            pos.setPos((int) Math.floor(x), cy, (int) Math.floor(z));
            if (!world.isAirBlock(pos)) return cy + 1;
        }
        return y;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.chainHealth = compound.getFloat("ChainHealth");
        this.chainLifetime = compound.getInteger("ChainLifetime");
        this.lashRadius = compound.getDouble("LashRadius");
        this.restraintStrength = compound.getFloat("Restraint");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setFloat("ChainHealth", this.chainHealth);
        compound.setInteger("ChainLifetime", this.chainLifetime);
        compound.setDouble("LashRadius", this.lashRadius);
        compound.setFloat("Restraint", this.restraintStrength);
    }
}
