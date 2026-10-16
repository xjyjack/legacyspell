package legacyspell.spell;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Bookkeeping for the 航天腾跃 (Aeropic) hit-chain ("只要击中目标就可以重复进行").
 *
 * <p>The remaining-hit counter rides on the caster's persistent entity data, so it survives across the
 * ticks the {@code acrobatics} effect runs. Kept in its own class both to keep the NBT key in one place
 * and so it can be driven directly by the offline verification harness.</p>
 */
public final class AeropicChain {

    private static final String TAG = "legacyspell:aeropic_hits";

    private AeropicChain() {
        throw new UnsupportedOperationException("No instances.");
    }

    /** Arms a fresh cast: the caster may chain through up to {@code hits} targets. */
    public static void arm(EntityLivingBase caster, int hits) {
        caster.getEntityData().setInteger(TAG, Math.max(0, hits));
    }

    /** How many more targets the current cast may still chain through. */
    public static int remaining(EntityLivingBase caster) {
        return caster.getEntityData().getInteger(TAG);
    }

    public static void setRemaining(EntityLivingBase caster, int hits) {
        caster.getEntityData().setInteger(TAG, Math.max(0, hits));
    }

    /** Convenience for tests / dumps. */
    public static NBTTagCompound data(EntityLivingBase caster) {
        return caster.getEntityData();
    }
}
