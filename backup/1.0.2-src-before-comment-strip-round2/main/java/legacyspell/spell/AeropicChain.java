package legacyspell.spell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * 航天腾跃连击链的状态载体（挂在施法者的扩展数据上）。
 *
 * <p>除连击次数外，还记录两样东西，用于"从第二击开始自动转向"：
 * <ul>
 *   <li>{@code hit_index} —— 已经命中了几次；</li>
 *   <li>{@code chain_target} —— 上一击命中时，距离玩家最近的非友善实体的实体 id。</li>
 * </ul>
 */
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

    /** 记住本击时刻的"下一个转向目标"。 */
    public static void rememberChainTarget(EntityLivingBase caster, EntityLivingBase target) {
        caster.getEntityData().setInteger(TAG_CHAIN_TARGET, target == null ? -1 : target.getEntityId());
    }

    /** 取出上一击时记下的转向目标；已消失则返回 null。 */
    public static EntityLivingBase chainTarget(EntityLivingBase caster) {
        int id = caster.getEntityData().getInteger(TAG_CHAIN_TARGET);
        if (id < 0) return null;

        Entity entity = caster.world.getEntityByID(id);
        return entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }
}
