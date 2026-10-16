package legacyspell.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class SpellEfface extends Spell {

    public static final float DAMAGE = 59049.0f;

    public SpellEfface() {
        super(Reference.MOD_ID, "efface", EnumAction.BOW, false);
        addProperties(Spell.RANGE);
        soundValues(1.0f, 0.8f, 0.0f);
    }

    @Override
    public boolean cast(World world, EntityPlayer caster, EnumHand hand, int modifiers, SpellModifiers spellModifiers) {
        return doCast(world, caster, spellModifiers);
    }

    @Override
    public boolean cast(World world, EntityLiving caster, EnumHand hand, int modifiers, EntityLivingBase target, SpellModifiers spellModifiers) {
        if (world.isRemote) return true;

        if (target != null && target != caster && !caster.isOnSameTeam(target)) {
            annihilate(world, caster, target);
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
                annihilate(world, caster, target);
            }
        }

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
    }

    private void annihilate(World world, EntityLivingBase caster, EntityLivingBase target) {

        target.attackEntityFrom(sourceFromCaster(caster), DAMAGE);
        if (isGone(target)) { dissolve(world, target); return; }

        target.attackEntityFrom(DamageSource.OUT_OF_WORLD, DAMAGE);
        if (isGone(target)) { dissolve(world, target); return; }

        target.setHealth(0.0f);
        target.setDead();
        target.isDead = true;
        dissolve(world, target);
        world.removeEntity(target);
    }

    static boolean isGone(EntityLivingBase target) {
        return target.isDead || target.getHealth() <= 0.0f;
    }

    static DamageSource sourceFromCaster(EntityLivingBase caster) {
        if (caster instanceof EntityPlayer) {
            return DamageSource.causePlayerDamage((EntityPlayer) caster);
        }
        return DamageSource.causeMobDamage(caster);
    }

    static void dissolve(World world, EntityLivingBase target) {
        if (world.isRemote || !(world instanceof WorldServer)) return;

        WorldServer server = (WorldServer) world;
        double x = target.posX;
        double y = target.posY + target.height / 2.0;
        double z = target.posZ;

        for (int i = 0; i < 12; i++) {
            server.spawnParticle(EnumParticleTypes.PORTAL, false,
                    x + (server.rand.nextDouble() - 0.5) * 1.6,
                    y + (server.rand.nextDouble() - 0.5) * target.height,
                    z + (server.rand.nextDouble() - 0.5) * 1.6,
                    3,
                    (server.rand.nextDouble() - 0.5) * 0.4,
                    (server.rand.nextDouble() - 0.5) * 0.4,
                    (server.rand.nextDouble() - 0.5) * 0.4,
                    0.5D);
        }
        for (int i = 0; i < 6; i++) {
            server.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, false,
                    x + (server.rand.nextDouble() - 0.5) * 1.2,
                    y + (server.rand.nextDouble() - 0.5) * target.height,
                    z + (server.rand.nextDouble() - 0.5) * 1.2,
                    1,
                    (server.rand.nextDouble() - 0.5) * 0.4,
                    (server.rand.nextDouble() - 0.5) * 0.4,
                    (server.rand.nextDouble() - 0.5) * 0.4,
                    0.2D);
        }
    }
}
