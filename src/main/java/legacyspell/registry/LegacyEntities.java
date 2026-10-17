package legacyspell.registry;

import legacyspell.Reference;
import legacyspell.entity.mob.EntitySpectralAerwhale;
import legacyspell.entity.projectile.EntityEnderShackle;
import legacyspell.entity.projectile.EntityDashStop;
import legacyspell.entity.projectile.EntityDisplacementCage;
import legacyspell.entity.projectile.EntityEnderChain;
import legacyspell.entity.projectile.EntityEarthEdge;
import legacyspell.entity.projectile.EntityGaiaPortal;
import legacyspell.entity.projectile.EntityJadeLoong;
import legacyspell.entity.projectile.EntityJadeSword;
import legacyspell.entity.projectile.EntityObsidianRod;
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

        registry.register(EntityEntryBuilder.<EntityJadeSword>create()
                .entity(EntityJadeSword.class)
                .id(new ResourceLocation(Reference.MOD_ID, "jade_sword"), id++)
                .name(Reference.MOD_ID + ".jade_sword")
                .tracker(64, 10, true)
                .build());

        registry.register(EntityEntryBuilder.<EntityJadeLoong>create()
                .entity(EntityJadeLoong.class)
                .id(new ResourceLocation(Reference.MOD_ID, "jade_loong"), id++)
                .name(Reference.MOD_ID + ".jade_loong")
                .tracker(96, 1, true)
                .build());

        registry.register(EntityEntryBuilder.<EntityEnderShackle>create()
                .entity(EntityEnderShackle.class)
                .id(new ResourceLocation(Reference.MOD_ID, "ender_shackle"), id++)
                .name(Reference.MOD_ID + ".ender_shackle")
                .tracker(64, 10, true)
                .build());

        registry.register(EntityEntryBuilder.<EntityEnderChain>create()
                .entity(EntityEnderChain.class)
                .id(new ResourceLocation(Reference.MOD_ID, "ender_chain"), id++)
                .name(Reference.MOD_ID + ".ender_chain")
                .tracker(96, 1, false)
                .build());

        registry.register(EntityEntryBuilder.<EntitySpectralAerwhale>create()
                .entity(EntitySpectralAerwhale.class)
                .id(new ResourceLocation(Reference.MOD_ID, "spectral_aerwhale"), id++)
                .name(Reference.MOD_ID + ".spectral_aerwhale")
                .tracker(96, 2, true)
                .build());

        registry.register(EntityEntryBuilder.<EntityDisplacementCage>create()
                .entity(EntityDisplacementCage.class)
                .id(new ResourceLocation(Reference.MOD_ID, "displacement_cage"), id++)
                .name(Reference.MOD_ID + ".displacement_cage")
                .tracker(64, 10, true)
                .build());

        registry.register(EntityEntryBuilder.<EntityGaiaPortal>create()
                .entity(EntityGaiaPortal.class)
                .id(new ResourceLocation(Reference.MOD_ID, "gaia_portal"), id++)
                .name(Reference.MOD_ID + ".gaia_portal")
                .tracker(32, 10, true)
                .build());

        registry.register(EntityEntryBuilder.<EntityEarthEdge>create()
                .entity(EntityEarthEdge.class)
                .id(new ResourceLocation(Reference.MOD_ID, "earth_edge"), id++)
                .name(Reference.MOD_ID + ".earth_edge")
                .tracker(64, 2, true)
                .build());

        registry.register(EntityEntryBuilder.<EntityObsidianRod>create()
                .entity(EntityObsidianRod.class)
                .id(new ResourceLocation(Reference.MOD_ID, "obsidian_rod"), id++)
                .name(Reference.MOD_ID + ".obsidian_rod")
                .tracker(64, 2, true)
                .build());
    }
}
