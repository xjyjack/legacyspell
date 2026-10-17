package legacyspell.client.renderer;

import legacyspell.entity.projectile.EntityGaiaPortal;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderGaiaPortal extends Render<EntityGaiaPortal> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("legacyspell", "textures/entity/gate_portal.png");

    private static final float WIDTH = 1.2f;
    private static final float HEIGHT = 1.2f;

    private static final double U_MAX = 0.5;
    private static final double V_MAX = 0.5;

    public RenderGaiaPortal(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0f;
    }

    @Override
    public void doRender(EntityGaiaPortal entity, double x, double y, double z, float entityYaw, float partialTicks) {

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(-this.renderManager.playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(this.renderManager.playerViewX, 1f, 0f, 0f);

        float halfW = WIDTH * 0.5f;
        float halfH = HEIGHT * 0.5f;

        this.bindTexture(TEXTURE);

        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(-halfW, -halfH, 0.0d).tex(0d, V_MAX).color(140, 235, 150, 235).endVertex();
        buffer.pos(halfW, -halfH, 0.0d).tex(U_MAX, V_MAX).color(140, 235, 150, 235).endVertex();
        buffer.pos(halfW, halfH, 0.0d).tex(U_MAX, 0d).color(140, 235, 150, 235).endVertex();
        buffer.pos(-halfW, halfH, 0.0d).tex(0d, 0d).color(140, 235, 150, 235).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGaiaPortal entity) {
        return TEXTURE;
    }
}
