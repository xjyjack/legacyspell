package legacyspell.spell;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.registry.LegacyPotions;
import legacyspell.Reference;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpellAeropic extends Spell {

    public SpellAeropic() {
        super(Reference.MOD_ID, "aeropic", EnumAction.NONE, false);
        addProperties(Spell.DAMAGE, "dash_power");
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

        float damage = this.getProperty(Spell.DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
        float dashPower = this.getProperty("dash_power").floatValue() * modifiers.get(SpellModifiers.POTENCY);

        Vec3d forward = caster.getLookVec().normalize();
        Vec3d impulse = new Vec3d(forward.x * 3.0 * dashPower, forward.y * 3.0 * dashPower * 0.8, forward.z * 3.0 * dashPower);
        if (caster.onGround) {
            impulse = new Vec3d(impulse.x, impulse.y + 0.5, impulse.z);
        }

        double lerp = 0.75;
        caster.motionX = lerp * caster.motionX + (1.0 - lerp) * impulse.x;
        caster.motionY = lerp * caster.motionY + (1.0 - lerp) * impulse.y;
        caster.motionZ = lerp * caster.motionZ + (1.0 - lerp) * impulse.z;
        caster.velocityChanged = true;

        caster.hurtResistantTime = 20;
        int amplifier = (int) damage;
        caster.addPotionEffect(new PotionEffect(LegacyPotions.acrobatics, 10, amplifier));

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
    }
}
