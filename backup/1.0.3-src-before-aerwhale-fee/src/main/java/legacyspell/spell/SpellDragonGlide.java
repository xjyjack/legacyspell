package legacyspell.spell;

import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellProjectile;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityJadeLoong;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class SpellDragonGlide extends SpellProjectile<EntityJadeLoong> {

    public static final float VELOCITY_MULTIPLIER = 1.44f;

    public SpellDragonGlide() {
        super(Reference.MOD_ID, "dragon_glide", EntityJadeLoong::new);

        addProperties(Spell.DAMAGE);

        soundValues(0.8f, 1.0f, 0.2f);
    }

    @Override
    protected float calculateVelocity(EntityMagicProjectile projectile, SpellModifiers modifiers, float launchHeight) {
        return super.calculateVelocity(projectile, modifiers, launchHeight) * VELOCITY_MULTIPLIER;
    }

    @Override
    protected void addProjectileExtras(EntityJadeLoong projectile, @Nullable EntityLivingBase caster,
                                       SpellModifiers modifiers) {
        if (caster != null) {
            Vec3d forward = caster.getLookVec();
            double x = caster.posX + forward.x;
            double y = caster.posY + caster.getEyeHeight() - projectile.height * 0.5;
            double z = caster.posZ + forward.z;
            projectile.setPosition(x, y, z);
        }
    }
}
