package legacyspell.spell;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityJadeSword;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * 剑舞（Sword Dance）—— 复刻自 ISS: Magic From The East 的 {@code SwordDanceSpell.java}。
 *
 * <p>原版做法：所有玉剑都生成在施法者双眼下方 0.75 格的同一点上，初速度统一为
 * {@code (0,0,1).xRot(1°).yRot(360/剑数 × i)}（1° 的下压倾角 + 均匀的水平扇形），
 * 速度 1.0/tick；随后由 {@link EntityJadeSword} 完成"外飞 → 悬停 → 追踪 → 碎裂"四段行为。
 *
 * <p>与原版的唯一差异：召唤数量随法术强度（potency）缩放，以满足附属模组的需求。
 */
public class SpellSwordDance extends Spell {

    private static final int BASE_SWORD_COUNT = 5;
    private static final int MAX_SWORD_COUNT = 16;
    private static final int WAIT_TICKS = 120;

    /** 原版 motion.xRot(Mth.DEG_TO_RAD) 的 1° 下压倾角。 */
    private static final double TILT_RADIANS = Math.toRadians(1.0);

    public SpellSwordDance() {
        super(Reference.MOD_ID, "sword_dance", EnumAction.BOW, false);
        addProperties(Spell.DAMAGE);
        soundValues(0.8f, 1.0f, 0.2f);
    }

    @Override
    public boolean cast(World world, EntityPlayer caster, EnumHand hand, int modifiers, SpellModifiers spellModifiers) {
        return doCast(world, caster, spellModifiers);
    }

    @Override
    public boolean cast(World world, EntityLiving caster, EnumHand hand, int modifiers, EntityLivingBase target, SpellModifiers spellModifiers) {
        return doCast(world, caster, spellModifiers);
    }

    private boolean doCast(World world, EntityLivingBase caster, SpellModifiers modifiers) {
        if (world.isRemote) return true;

        float potency = modifiers.get(SpellModifiers.POTENCY);
        float damage = this.getProperty(Spell.DAMAGE).floatValue() * potency;

        int swordCount = Math.round(BASE_SWORD_COUNT + (potency - 1.0f) * 3.0f);
        swordCount = Math.max(2, Math.min(MAX_SWORD_COUNT, swordCount));

        // 原版：offset = 360 / swordCount（整数除法），center = eyePosition + (0, -0.75, 0)
        int offset = 360 / swordCount;
        double centerX = caster.posX;
        double centerY = caster.posY + caster.getEyeHeight() - 0.75;
        double centerZ = caster.posZ;

        double cosTilt = Math.cos(TILT_RADIANS);
        double sinTilt = Math.sin(TILT_RADIANS);

        for (int i = 0; i < swordCount; i++) {

            // 原版：motion = (0,0,1).xRot(1°).yRot(offset * i°)
            double theta = Math.toRadians(offset * i);
            double motionX = cosTilt * Math.sin(theta);
            double motionY = -sinTilt;
            double motionZ = cosTilt * Math.cos(theta);

            EntityJadeSword sword = new EntityJadeSword(world, caster);
            sword.setSwordDamage(damage);
            sword.setWaitTimer(WAIT_TICKS);
            sword.setRememberedTarget(caster.getLastAttackedEntity());
            sword.setPosition(centerX, centerY, centerZ);
            sword.motionX = motionX;
            sword.motionY = motionY;
            sword.motionZ = motionZ;
            sword.velocityChanged = true;

            // 原版：每三把剑静音一把，避免命中音效过密
            if (i % 3 == 0) sword.setSilent(true);

            world.spawnEntity(sword);
        }

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
    }
}
