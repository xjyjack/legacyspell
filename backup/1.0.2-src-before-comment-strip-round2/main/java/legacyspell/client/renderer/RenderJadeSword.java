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

/**
 * 玉剑渲染器 —— 直接渲染 {@link ModelJadeSword}（由 ISS 的 Bedrock 模型逐 cube 移植）。
 *
 * <p>朝向约定：模型 -Z 为剑尖。按 1.12.2 投射物旋转约定
 * （{@code shoot()} 会写 {@code rotationYaw = atan2(mx, mz)}、{@code rotationPitch = atan2(my, horiz)}）
 * 使用 {@code rotate(-yaw,0,1,0); rotate(pitch,1,0,0)}，即可让 -Z 指向运动方向。
 * ISS 贴图带大量透明像素，因此关闭背面剔除并开启混合。
 */
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

        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

        GlStateManager.rotate(-yaw, 0f, 1f, 0f);
        GlStateManager.rotate(pitch, 1f, 0f, 0f);

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
