package legacyspell.spell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public final class AeropicChain {

    private static final String TAG = "legacyspell:aeropic_hits";
    private static final String TAG_HIT_INDEX = "legacyspell:aeropic_hit_index";
    private static final String TAG_CHAIN_TARGET = "legacyspell:aeropic_chain_target";

    private AeropicChain() {
        throw new UnsupportedOperationException("No instances.");
    }

    public static void arm(EntityLivingBase caster, int hits) {
        NBTTagCompound data = caster.getEntityData();
        data.setInteger(TAG, Math.max(0, hits));
        data.setInteger(TAG_HIT_INDEX, 0);
        data.setInteger(TAG_CHAIN_TARGET, -1);
    }

    public static int remaining(EntityLivingBase caster) {
        return caster.getEntityData().getInteger(TAG);
    }

    public static void setRemaining(EntityLivingBase caster, int hits) {
        caster.getEntityData().setInteger(TAG, Math.max(0, hits));
    }

    public static int hitIndex(EntityLivingBase caster) {
        return caster.getEntityData().getInteger(TAG_HIT_INDEX);
    }

    public static void setHitIndex(EntityLivingBase caster, int index) {
        caster.getEntityData().setInteger(TAG_HIT_INDEX, Math.max(0, index));
    }

    public static void rememberChainTarget(EntityLivingBase caster, EntityLivingBase target) {
        caster.getEntityData().setInteger(TAG_CHAIN_TARGET, target == null ? -1 : target.getEntityId());
    }

    public static EntityLivingBase chainTarget(EntityLivingBase caster) {
        int id = caster.getEntityData().getInteger(TAG_CHAIN_TARGET);
        if (id < 0) return null;

        Entity entity = caster.world.getEntityByID(id);
        return entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }
}
