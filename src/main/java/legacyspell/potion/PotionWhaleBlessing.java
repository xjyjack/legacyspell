package legacyspell.potion;

import legacyspell.LegacySpell;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class PotionWhaleBlessing extends Potion {

    private static final double MAX_FALL_SPEED = -0.2;

    public PotionWhaleBlessing() {
        super(false, 0x8fd8ff);
        this.setRegistryName(LegacySpell.MODID, "whale_blessing");
        this.setPotionName("potion." + LegacySpell.MODID + ":whale_blessing");
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase living, int amplifier) {
        World world = living.world;
        if (world.isRemote) return;

        living.fallDistance = 0.0f;

        if (living.motionY < MAX_FALL_SPEED) {
            living.motionY = MAX_FALL_SPEED;
            living.velocityChanged = true;
        }
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        return false;
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect) {
        return false;
    }
}
