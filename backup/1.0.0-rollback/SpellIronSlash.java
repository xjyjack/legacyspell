package legacyspell.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntitySlashEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class SpellIronSlash extends Spell {

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
        Vec3d end = eye.add(look.scale(range));

        RayTraceResult blockHit = RayTracer.standardBlockRayTrace(world, caster, range, false, false, false);
        Vec3d dest;
        if (blockHit != null && blockHit.typeOfHit == RayTraceResult.Type.BLOCK) {
            dest = blockHit.hitVec.subtract(look.scale(1.2));
        } else {
            dest = end;
        }

        caster.setPositionAndUpdate(dest.x, dest.y, dest.z);

        float damage = this.getProperty(Spell.DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
        damage += (float) caster.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();

        boolean iceMode = isCold(world, caster);

        Vec3d start = eye;
        Vec3d dir = dest.subtract(start);
        double length = dir.length();
        if (length > 0.01) {
            Vec3d dirNorm = dir.normalize();
            double radius = Math.max(1.5, length / 2.0 + 1.5);
            Vec3d center = start.add(dir.scale(0.5));
            AxisAlignedBB aabb = new AxisAlignedBB(center, center).grow(radius + 2.0, radius + 2.0, radius + 2.0);
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

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
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
