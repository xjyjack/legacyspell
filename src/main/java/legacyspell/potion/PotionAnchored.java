package legacyspell.potion;

import legacyspell.LegacySpell;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class PotionAnchored extends Potion {

    public PotionAnchored() {
        super(true, 0x96141e);
        this.setRegistryName(LegacySpell.MODID, "anchored");
        this.setPotionName("potion." + LegacySpell.MODID + ":anchored");
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase living, int amplifier) {
        World world = living.world;
        if (world.isRemote) return;

        if (living.motionX != 0.0 || living.motionZ != 0.0) {
            living.motionX = 0.0;
            living.motionZ = 0.0;
            living.velocityChanged = true;
        }
        living.fallDistance = 0.0f;
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
