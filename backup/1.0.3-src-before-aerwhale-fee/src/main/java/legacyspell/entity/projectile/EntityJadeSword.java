package legacyspell.entity.projectile;

import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;

public class EntityJadeSword extends EntityMagicProjectile {

    private static final double HOMING_SPEED = 2.0;
    private static final int MAX_PIERCE = 6;
    private static final double AIM_RADIUS = 48.0;
    private static final double STRIKE_RADIUS = 1.6;
    private static final double Y_AIM_SLOPE = 0.6;
    private static final double Y_AIM_FLOOR = 0.5;

    private int hitPerTicks;
    private int waitTimer = -1;
    private int rememberedTargetId = -1;
    private final List<Entity> hitThisTick = new ArrayList<>();
    private boolean pendingDiscard;

    private float swordDamage = 6.0f;

    public float lastYaw;
    public float lastPitch;

    public EntityJadeSword(World world) {
        super(world);
        this.setSize(0.55f, 0.55f);
    }

    public EntityJadeSword(World world, EntityLivingBase caster) {
        this(world);
        this.setCaster(caster);
    }

    public void setWaitTimer(int timer) {
        this.waitTimer = timer;
    }

    public void setSwordDamage(float damage) {
        this.swordDamage = damage;
    }

    public float getSwordDamage() {
        return this.swordDamage;
    }

    public void updateOrientation(double mx, double my, double mz) {
        double speed = Math.sqrt(mx * mx + my * my + mz * mz);
        if (speed < 1.0E-3) return;
        double horiz = Math.sqrt(mx * mx + mz * mz);
        this.lastYaw = (float) Math.toDegrees(Math.atan2(mx, mz));
        this.lastPitch = (float) Math.toDegrees(Math.atan2(my, Math.max(horiz, 1.0E-6)));
    }

    @Override
    public int getLifetime() {
        return 240;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    private EntityLivingBase owner() {
        return this.getThrower() instanceof EntityLivingBase ? (EntityLivingBase) this.getThrower() : null;
    }

    public void setRememberedTarget(EntityLivingBase target) {
        this.rememberedTargetId = target == null ? -1 : target.getEntityId();
    }

    private EntityLivingBase resolveTarget(EntityLivingBase exclude) {
        EntityLivingBase owner = this.owner();

        if (owner != null) {
            EntityLivingBase last = owner.getLastAttackedEntity();
            if (isValidTarget(last, exclude)) {
                return last;
            }
        }

        if (this.rememberedTargetId != -1 && this.world != null) {
            Entity remembered = this.world.getEntityByID(this.rememberedTargetId);
            if (isValidTarget(remembered, exclude)) {
                return (EntityLivingBase) remembered;
            }
        }

        if (owner != null) {
            return nearestHostile(owner, exclude);
        }
        return null;
    }

    private boolean isValidTarget(Entity entity, Entity exclude) {
        if (!(entity instanceof EntityLivingBase) || entity == exclude) return false;
        EntityLivingBase living = (EntityLivingBase) entity;
        if (!living.isEntityAlive()) return false;
        Entity thrower = this.getThrower();
        return thrower == null || !thrower.isOnSameTeam(living);
    }

    private static EntityLivingBase nearestHostile(EntityLivingBase caster, Entity exclude) {
        double limitSqr = AIM_RADIUS * AIM_RADIUS;
        EntityLivingBase best = null;
        double bestDistSqr = Double.MAX_VALUE;

        AxisAlignedBB eb = caster.getEntityBoundingBox();
        AxisAlignedBB area = eb.grow(AIM_RADIUS);

        for (Entity e : caster.world.getEntitiesWithinAABBExcludingEntity(caster, area)) {
            if (e == exclude || !(e instanceof EntityLivingBase) || !e.isEntityAlive()) continue;
            if (caster.isOnSameTeam(e)) continue;
            double distSqr = caster.getDistanceSq(e);
            if (distSqr <= limitSqr && distSqr < bestDistSqr) {
                bestDistSqr = distSqr;
                best = (EntityLivingBase) e;
            }
        }
        return best;
    }

    @Override
    public void onUpdate() {

        this.hitPerTicks = 0;
        this.hitThisTick.clear();

        super.onUpdate();

        if (this.isDead) return;

        if (this.waitTimer > 0) {
            --this.waitTimer;

            if (this.waitTimer <= 110) {
                this.motionX = 0.0;
                this.motionY = 0.0;
                this.motionZ = 0.0;

                if (this.waitTimer <= 100 && this.waitTimer > 10) {
                    EntityLivingBase target = this.resolveTarget(null);
                    if (target != null) {
                        Vec3d attack = target.getEntityBoundingBox().getCenter().subtract(this.getPositionVector());
                        double horiz = Math.sqrt(attack.x * attack.x + attack.z * attack.z);
                        double limit = Math.max(horiz * Y_AIM_SLOPE, Y_AIM_FLOOR);
                        double aimY = attack.y;
                        if (aimY > limit) aimY = limit;
                        if (aimY < -limit) aimY = -limit;
                        attack = new Vec3d(attack.x, aimY, attack.z);
                        double length = attack.length();
                        if (length <= STRIKE_RADIUS) {
                            this.strike(target);
                        }
                        if (length > 1.0E-4) {
                            attack = attack.scale(HOMING_SPEED / length);
                            this.motionX = attack.x;
                            this.motionY = attack.y;
                            this.motionZ = attack.z;
                            this.velocityChanged = true;
                        }
                    }
                } else if (this.waitTimer == 5) {
                    this.doBreaking();
                } else if (this.waitTimer == 0) {
                    this.setDead();
                }
            }
        }

        if (this.world.isRemote) this.spawnTrailParticles();
    }

    @Override
    protected void onImpact(RayTraceResult result) {

        if (result == null || this.isDead) return;

        if (result.entityHit != null) {

            if (!this.world.isRemote) {
                this.hitEntity(result.entityHit);
                while (this.hitPerTicks < MAX_PIERCE) {
                    Entity next = this.findNextTarget();
                    if (next == null) break;
                    this.hitEntity(next);
                }
                if (this.pendingDiscard) this.setDead();
            }
            return;
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            if (this.world instanceof WorldServer) {
                ((WorldServer) this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC,
                        result.hitVec.x, result.hitVec.y, result.hitVec.z, 2, 0.1, 0.1, 0.1, 0.03);
            }
        }
    }

    private boolean canHitEntity(Entity entity) {
        if (entity == this || entity == this.getThrower()) return false;
        if (!(entity instanceof EntityLivingBase)) return false;
        EntityLivingBase living = (EntityLivingBase) entity;
        if (!living.isEntityAlive() || !living.canBeCollidedWith()) return false;
        if (this.hitThisTick.contains(entity)) return false;
        Entity thrower = this.getThrower();
        return thrower == null || !thrower.isOnSameTeam(living);
    }

    private Entity findNextTarget() {
        Vec3d start = this.getPositionVector();
        Vec3d end = start.add(this.motionX, this.motionY, this.motionZ);
        AxisAlignedBB search = this.getEntityBoundingBox()
                .expand(this.motionX, this.motionY, this.motionZ).grow(1.0);

        Entity best = null;
        double bestDistance = Double.MAX_VALUE;

        for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, search)) {
            if (!this.canHitEntity(entity)) continue;
            RayTraceResult hit = entity.getEntityBoundingBox().grow(0.1).calculateIntercept(start, end);
            if (hit == null) continue;
            double distance = hit.hitVec.squareDistanceTo(start);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }

