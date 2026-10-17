package legacyspell.registry;

import legacyspell.LegacySpell;
import legacyspell.Reference;
import legacyspell.potion.PotionAcrobatics;
import legacyspell.potion.PotionAnchored;
import legacyspell.potion.PotionWhaleBlessing;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@ObjectHolder(Reference.MOD_ID)
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class LegacyPotions {

    public static final Potion acrobatics = placeholder();
    public static final Potion whale_blessing = placeholder();
    public static final Potion anchored = placeholder();

    private LegacyPotions() {
        throw new UnsupportedOperationException("No instances.");
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private static <T> T placeholder() {
        return null;
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Potion> event) {
        IForgeRegistry<Potion> registry = event.getRegistry();
        registry.register(new PotionAcrobatics());
        registry.register(new PotionWhaleBlessing());
        registry.register(new PotionAnchored());
    }
}
