package legacyspell.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.math.Vec3d;

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
