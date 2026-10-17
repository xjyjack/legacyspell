package legacyspell.client;

import legacyspell.CommonProxy;
import legacyspell.Reference;
import legacyspell.client.renderer.RenderArcaneShackle;
import legacyspell.client.renderer.RenderDashStop;
import legacyspell.client.renderer.RenderEnderChain;
import legacyspell.client.renderer.RenderJadeLoong;
import legacyspell.client.renderer.RenderJadeSword;
import legacyspell.client.renderer.RenderSlashEffect;
import legacyspell.client.renderer.RenderWindBlade;
import legacyspell.entity.projectile.EntityArcaneShackle;
import legacyspell.entity.projectile.EntityDashStop;
import legacyspell.entity.projectile.EntityEnderChain;
import legacyspell.entity.projectile.EntityJadeLoong;
import legacyspell.entity.projectile.EntityJadeSword;
import legacyspell.entity.projectile.EntitySlashEffect;
import legacyspell.entity.projectile.EntityWindBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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

        RenderingRegistry.registerEntityRenderingHandler(EntityJadeSword.class,
                RenderJadeSword::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityJadeLoong.class,
                RenderJadeLoong::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityArcaneShackle.class,
                RenderArcaneShackle::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderChain.class,
                RenderEnderChain::new);
    }

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

    @Override
    public void handleAeropicFace(float yaw, float pitch) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        if (player == null) return;
        player.rotationYaw = yaw;
        player.prevRotationYaw = yaw;
        player.rotationYawHead = yaw;
        player.prevRotationYawHead = yaw;
        player.rotationPitch = pitch;
        player.prevRotationPitch = pitch;
    }
}
