package legacyspell.client.renderer;

import legacyspell.client.model.ModelJadeSword;
import legacyspell.entity.projectile.EntityJadeSword;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderJadeSword extends Render<EntityJadeSword> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("legacyspell", "textures/entity/jade_sword.png");

    private final ModelJadeSword model = new ModelJadeSword();

    public RenderJadeSword(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0f;
    }

    @Override
    public void doRender(EntityJadeSword entity, double x, double y, double z, float entityYaw, float partialTicks) {

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);

        entity.updateOrientation(entity.motionX, entity.motionY, entity.motionZ);

        GlStateManager.rotate(entity.lastYaw + 180.0f, 0f, 1f, 0f);
        GlStateManager.rotate(entity.lastPitch, 1f, 0f, 0f);

        this.bindTexture(TEXTURE);

        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

        this.model.render(entity, 0f, 0f, entity.ticksExisted + partialTicks, 0f, 0f, 0.0625f);

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityJadeSword entity) {
        return TEXTURE;
    }
}
