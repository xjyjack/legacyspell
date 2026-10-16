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
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 裂空斩 —— 远距离单体斩击法术。
 *
 * <p>与飞刃袭（{@link SpellIronSlash}）同源，但完全去掉了施法者的位移：
 * 施法者原地不动，只在视线锁定的敌人身上直接生成一道斩击图标，
 * 并造成等同于该敌人最大生命值 6% 的 shock 伤害。属于 lightning 学派。
 */
public class SpellSkyCleave extends Spell {

    /** 斩击伤害占目标最大生命值的比例（6%）。 */
    private static final float MAX_HEALTH_FRACTION = 0.06f;

    public SpellSkyCleave() {
        super(Reference.MOD_ID, "sky_cleave", EnumAction.BOW, false);
        addProperties(Spell.RANGE);
        soundValues(0.8f, 1.0f, 0.2f);
    }

    @Override
    public boolean cast(World world, EntityPlayer caster, EnumHand hand, int modifiers, SpellModifiers spellModifiers) {
        return doCast(world, caster, spellModifiers);
    }

    @Override
    public boolean cast(World world, EntityLiving caster, EnumHand hand, int modifiers, EntityLivingBase target, SpellModifiers spellModifiers) {
        if (world.isRemote) return true;

        if (target != null && target != caster && !caster.isOnSameTeam(target)) {
            strike(world, caster, target);
        } else {
            doCast(world, caster, spellModifiers);
        }
        this.playSound(world, caster, 0, 1, spellModifiers);
        return true;
    }

    private boolean doCast(World world, EntityLivingBase caster, SpellModifiers modifiers) {
        if (world.isRemote) return true;

        double range = this.getProperty(Spell.RANGE).doubleValue() * modifiers.get(WizardryItems.range_upgrade);
        RayTraceResult result = RayTracer.standardEntityRayTrace(world, caster, range, false);

        if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY
                && result.entityHit instanceof EntityLivingBase) {

            EntityLivingBase target = (EntityLivingBase) result.entityHit;
            if (target != caster && !caster.isOnSameTeam(target)) {
                strike(world, caster, target);
            }
        } else {
            // 空挥：在视线落点处给一个斩击特效作为反馈，不造成任何伤害与位移。
            RayTraceResult blockHit = RayTracer.standardBlockRayTrace(world, caster, range, false, false, false);
            if (blockHit != null && blockHit.typeOfHit == RayTraceResult.Type.BLOCK) {
                Vec3d hv = blockHit.hitVec;
                world.spawnEntity(new EntitySlashEffect(world, hv.x, hv.y, hv.z, caster.rotationYaw, false));
            }
        }

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
    }

    /** 在目标身上生成斩击图标并结算伤害。 */
    private void strike(World world, EntityLivingBase caster, EntityLivingBase target) {

        float damage = target.getMaxHealth() * MAX_HEALTH_FRACTION;

        target.attackEntityFrom(
                MagicDamage.causeIndirectMagicDamage(caster, caster, DamageType.SHOCK),
                damage);
        target.hurtResistantTime = 0;

        world.spawnEntity(new EntitySlashEffect(world,
                target.posX, target.posY + target.height / 2.0, target.posZ,
                caster.rotationYaw, false));

        world.playSound(null, target.posX, target.posY, target.posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, target.getSoundCategory(),
                1.0f, 1.2f + (float) (Math.random() * 0.2f));
    }
}
