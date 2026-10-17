package legacyspell.client;

import legacyspell.CommonProxy;
import legacyspell.Reference;
import legacyspell.client.renderer.RenderEnderShackle;
import legacyspell.client.renderer.RenderDashStop;
import legacyspell.client.renderer.RenderDisplacementCage;
import legacyspell.client.renderer.RenderEnderChain;
import legacyspell.client.renderer.RenderEarthEdge;
import legacyspell.client.renderer.RenderGaiaPortal;
import legacyspell.client.renderer.RenderJadeLoong;
import legacyspell.client.renderer.RenderJadeSword;
import legacyspell.client.renderer.RenderSlashEffect;
import legacyspell.client.renderer.RenderSpectralAerwhale;
import legacyspell.client.renderer.RenderSpellQuad;
import legacyspell.client.renderer.RenderWindBlade;
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
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderShackle.class,
                RenderEnderShackle::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderChain.class,
                RenderEnderChain::new);

        RenderingRegistry.registerEntityRenderingHandler(EntitySpectralAerwhale.class,
                RenderSpectralAerwhale::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityDisplacementCage.class,
                RenderDisplacementCage::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGaiaPortal.class,
                RenderGaiaPortal::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityEarthEdge.class,
                RenderEarthEdge::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityObsidianRod.class,
                manager -> new RenderSpellQuad<>(manager,
                        new ResourceLocation(Reference.MOD_ID, "textures/entity/obsidian_rod.png"),
                        0.9f, 0xffffff, 100.0 / 128.0, 50.0 / 128.0));
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
