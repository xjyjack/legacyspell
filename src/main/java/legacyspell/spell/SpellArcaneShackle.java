package legacyspell.spell;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellProjectile;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityArcaneShackle;
import net.minecraft.entity.EntityLivingBase;

public class SpellArcaneShackle extends SpellProjectile<EntityArcaneShackle> {

    private static final double BASE_LASH_RADIUS = 5.0;
    private static final float RESTRAINT_STRENGTH = 0.015f;

    public SpellArcaneShackle() {
        super(Reference.MOD_ID, "ender_shackle", EntityArcaneShackle::new);

        addProperties(Spell.DAMAGE);

        soundValues(0.8f, 1.0f, 0.2f);
    }

    @Override
    protected void addProjectileExtras(EntityArcaneShackle projectile, EntityLivingBase caster, SpellModifiers modifiers) {

        float potency = modifiers.get(SpellModifiers.POTENCY);

        projectile.setChainHealth(15.0f * potency);
        projectile.setChainLifetime((int) (100 + 40 * potency));
        projectile.setLashRadius(BASE_LASH_RADIUS);
        projectile.setRestraintStrength(RESTRAINT_STRENGTH);
    }
}
