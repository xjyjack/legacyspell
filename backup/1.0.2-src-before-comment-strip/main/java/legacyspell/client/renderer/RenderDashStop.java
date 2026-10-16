package legacyspell.client.renderer;

import legacyspell.entity.projectile.EntityDashStop;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * Null renderer for {@link EntityDashStop}. The entity is a pure server-side bookkeeping marker; it is
 * still tracked to clients (so they never see an unknown entity), but it must draw nothing.
 */
public class RenderDashStop extends Render<EntityDashStop> {

    public RenderDashStop(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityDashStop entity, double x, double y, double z, float entityYaw, float partialTicks) {
        // intentionally empty
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDashStop entity) {
        return null;
    }
}
