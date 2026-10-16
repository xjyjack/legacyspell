package legacyspell.client.renderer;

import legacyspell.entity.projectile.EntityDashStop;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDashStop extends Render<EntityDashStop> {

    public RenderDashStop(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityDashStop entity, double x, double y, double z, float entityYaw, float partialTicks) {
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDashStop entity) {
        return null;
    }
}
