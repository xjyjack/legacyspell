package legacyspell.spell;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityObsidianRod;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpellObsidianRod extends Spell {

    private static final int SHOOT_INTERVAL = 5;
    private static final int CHANNEL_BASE = 60;
    private static final int CHANNEL_PER_POTENCY = 5;
    private static final double SPAWN_OFFSET = 0.2;

    public SpellObsidianRod() {
        super(Reference.MOD_ID, "obsidian_rod", EnumAction.BOW, true);
        addProperties(Spell.DAMAGE, Spell.EFFECT_DURATION);
        soundValues(0.8f, 1.0f, 0.2f);
    }

    @Override
    public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {

        if (world.isRemote) return true;

        int channel = (int) (CHANNEL_BASE + CHANNEL_PER_POTENCY * modifiers.get(SpellModifiers.POTENCY));
        if (ticksInUse >= channel) {
            caster.stopActiveHand();
            return true;
        }

        if (ticksInUse % SHOOT_INTERVAL == 0) {
            this.shootObsidianRod(world, caster, modifiers);
        }

        return true;
    }

    @Override
    public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers) {

        if (world.isRemote) return true;

        if (ticksInUse % SHOOT_INTERVAL == 0) {
            this.shootObsidianRod(world, caster, modifiers);
        }

        return true;
    }

    private void shootObsidianRod(World world, EntityLivingBase caster, SpellModifiers modifiers) {

        Vec3d look = caster.getLookVec();
        Vec3d origin = caster.getPositionEyes(1.0f).add(look.scale(SPAWN_OFFSET));

        float potency = modifiers.get(SpellModifiers.POTENCY);

        EntityObsidianRod rod = new EntityObsidianRod(world, caster,
                (int) (this.getProperty(Spell.EFFECT_DURATION).floatValue() * potency));
        rod.setPosition(origin.x, origin.y - rod.height + 0.5, origin.z);
        rod.setDamage(this.getProperty(Spell.DAMAGE).floatValue() * potency);
        rod.shootWithInaccuracy(look);

        world.spawnEntity(rod);

        world.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 2.0f, 1.0f);
    }
}
