package legacyspell.client.renderer;

import legacyspell.client.model.AerwhaleModel;
import legacyspell.entity.mob.EntitySpectralAerwhale;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderSpectralAerwhale extends RenderLiving<EntitySpectralAerwhale> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("legacyspell", "textures/entity/aerwhale.png");

    private static final float OPACITY = 0.72f;

    public RenderSpectralAerwhale(RenderManager renderManager) {
        super(renderManager, new AerwhaleModel(), 0f);
    }

    @Override
    protected void preRenderCallback(EntitySpectralAerwhale whale, float partialTick) {
        GlStateManager.translate(0, 1.2, 0);
        GlStateManager.rotate(-90, 0, 1, 0);
        GlStateManager.scale(2, 2, 2);
        GlStateManager.enableCull();
    }

    @Override
    public void doRender(EntitySpectralAerwhale whale, double x, double y, double z, float entityYaw, float partialTicks) {

        if (whale.ticksExisted > whale.getLifetimeTicks() + 40) return;

        float fade = whale.getFade(partialTicks);
        float alpha = OPACITY * (fade > 0.02f ? fade : 0.3f);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        GlStateManager.color(0.82f, 0.92f, 1.0f, alpha);

        super.doRender(whale, x, y, z, entityYaw, partialTicks);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySpectralAerwhale whale) {
        return TEXTURE;
    }
}
