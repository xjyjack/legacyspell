package legacyspell.client.renderer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderSpellQuad<T extends Entity> extends Render<T> {

    private final ResourceLocation texture;
    private final float size;
    private final int tint;
    private final double uMax;
    private final double vMax;

    public RenderSpellQuad(RenderManager renderManager, ResourceLocation texture, float size, int tint) {
        this(renderManager, texture, size, tint, 1.0, 1.0);
    }

    public RenderSpellQuad(RenderManager renderManager, ResourceLocation texture, float size, int tint,
                           double uMax, double vMax) {
        super(renderManager);
        this.texture = texture;
        this.size = size;
        this.tint = tint;
        this.uMax = uMax;
        this.vMax = vMax;
        this.shadowSize = 0f;
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(-this.renderManager.playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(this.renderManager.playerViewX, 1f, 0f, 0f);
        GlStateManager.rotate(-entity.rotationPitch, 1f, 0f, 0f);

        float half = this.size * 0.5f;

        this.bindTexture(this.texture);

        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

        int r = (this.tint >> 16) & 0xFF;
        int g = (this.tint >> 8) & 0xFF;
        int b = this.tint & 0xFF;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(-half, -half, 0.0d).tex(0d, this.vMax).color(r, g, b, 235).endVertex();
        buffer.pos(half, -half, 0.0d).tex(this.uMax, this.vMax).color(r, g, b, 235).endVertex();
        buffer.pos(half, half, 0.0d).tex(this.uMax, 0d).color(r, g, b, 235).endVertex();
        buffer.pos(-half, half, 0.0d).tex(0d, 0d).color(r, g, b, 235).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return this.texture;
    }
}
