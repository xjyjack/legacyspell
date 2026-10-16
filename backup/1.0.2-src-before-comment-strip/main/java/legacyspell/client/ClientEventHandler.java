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

/**
 * Client-only visuals for the 航天腾跃 (Aeropic) lunge.
 *
 * <p><b>Third person / other players:</b> the caster lies <b>prone (face-down)</b> first, then spins
 * while prone — the 1.12.2 stand-in for 1.20.1's {@code setSpinAttackType(FIRE)} trident spin. 1.12.2
 * has no native spin-attack model pose, and {@code RenderPlayerEvent.Pre} fires <em>before</em> the
 * renderer translates/rotates the model, so the pose cannot be produced by tweaking the entity's
 * yaw/pitch fields. Instead the whole model is rotated with the GL matrix: {@code Pre} pushes a
 * transform (tip the standing rig face-down, then roll it about its own long axis) and {@code Post}
 * pops it, so the default player render runs inside that transform. That makes the prone spin show in
 * third person (front and back) and to every other player.</p>
 *
 * <p><b>First person:</b> the body is never drawn, and the request is that the caster simply
 * <em>translate along the movement direction</em> rather than spin — so <b>no camera rotation is
 * applied at all</b> in first person. The dash velocity carries them straight down the movement
 * direction, and a wreath of white mist around the caster sells the motion in every view.</p>
 */
@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    /** Degrees of spin per tick while a lunge is active in third person (a full turn every 9 ticks). */
    private static final float SPIN_PER_TICK = 40.0f;
    /** Forward tip that lays the model face-down to horizontal. */
    private static final float PRONE_PITCH = 90.0f;
    /** Height the prone tip pivots around (waist), so the body swings about its middle, not its feet. */
    private static final double PRONE_PIVOT = 0.9;

    // --- third person (and every other player's view): tip prone, then drill-spin --------------------

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.isPotionActive(LegacyPotions.acrobatics)) return;

        float spin = spin(player, event.getPartialRenderTick());
        double x = event.getX(), y = event.getY(), z = event.getZ();

        // The axis the body tips over is horizontal and across the shoulders (perpendicular to the
        // way the caster is facing), so the tip always lays them face-first regardless of heading.
        Vec3d facing = horizontalFacing(player);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.0, PRONE_PIVOT, 0.0);
        // Spin: roll the (soon to be horizontal) body about its own forward long axis -> a drill spin.
        GlStateManager.rotate(spin, (float) facing.x, 0.0f, (float) facing.z);
        // Prone: tip the standing rig face-down to horizontal. First in matrix order = applied first,
        // so the body is already flat when the spin above sweeps it around.
        GlStateManager.rotate(PRONE_PITCH, (float) facing.z, 0.0f, (float) -facing.x);
        GlStateManager.translate(0.0, -PRONE_PIVOT, 0.0);
        GlStateManager.translate(-x, -y, -z);
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        // Balanced with the push in Pre: both fire (Pre is never cancelled) and both test the same flag,
        // so the matrix stack stays even even if the model itself ends up not being drawn this frame.
        if (event.getEntityPlayer().isPotionActive(LegacyPotions.acrobatics)) {
            GlStateManager.popMatrix();
        }
    }

    // --- white mist wreathing the caster (visible in every view, including first person) --------------

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null || !player.isPotionActive(LegacyPotions.acrobatics)) return;

        // A drifting wreath of white vapour clinging to the caster for the whole lunge.
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