        return best;
    }

    private void strike(EntityLivingBase target) {
        if (this.world.isRemote) return;
        if (this.hitThisTick.contains(target)) return;

        this.hitEntity(target);

        while (this.hitPerTicks < MAX_PIERCE) {
            Entity next = this.findNextTarget();
            if (next == null) break;
            this.hitEntity(next);
        }
        if (this.pendingDiscard) this.setDead();
    }

    private void hitEntity(Entity target) {

        this.hitThisTick.add(target);
        ++this.hitPerTicks;

        Entity thrower = this.getThrower();
        DamageSource source = thrower == null
                ? new DamageSource("generic")
                : new EntityDamageSource("generic", thrower);
        source.setDamageBypassesArmor();
        target.attackEntityFrom(source, this.swordDamage);

        this.world.playSound(null, this.posX, this.posY, this.posZ,
                SoundEvents.BLOCK_GLASS_HIT, SoundCategory.NEUTRAL, 0.5f, 1.4f);

        if (this.ticksExisted > 10) {
            if (target instanceof EntityLivingBase) {
                ((EntityLivingBase) target).hurtResistantTime = 0;
            }
            this.pendingDiscard = true;
        }
    }

    private void doBreaking() {
        if (this.world instanceof WorldServer) {
            WorldServer server = (WorldServer) this.world;
            server.spawnParticle(EnumParticleTypes.CRIT,
                    this.posX, this.posY, this.posZ, 8, 0.4, 0.4, 0.4, 0.15);
            server.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
                    this.posX, this.posY, this.posZ, 3, 0.3, 0.3, 0.3, 0.1);
        }
        this.world.playSound(null, this.posX, this.posY, this.posZ,
                SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.NEUTRAL, 1.6f, 0.5f);
    }

    private void spawnTrailParticles() {
        Vec3d vec = this.getPositionVector().subtract(this.motionX, this.motionY, this.motionZ);
        this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, vec.x, vec.y, vec.z, 0, 0, 0);
        this.world.spawnParticle(EnumParticleTypes.SPELL, vec.x, vec.y, vec.z, 0, 0, 0);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.waitTimer = compound.getInteger("WaitTimer");
        this.swordDamage = compound.getFloat("SwordDamage");
        this.rememberedTargetId = compound.getInteger("RememberedTarget");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("WaitTimer", this.waitTimer);
        compound.setFloat("SwordDamage", this.swordDamage);
        compound.setInteger("RememberedTarget", this.rememberedTargetId);
    }
}
