package legacyspell.registry;

import legacyspell.Reference;
import legacyspell.entity.projectile.EntityDashStop;
import legacyspell.entity.projectile.EntitySlashEffect;
import legacyspell.entity.projectile.EntityWindBlade;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class LegacyEntities {

    private static int id = 0;

    private LegacyEntities() {
        throw new UnsupportedOperationException("No instances.");
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<EntityEntry> event) {

        IForgeRegistry<EntityEntry> registry = event.getRegistry();

        registry.register(EntityEntryBuilder.<EntityWindBlade>create()
                .entity(EntityWindBlade.class)
                .id(new ResourceLocation(Reference.MOD_ID, "wind_blade"), id++)
                .name(Reference.MOD_ID + ".wind_blade")
                .tracker(64, 10, true)
                .build());

        registry.register(EntityEntryBuilder.<EntitySlashEffect>create()
                .entity(EntitySlashEffect.class)
                .id(new ResourceLocation(Reference.MOD_ID, "slash_effect"), id++)
                .name(Reference.MOD_ID + ".slash_effect")
                .tracker(64, 10, true)
                .build());

        registry.register(EntityEntryBuilder.<EntityDashStop>create()
                .entity(EntityDashStop.class)
                .id(new ResourceLocation(Reference.MOD_ID, "dash_stop"), id++)
                .name(Reference.MOD_ID + ".dash_stop")
                .tracker(64, 10, true)
                .build());
    }
}
