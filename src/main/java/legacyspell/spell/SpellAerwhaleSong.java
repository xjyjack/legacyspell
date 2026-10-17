package legacyspell.spell;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.mob.EntitySpectralAerwhale;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpellAerwhaleSong extends Spell {

    public SpellAerwhaleSong() {
        super(Reference.MOD_ID, "aerwhale_song", EnumAction.BOW, false);
        addProperties(Spell.DAMAGE, Spell.EFFECT_RADIUS);
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

        Vec3d look = caster.getLookVec();
        Vec3d flat = new Vec3d(look.x, 0, look.z);
        Vec3d heading = flat.lengthSquared() < 1.0E-4 ? new Vec3d(0, 0, 1) : flat.normalize();

        Vec3d start = caster.getPositionVector()
                .subtract(heading.scale(EntitySpectralAerwhale.START_BEHIND))
                .add(0, EntitySpectralAerwhale.HEIGHT, 0);

        EntitySpectralAerwhale whale = new EntitySpectralAerwhale(world, caster, heading,
                EntitySpectralAerwhale.getLifetime());
        whale.setPosition(start.x, start.y, start.z);

        float damage = this.getProperty(Spell.DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
        float radius = this.getProperty(Spell.EFFECT_RADIUS).floatValue();
        whale.configure(damage, radius, EntitySpectralAerwhale.SPEED);

        world.spawnEntity(whale);

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
    }
}
