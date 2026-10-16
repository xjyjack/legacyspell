package legacyspell.spell;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellProjectile;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityJadeLoong;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * 御龙滑翔（Dragon Glide）—— 复刻自 ISS: Magic From The East 的 {@code DragonGlideSpell.java}。
 *
 * <p>原版：向前方 1 格、双眼高度下半格身高处召出游龙，以 0.6/tick 沿视线滑翔。
 * Wizardry 的 {@link SpellProjectile} 会把速度换算成 {@code range / lifetime}，
 * 当前 JSON（range 48、寿命 80）恰好也是 0.6，因此只需把出生点改成原版的位置。
 */
public class SpellDragonGlide extends SpellProjectile<EntityJadeLoong> {

    public SpellDragonGlide() {
        super(Reference.MOD_ID, "dragon_glide", EntityJadeLoong::new);

        addProperties(Spell.DAMAGE);

        soundValues(0.8f, 1.0f, 0.2f);
    }

    @Override
    protected void addProjectileExtras(EntityJadeLoong projectile, @Nullable EntityLivingBase caster,
                                       SpellModifiers modifiers) {
        if (caster != null) {
            // 原版：position().add(0, eyeHeight - bbHeight/2, 0).add(getForward())
            Vec3d forward = caster.getLookVec();
            double x = caster.posX + forward.x;
            double y = caster.posY + caster.getEyeHeight() - projectile.height * 0.5;
            double z = caster.posZ + forward.z;
            projectile.setPosition(x, y, z);
        }
    }
}
