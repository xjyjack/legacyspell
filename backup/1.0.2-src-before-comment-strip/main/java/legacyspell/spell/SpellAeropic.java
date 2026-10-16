package legacyspell.spell;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.registry.LegacyPotions;
import legacyspell.util.MotionHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 航天腾跃 (Aeropic, wind_spellbooks) — a spinning, self-propelled lunge.
 *
 * <p>Casting hurls the caster along their line of sight with a fierce impulse and applies the
 * {@code acrobatics} effect, which keeps driving the lunge. The first fresh target the caster smashes
 * into takes the spell damage (plus a falloff burst on nearby enemies), the caster is punched straight
 * up into the air, and — as long as they keep connecting — the lunge continues, chaining through up to
 * {@code max_hits} targets per cast. Ported from Iron's Spells' Burning Dash movement model
 * ({@code forward * 3.0 * multiplier}, lerp onto the current motion, i-frames on cast).</p>
 */
public class SpellAeropic extends Spell {

    public SpellAeropic() {
        super(Reference.MOD_ID, "aeropic", EnumAction.NONE, false);
        addProperties(Spell.DAMAGE, "dash_power", "max_hits");
        soundValues(0.8f, 1.0f, 0.2f);
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

        float damage = this.getProperty(Spell.DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
        float dashPower = this.getProperty("dash_power").floatValue() * modifiers.get(SpellModifiers.POTENCY);
        int maxHits = Math.max(1, this.getProperty("max_hits").intValue());

        // Fierce lunge: full horizontal push, a softer vertical share (so looking level still carries
        // you up), plus a hop off the ground. The previous build only let a quarter of this through.
        Vec3d forward = caster.getLookVec().normalize();
        double power = 3.0 * dashPower;
        Vec3d impulse = new Vec3d(forward.x * power, forward.y * power * 0.8, forward.z * power);
        if (caster.onGround) {
            impulse = new Vec3d(impulse.x, impulse.y + 0.4, impulse.z);
        }

        double lerp = 0.55;
        MotionHelper.setMotion(caster,
                lerp * caster.motionX + (1.0 - lerp) * impulse.x,
                lerp * caster.motionY + (1.0 - lerp) * impulse.y,
                lerp * caster.motionZ + (1.0 - lerp) * impulse.z);

        caster.hurtResistantTime = 20;
        caster.fallDistance = 0.0f;
        AeropicChain.arm(caster, maxHits);
        caster.addPotionEffect(new PotionEffect(LegacyPotions.acrobatics, 20, (int) damage));

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
    }
}
