package legacyspell.client.renderer;

import legacyspell.entity.projectile.EntitySlashEffect;
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
public class RenderSlashEffect extends Render<EntitySlashEffect> {

    private static final ResourceLocation SLASH = new ResourceLocation("legacyspell", "textures/entity/iron_slash/slash.png");
    private static final ResourceLocation SLASH_ICE = new ResourceLocation("legacyspell", "textures/entity/iron_slash/slash_ice.png");

    public RenderSlashEffect(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0f;
    }

    @Override
    public void doRender(EntitySlashEffect entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float yaw = entity.getSlashYRot() + entity.getRandomRotOffset();
        float half = 1.0f;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(yaw, 0f, 1f, 0f);
        GlStateManager.rotate(-30f, 1f, 0f, 0f);

        this.bindTexture(entity.isIceMode() ? SLASH_ICE : SLASH);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(-half, -half, 0).tex(0d, 1d).color(255, 255, 255, 220).endVertex();
        buffer.pos(half, -half, 0).tex(1d, 1d).color(255, 255, 255, 220).endVertex();
        buffer.pos(half, half, 0).tex(1d, 0d).color(255, 255, 255, 220).endVertex();
        buffer.pos(-half, half, 0).tex(0d, 0d).color(255, 255, 255, 220).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySlashEffect entity) {
        return SLASH;
    }
}
