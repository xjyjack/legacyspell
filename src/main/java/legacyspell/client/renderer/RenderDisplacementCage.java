package legacyspell.client.renderer;

import legacyspell.entity.projectile.EntityDisplacementCage;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDisplacementCage extends Render<EntityDisplacementCage> {

    public RenderDisplacementCage(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityDisplacementCage entity, double x, double y, double z, float entityYaw, float partialTicks) {
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDisplacementCage entity) {
        return null;
    }
}
