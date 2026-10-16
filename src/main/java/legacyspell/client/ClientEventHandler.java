package legacyspell.client;

import legacyspell.registry.LegacyPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    private static final float SPIN_PER_TICK = 40.0f;
    private static final float PRONE_PITCH = 90.0f;
    private static final double PRONE_PIVOT = 0.9;


    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.isPotionActive(LegacyPotions.acrobatics)) return;

        float spin = spin(player, event.getPartialRenderTick());
        double x = event.getX(), y = event.getY(), z = event.getZ();

        Vec3d facing = horizontalFacing(player);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.0, PRONE_PIVOT, 0.0);
        GlStateManager.rotate(spin, (float) facing.x, 0.0f, (float) facing.z);
        GlStateManager.rotate(PRONE_PITCH, (float) facing.z, 0.0f, (float) -facing.x);
        GlStateManager.translate(0.0, -PRONE_PIVOT, 0.0);
        GlStateManager.translate(-x, -y, -z);
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (event.getEntityPlayer().isPotionActive(LegacyPotions.acrobatics)) {
            GlStateManager.popMatrix();
        }
    }


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null || !player.isPotionActive(LegacyPotions.acrobatics)) return;

        for (int i = 0; i < 8; i++) {
            double angle = player.world.rand.nextDouble() * Math.PI * 2.0;
            double radius = 0.35 + player.world.rand.nextDouble() * 0.45;
            double ox = Math.cos(angle) * radius;
            double oz = Math.sin(angle) * radius;
            double oy = 0.1 + player.world.rand.nextDouble() * 1.8;
            player.world.spawnParticle(EnumParticleTypes.CLOUD,
                    player.posX + ox, player.posY + oy, player.posZ + oz,
                    ox * 0.10, 0.02 + player.world.rand.nextDouble() * 0.02, oz * 0.10);
        }
    }

    private static Vec3d horizontalFacing(EntityPlayer player) {
        Vec3d look = player.getLookVec();
        Vec3d facing = new Vec3d(look.x, 0.0, look.z);
        if (facing.lengthSquared() < 1.0E-4D) {
            facing = new Vec3d(0.0, 0.0, 1.0);
        }
        return facing.normalize();
    }

    private static float spin(EntityPlayer player, double partialTicks) {
        return (float) ((player.ticksExisted + partialTicks) * (double) SPIN_PER_TICK);
    }
}
