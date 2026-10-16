package legacyspell.spell;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityJadeSword;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class SpellSwordDance extends Spell {

    private static final int BASE_SWORD_COUNT = 5;
    private static final int MAX_SWORD_COUNT = 16;
    private static final int WAIT_TICKS = 120;

    private static final double TILT_RADIANS = Math.toRadians(1.0);

    public SpellSwordDance() {
        super(Reference.MOD_ID, "sword_dance", EnumAction.BOW, false);
        addProperties(Spell.DAMAGE);
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

        float potency = modifiers.get(SpellModifiers.POTENCY);
        float damage = this.getProperty(Spell.DAMAGE).floatValue() * potency;

        int swordCount = Math.round(BASE_SWORD_COUNT + (potency - 1.0f) * 3.0f);
        swordCount = Math.max(2, Math.min(MAX_SWORD_COUNT, swordCount));

        int offset = 360 / swordCount;
        double centerX = caster.posX;
        double centerY = caster.posY + caster.getEyeHeight() - 0.75;
        double centerZ = caster.posZ;

        double cosTilt = Math.cos(TILT_RADIANS);
        double sinTilt = Math.sin(TILT_RADIANS);

        for (int i = 0; i < swordCount; i++) {

            double theta = Math.toRadians(offset * i);
            double motionX = cosTilt * Math.sin(theta);
            double motionY = -sinTilt;
            double motionZ = cosTilt * Math.cos(theta);

            EntityJadeSword sword = new EntityJadeSword(world, caster);
            sword.setSwordDamage(damage);
            sword.setWaitTimer(WAIT_TICKS);
            sword.setRememberedTarget(caster.getLastAttackedEntity());
            sword.setPosition(centerX, centerY, centerZ);
            sword.motionX = motionX;
            sword.motionY = motionY;
            sword.motionZ = motionZ;
            sword.velocityChanged = true;

            if (i % 3 == 0) sword.setSilent(true);

            world.spawnEntity(sword);
        }

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
    }
}
