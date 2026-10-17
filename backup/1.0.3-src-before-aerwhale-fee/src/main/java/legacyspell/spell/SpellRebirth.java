package legacyspell.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class SpellRebirth extends Spell {

    public SpellRebirth() {
        super(Reference.MOD_ID, "rebirth", EnumAction.BOW, false);
        addProperties(Spell.RANGE);
        soundValues(1.0f, 0.7f, 0.0f);
    }

    @Override
    public boolean cast(World world, EntityPlayer caster, EnumHand hand, int modifiers, SpellModifiers spellModifiers) {
        return doCast(world, caster, spellModifiers);
    }

    @Override
    public boolean cast(World world, EntityLiving caster, EnumHand hand, int modifiers, EntityLivingBase target, SpellModifiers spellModifiers) {
        return doCast(world, caster, spellModifiers);
    }

    private boolean doCast(World world, EntityLivingBase caster, SpellModifiers modifiers) {
        if (world.isRemote) return true;

        double radius = this.getProperty(Spell.RANGE).doubleValue() * modifiers.get(WizardryItems.range_upgrade);
        if (radius <= 0.0) return true;

        double radiusSqr = radius * radius;
        AxisAlignedBB box = caster.getEntityBoundingBox().grow(radius);

        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(caster, box)) {

            if (!(entity instanceof EntityLivingBase)) continue;

            EntityLivingBase victim = (EntityLivingBase) entity;
            if (victim == caster || !victim.isEntityAlive()) continue;
            if (caster.isOnSameTeam(victim)) continue;
            if (caster.getDistanceSq(victim) > radiusSqr) continue;

            SpellEfface.annihilate(world, caster, victim);
        }

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
    }
}
