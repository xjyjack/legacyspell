package legacyspell.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.math.Vec3d;

/**
 * Applies an instantaneous velocity change that actually reaches the client for players.
 *
 * <p>For a mob, writing {@code motionX/Y/Z} is enough because the server integrates the motion itself.
 * A player's position is client-authoritative in 1.12.2, so the server must additionally push the
 * motion down as {@link SPacketEntityVelocity} or the local player never sees the dash. This is the
 * 1.12.2 equivalent of Iron's Spells' {@code ImpulseCastData} / {@code onClientCast} round trip.</p>
 */
public final class MotionHelper {

    private MotionHelper() {
        throw new UnsupportedOperationException("No instances.");
    }

    public static void setMotion(EntityLivingBase entity, double x, double y, double z) {
        entity.motionX = x;
        entity.motionY = y;
        entity.motionZ = z;
        entity.velocityChanged = true;
        if (entity instanceof EntityPlayerMP) {
            ((EntityPlayerMP) entity).connection.sendPacket(
                    new SPacketEntityVelocity(entity.getEntityId(), x, y, z));
        }
    }

    public static void setMotion(EntityLivingBase entity, Vec3d motion) {
        setMotion(entity, motion.x, motion.y, motion.z);
    }
}
