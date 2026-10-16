package legacyspell.registry;

import electroblob.wizardry.spell.Spell;
import legacyspell.Reference;
import legacyspell.spell.SpellAeropic;
import legacyspell.spell.SpellIronSlash;
import legacyspell.spell.SpellWindBlade;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@ObjectHolder(Reference.MOD_ID)
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class LegacySpells {

    public static final Spell wind_blade = placeholder();
    public static final Spell iron_slash = placeholder();
    public static final Spell aeropic = placeholder();

    private LegacySpells() {
        throw new UnsupportedOperationException("No instances.");
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private static <T> T placeholder() {
        return null;
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Spell> event) {
        IForgeRegistry<Spell> registry = event.getRegistry();
        registry.register(new SpellWindBlade());
        registry.register(new SpellIronSlash());
        registry.register(new SpellAeropic());
    }
}
