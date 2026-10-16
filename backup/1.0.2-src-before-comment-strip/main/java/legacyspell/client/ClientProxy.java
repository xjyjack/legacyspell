package legacyspell.client;

import legacyspell.CommonProxy;
import legacyspell.Reference;
import legacyspell.client.renderer.RenderDashStop;
import legacyspell.client.renderer.RenderSlashEffect;
import legacyspell.client.renderer.RenderWindBlade;
import legacyspell.entity.projectile.EntityDashStop;
import legacyspell.entity.projectile.EntitySlashEffect;
import legacyspell.entity.projectile.EntityWindBlade;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityWindBlade.class,
                manager -> new RenderWindBlade(manager,
                        new ResourceLocation(Reference.MOD_ID, "textures/items/wind_blade.png")));
        RenderingRegistry.registerEntityRenderingHandler(EntitySlashEffect.class,
                RenderSlashEffect::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityDashStop.class,
                RenderDashStop::new);
    }

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }
}
