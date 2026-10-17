package legacyspell.registry;

import electroblob.wizardry.spell.Spell;
import legacyspell.Reference;
import legacyspell.spell.SpellAeropic;
import legacyspell.spell.SpellAerwhaleSong;
import legacyspell.spell.SpellEnderShackle;
import legacyspell.spell.SpellDisplacementCage;
import legacyspell.spell.SpellDragonGlide;
import legacyspell.spell.SpellEfface;
import legacyspell.spell.SpellGaiaTreasury;
import legacyspell.spell.SpellIronSlash;
import legacyspell.spell.SpellObsidianRod;
import legacyspell.spell.SpellRebirth;
import legacyspell.spell.SpellSkyCleave;
import legacyspell.spell.SpellSwordDance;
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

    public static final Spell sky_cleave = placeholder();
    public static final Spell efface = placeholder();
    public static final Spell rebirth = placeholder();
    public static final Spell sword_dance = placeholder();
    public static final Spell dragon_glide = placeholder();
    public static final Spell ender_shackle = placeholder();

    public static final Spell aerwhale_song = placeholder();
    public static final Spell displacement_cage = placeholder();
    public static final Spell gaia_treasury = placeholder();
    public static final Spell obsidian_rod = placeholder();

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

        registry.register(new SpellSkyCleave());
        registry.register(new SpellEfface());
        registry.register(new SpellRebirth());
        registry.register(new SpellSwordDance());
        registry.register(new SpellDragonGlide());
        registry.register(new SpellEnderShackle());

        registry.register(new SpellAerwhaleSong());
        registry.register(new SpellDisplacementCage());
        registry.register(new SpellGaiaTreasury());
        registry.register(new SpellObsidianRod());
    }
}
