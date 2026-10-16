package legacyspell.entity.projectile;

import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;

/**
 * 玉剑 —— 剑舞（Sword Dance）的召唤物，行为逐行移植自 ISS: Magic From The East 的
 * {@code JadeSword.java}。
 *
 * <p>四段行为（与原版完全一致）：
 * <ol>
 *   <li>{@code waitTimer 120 → 110}：以 1.0/tick 的速度沿施法时给定的环形方向向外飞出；</li>
 *   <li>{@code waitTimer 110 → 100}：速度归零，原地悬停；</li>
 *   <li>{@code waitTimer 100 → 10}：以 2.0/tick 追踪施法者上一次攻击的实体；</li>
 *   <li>{@code waitTimer == 5}：碎裂特效；{@code waitTimer == 0}：消失。</li>
 * </ol>
 *
 * <p>命中：造成穿甲（无视护甲）的施法者来源伤害；同一 tick 内可沿运动方向连续穿透最多 6 个目标
 * （原版 {@code hitPerTicks++ < 5}）；{@code ticksExisted > 10} 之后命中即清零目标的受伤无敌帧并消失。
 * 撞到方块且该方块与本 tick 之前撞到的不同时立即消失。
 */
public class EntityJadeSword extends EntityMagicProjectile {

    private static final double HOMING_SPEED = 2.0;
    private static final int MAX_PIERCE = 6;
    private static final double AIM_RADIUS = 48.0;
    private static final double STRIKE_RADIUS = 1.6;

    private int hitPerTicks;
    private int waitTimer = -1;
    private int rememberedTargetId = -1;
    private BlockPos lastHitBlock;
    private final List<Entity> hitThisTick = new ArrayList<>();
    private boolean pendingDiscard;

    private float swordDamage = 6.0f;

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

    @Override
    public int getLifetime() {
        return 240;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    /** 施法者（原版的 owner 字段），取 {@link #getThrower()} 以便跨维度/重进游戏后仍然有效。 */
    private EntityLivingBase owner() {
        return this.getThrower() instanceof EntityLivingBase ? (EntityLivingBase) this.getThrower() : null;
    }

    public void setRememberedTarget(EntityLivingBase target) {
        this.rememberedTargetId = target == null ? -1 : target.getEntityId();
    }

    /**
     * 解析本tick 的追踪目标。三级兜底，保证"集体俯冲斩击"永远不会因为一次取值失败而整段失效：
     * ①施法者实时记录的最近攻击对象（ISS 原版语义）；②施法瞬间记住的那个实体（即使它之后已死也不回退）；
     * ③施法者周围最近的敌对实体。
     */
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

        // 原版在 super.tick() 之后把 hitPerTicks 清零；这里放在前面，语义等价（都是"每 tick 重新计数"）。
        this.hitPerTicks = 0;
        this.hitThisTick.clear();

        super.onUpdate();

        if (this.isDead) return;

        if (this.waitTimer > 0) {
            --this.waitTimer;

            if (this.waitTimer <= 110) {
                // 急停：无论处于哪个阶段，速度先归零
                this.motionX = 0.0;
                this.motionY = 0.0;
                this.motionZ = 0.0;

if (this.waitTimer <= 100 && this.waitTimer > 10) {
                    EntityLivingBase target = this.resolveTarget(null);
                    if (target != null) {
                        Vec3d attack = target.getEntityBoundingBox().getCenter().subtract(this.getPositionVector());
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
                // 原版命中后会沿运动方向再取一次命中结果，实现"一 tick 内连穿多个目标"
                while (this.hitPerTicks < MAX_PIERCE) {
                    Entity next = this.findNextTarget();
                    if (next == null) break;
                    this.hitEntity(next);
                }
                if (this.pendingDiscard) this.setDead();
            }
            return;
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK && !this.world.isRemote) {
            BlockPos pos = new BlockPos(result.hitVec);
            if (!pos.equals(this.lastHitBlock)) {
                this.lastHitBlock = pos;
                this.setDead();
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

    /** 沿当前运动方向做一次射线，取最近的可命中实体（原版 {@code getHitResultOnMoveVector}）。 */
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

    /** 追踪阶段主动贴合目标时的斩击判定：不受 tick 射线粗细影响，保证俯冲一定能砍中。 */
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

        // 穿甲：无视护甲的施法者来源伤害
        DamageSource source = MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.MAGIC);
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

    /** 原版 {@code doBreaking()}：碎裂特效 + 物品碎裂音效。 */
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

    /** 原版 {@code trailParticles()}：玉色尾迹。 */
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
