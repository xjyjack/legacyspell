package legacyspell;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.MOD_VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:ebwizardry"
)
public class LegacySpell {

    public static final String MODID = Reference.MOD_ID;
    public static final String NAME = Reference.MOD_NAME;

    @Mod.Instance(Reference.MOD_ID)
    public static LegacySpell instance;

    @SidedProxy(clientSide = "legacyspell.client.ClientProxy", serverSide = "legacyspell.CommonProxy")
    public static CommonProxy proxy;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }
}
