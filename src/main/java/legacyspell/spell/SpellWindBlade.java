package legacyspell.spell;

import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellProjectile;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityWindBlade;
import net.minecraft.world.World;

import java.util.function.Function;

public class SpellWindBlade extends SpellProjectile<EntityWindBlade> {

    public SpellWindBlade() {
        this(Reference.MOD_ID, "wind_blade", EntityWindBlade::new);
    }

    public SpellWindBlade(String modID, String name, Function<World, EntityWindBlade> projectileFactory) {
        super(modID, name, projectileFactory);

        addProperties(Spell.DAMAGE);

        soundValues(0.8f, 1.0f, 0.2f);
    }

    @Override
    protected float calculateVelocity(EntityMagicProjectile projectile, SpellModifiers modifiers, float launchHeight) {
        return super.calculateVelocity(projectile, modifiers, launchHeight) * EntityWindBlade.VELOCITY_MULTIPLIER;
    }
}
