package legacyspell.client.renderer;

import legacyspell.entity.projectile.EntityEnderChain;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderEnderChain extends Render<EntityEnderChain> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("legacyspell", "textures/entity/ender_shackle.png");

    private static final int SEGMENTS = 8;
    private static final double WIDTH = 0.3;

    public RenderEnderChain(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0f;
    }

    @Override
    public void doRender(EntityEnderChain entity, double x, double y, double z, float entityYaw, float partialTicks) {

        double ax = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double ay = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double az = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        Vec3d end;

        EntityLivingBase victim = entity.getVictim();
        if (victim != null) {
            double vx = victim.lastTickPosX + (victim.posX - victim.lastTickPosX) * partialTicks;
            double vy = victim.lastTickPosY + (victim.posY - victim.lastTickPosY) * partialTicks
                    + victim.height * 0.5;
            double vz = victim.lastTickPosZ + (victim.posZ - victim.lastTickPosZ) * partialTicks;
            end = new Vec3d(vx - ax, vy - ay, vz - az);
        } else {
            end = new Vec3d(0, 1.2, 0);
        }

        double length = end.length();
        if (length < 0.05) return;

        Vec3d dir = end.scale(1.0 / length);
        Vec3d camLocal = new Vec3d(-x, -y, -z);
        Vec3d mid = end.scale(0.5);
        Vec3d toCam = camLocal.subtract(mid);

        if (toCam.lengthSquared() < 1.0E-8) return;
        toCam = toCam.normalize();

        Vec3d side = dir.crossProduct(toCam);
        if (side.lengthSquared() < 1.0E-8) return;
        side = side.normalize().scale(WIDTH * 0.5);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);

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

        int alpha = 245;

        for (int i = 0; i < SEGMENTS; i++) {

            double t0 = (double) i / SEGMENTS;
            double t1 = (double) (i + 1) / SEGMENTS;

            Vec3d p0 = end.scale(t0);
            Vec3d p1 = end.scale(t1);

            double v0 = i % 2 == 0 ? 0.0 : 1.0;
            double v1 = i % 2 == 0 ? 1.0 : 0.0;

            vertex(buffer, p0.subtract(side), 0.0, v0, alpha);
            vertex(buffer, p0.add(side), 1.0, v0, alpha);
            vertex(buffer, p1.add(side), 1.0, v1, alpha);
            vertex(buffer, p1.subtract(side), 0.0, v1, alpha);
        }

        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static void vertex(BufferBuilder buffer, Vec3d pos, double u, double v, int alpha) {
        buffer.pos(pos.x, pos.y, pos.z).tex(u, v).color(210, 160, 255, alpha).endVertex();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEnderChain entity) {
        return TEXTURE;
    }
}
