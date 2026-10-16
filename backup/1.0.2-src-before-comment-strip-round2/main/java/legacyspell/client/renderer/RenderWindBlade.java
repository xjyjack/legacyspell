package legacyspell.client.renderer;

import legacyspell.entity.projectile.EntityWindBlade;
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
public class RenderWindBlade extends Render<EntityWindBlade> {

    private static final int TINT_R = 200;
    private static final int TINT_G = 240;
    private static final int TINT_B = 255;
    private static final int TINT_A = 220;

    private final ResourceLocation texture;

    public RenderWindBlade(RenderManager renderManager, ResourceLocation texture) {
        super(renderManager);
        this.texture = texture;
        this.shadowSize = 0f;
    }

    @Override
    public void doRender(EntityWindBlade entity, double x, double y, double z, float entityYaw, float partialTicks) {

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);

        float yaw = lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw);
        float pitch = lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch);
        GlStateManager.rotate(yaw, 0f, 1f, 0f);
        GlStateManager.rotate(-pitch, 1f, 0f, 0f);

        float width = lerp(partialTicks, entity.getPrevRadius(), entity.getRadius()) * 1.4f;
        float half = width * 0.5f;

        this.bindTexture(this.texture);

        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(-half, 0.0d, -half).tex(0d, 1d).color(TINT_R, TINT_G, TINT_B, TINT_A).endVertex();
        buffer.pos(half, 0.0d, -half).tex(1d, 1d).color(TINT_R, TINT_G, TINT_B, TINT_A).endVertex();
        buffer.pos(half, 0.0d, half).tex(1d, 0d).color(TINT_R, TINT_G, TINT_B, TINT_A).endVertex();
        buffer.pos(-half, 0.0d, half).tex(0d, 0d).color(TINT_R, TINT_G, TINT_B, TINT_A).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWindBlade entity) {
        return this.texture;
    }

    private static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }
}
