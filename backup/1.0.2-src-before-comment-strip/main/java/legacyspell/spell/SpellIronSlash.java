package legacyspell.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityDashStop;
import legacyspell.entity.projectile.EntitySlashEffect;
import legacyspell.util.MotionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 飞刃袭 (Iron Slash, wind_spellbooks) — an ultra-high-speed forward dash that shreds everything in its path.
 *
 * <p>Ported from {@code IronSlashSpell.performDash}: instead of teleporting, the caster is given a burst of
 * velocity that carries them the full distance over a handful of ticks, while an invisible
 * {@link EntityDashStop} guarantees they come to rest exactly on the aimed spot. Damage is applied to
 * every enemy swept by the path, plus a slash visual per victim.</p>
 */
public class SpellIronSlash extends Spell {

    /** Ticks the dash is spread across (matches wind_spellbooks' 4-tick dash). */
    private static final int DASH_TICKS = 4;
    /** Hard cap on the dash speed, in blocks/tick. */
    private static final double MAX_DASH_SPEED = 5.0;

    public SpellIronSlash() {
        super(Reference.MOD_ID, "iron_slash", EnumAction.BOW, false);
        addProperties(Spell.DAMAGE, Spell.RANGE);
        soundValues(0.8f, 1.0f, 0.2f);
    }

    @Override
    public boolean cast(World world, EntityPlayer caster, EnumHand hand, int modifiers, SpellModifiers spellModifiers) {
        return doCast(world, caster, spellModifiers);
    }

    @Override
    public boolean cast(World world, net.minecraft.entity.EntityLiving caster, EnumHand hand, int modifiers, EntityLivingBase target, SpellModifiers spellModifiers) {
        return doCast(world, caster, spellModifiers);
    }

    private boolean doCast(World world, EntityLivingBase caster, SpellModifiers modifiers) {
        if (world.isRemote) return true;

        float range = this.getProperty(Spell.RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);
        Vec3d eye = caster.getPositionEyes(1.0f);
        Vec3d look = caster.getLookVec().normalize();
        Vec3d end = new Vec3d(eye.x + look.x * range, eye.y + look.y * range, eye.z + look.z * range);

        RayTraceResult blockHit = RayTracer.standardBlockRayTrace(world, caster, range, false, false, false);
        Vec3d aim;
        if (blockHit != null && blockHit.typeOfHit == RayTraceResult.Type.BLOCK) {
            Vec3d hv = blockHit.hitVec;
            aim = new Vec3d(hv.x - look.x * 1.2, hv.y - look.y * 1.2, hv.z - look.z * 1.2);
        } else {
            aim = end;
        }

        // The ray is cast from the eyes; the caster's position is their feet. Drop the endpoint to foot
        // level and lift it out of any solid block so the dash cannot end embedded in terrain.
        Vec3d dest = new Vec3d(aim.x, aim.y - caster.getEyeHeight(), aim.z);
        dest = raiseToAir(world, dest);

        // --- damage every enemy swept by the path (independent of where the dash finally stops) ------
        float damage = this.getProperty(Spell.DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
        if (caster instanceof EntityPlayer) {
            damage += (float) caster.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        }

        boolean iceMode = isCold(world, caster);

        Vec3d start = eye;
        Vec3d dir = dest.subtract(start);
        double length = dir.length();
        if (length > 0.01) {
            Vec3d dirNorm = dir.normalize();
            double radius = Math.max(1.5, length / 2.0 + 1.5);
            Vec3d center = new Vec3d(start.x + dir.x * 0.5, start.y + dir.y * 0.5, start.z + dir.z * 0.5);
            AxisAlignedBB aabb = new AxisAlignedBB(
                    center.x - radius - 2.0, center.y - radius - 2.0, center.z - radius - 2.0,
                    center.x + radius + 2.0, center.y + radius + 2.0, center.z + radius + 2.0);
            List<Entity> candidates = world.getEntitiesWithinAABBExcludingEntity(caster, aabb);
            for (Entity e : candidates) {
                if (!(e instanceof EntityLivingBase)) continue;
                EntityLivingBase target = (EntityLivingBase) e;
                if (!target.isEntityAlive() || target.isDead) continue;
                if (caster.isOnSameTeam(target)) continue;
                Vec3d toEntity = target.getPositionVector().subtract(start);
                double projection = toEntity.dotProduct(dirNorm);
                if (projection < -2.0 || projection > length + 2.0) continue;
                Vec3d projected = dirNorm.scale(projection);
                double perpDist = toEntity.subtract(projected).length();
                double halfWidth = 1.5 + (double) target.width / 2.0;
                if (perpDist > halfWidth) continue;

                target.attackEntityFrom(MagicDamage.causeIndirectMagicDamage(caster, caster, DamageType.FORCE), damage);
                target.motionY += 0.3;
                target.velocityChanged = true;
                target.hurtResistantTime = 0;
                world.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, target.getSoundCategory(), 1.0f, 0.8f + (float) (Math.random() * 0.4f));

                if (iceMode) {
                    target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
                }

                EntitySlashEffect slash = new EntitySlashEffect(world, target.posX, target.posY + target.height / 2.0, target.posZ, caster.rotationYaw, iceMode);
                world.spawnEntity(slash);
            }
        }

        // --- the dash itself: high-speed motion + a stop entity pinned to the destination ------------
        performDash(world, caster, dest);

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
    }

    private void performDash(World world, EntityLivingBase caster, Vec3d dest) {
        Vec3d dashDir = dest.subtract(caster.getPositionVector());
        double dashDistance = dashDir.length();

        // The dash is pure velocity from here on: an initial burst plus an EntityDashStop that keeps
        // steering the motion toward the destination and finally eases it to a stop. Nothing ever
        // force-sets the caster's position, so there is no teleport-like snap anywhere in the move.
        if (dashDistance > 0.1) {
            Vec3d velocity = dashDir.scale(1.0 / DASH_TICKS);
            if (velocity.length() > MAX_DASH_SPEED) {
                velocity = velocity.normalize().scale(MAX_DASH_SPEED);
            }
            MotionHelper.setMotion(caster, velocity);
            caster.fallDistance = 0.0f;
            world.spawnEntity(new EntityDashStop(world, caster, dest, DASH_TICKS + 2));
        }
    }

    private static Vec3d raiseToAir(World world, Vec3d point) {
        BlockPos pos = new BlockPos(point.x, point.y, point.z);
        for (int i = 0; i < 32; i++) {
            if (world.isAirBlock(pos) && world.isAirBlock(pos.up())) {
                return new Vec3d(point.x, pos.getY(), point.z);
            }
            pos = pos.up();
        }
        return point;
    }

    private static boolean isCold(World world, EntityLivingBase caster) {
        try {
            net.minecraft.world.biome.Biome biome = world.getBiome(caster.getPosition());
            return biome.getTemperature(caster.getPosition()) < 0.2f;
        } catch (Throwable t) {
            return false;
        }
    }
}
