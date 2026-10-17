package legacyspell.spell;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellProjectile;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityEnderShackle;
import net.minecraft.entity.EntityLivingBase;

public class SpellEnderShackle extends SpellProjectile<EntityEnderShackle> {

    private static final double BASE_LASH_RADIUS = 5.0;
    private static final float RESTRAINT_STRENGTH = 0.015f;

    public SpellEnderShackle() {
        super(Reference.MOD_ID, "ender_shackle", EntityEnderShackle::new);

        addProperties(Spell.DAMAGE);

        soundValues(0.8f, 1.0f, 0.2f);
    }

    @Override
    protected void addProjectileExtras(EntityEnderShackle projectile, EntityLivingBase caster, SpellModifiers modifiers) {

        float potency = modifiers.get(SpellModifiers.POTENCY);

        projectile.setChainHealth(15.0f * potency);
        projectile.setChainLifetime((int) (100 + 40 * potency));
        projectile.setLashRadius(BASE_LASH_RADIUS);
        projectile.setRestraintStrength(RESTRAINT_STRENGTH);
    }
}
