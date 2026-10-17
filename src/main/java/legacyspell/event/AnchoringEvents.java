package legacyspell.event;

import legacyspell.Reference;
import legacyspell.registry.LegacyPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class AnchoringEvents {

    @SubscribeEvent
    public static void onEnderTeleport(EnderTeleportEvent event) {

        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || !entity.isPotionActive(LegacyPotions.anchored)) return;

        event.setCanceled(true);

        if (!entity.world.isRemote) {
            for (int i = 0; i < 12; i++) {
                entity.world.spawnParticle(EnumParticleTypes.PORTAL,
                        entity.posX + (entity.getRNG().nextDouble() - 0.5) * entity.width * 2,
                        entity.posY + entity.getRNG().nextDouble() * entity.height,
                        entity.posZ + (entity.getRNG().nextDouble() - 0.5) * entity.width * 2,
                        (entity.getRNG().nextDouble() - 0.5) * 0.3,
                        (entity.getRNG().nextDouble() - 0.5) * 0.3,
                        (entity.getRNG().nextDouble() - 0.5) * 0.3);
            }
            entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
                    SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.NEUTRAL, 1.0f, 1.2f);
        }
    }
}
