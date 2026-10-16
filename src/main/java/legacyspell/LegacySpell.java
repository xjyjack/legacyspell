package legacyspell;

import legacyspell.network.AeropicFaceMessage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
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

    public static final SimpleNetworkWrapper NETWORK =
            NetworkRegistry.INSTANCE.newSimpleChannel("legacyspell");

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.registerRenderers();
        NETWORK.registerMessage(AeropicFaceMessage.Handler.class, AeropicFaceMessage.class, 0, Side.CLIENT);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }
}
