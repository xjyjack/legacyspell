package legacyspell.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityDisplacementCage;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpellDisplacementCage extends Spell {

    private static final int GROUND_SEARCH_UP = 6;
    private static final int GROUND_SEARCH_DOWN = 6;

    public SpellDisplacementCage() {
        super(Reference.MOD_ID, "displacement_cage", EnumAction.BOW, false);
        addProperties(Spell.RANGE, Spell.EFFECT_RADIUS, Spell.DURATION);
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

        float range = this.getProperty(Spell.RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);
        RayTraceResult result = RayTracer.standardEntityRayTrace(world, caster, range, false);

        Vec3d spawn;
        if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY
                && result.entityHit instanceof EntityLivingBase
                && result.entityHit != caster
                && !caster.isOnSameTeam(result.entityHit)) {
            spawn = result.entityHit.getPositionVector();
        } else {
            RayTraceResult blockHit = RayTracer.standardBlockRayTrace(world, caster, range, false, false, false);
            if (blockHit != null && blockHit.typeOfHit == RayTraceResult.Type.BLOCK && blockHit.hitVec != null) {
                spawn = blockHit.hitVec;
            } else {
                Vec3d eye = caster.getPositionEyes(1.0f);
                Vec3d look = caster.getLookVec();
                spawn = eye.add(look.scale(range));
            }
            spawn = this.moveToGround(world, spawn);
        }

        float radius = this.getProperty(Spell.EFFECT_RADIUS).floatValue();
        int duration = (int) (this.getProperty(Spell.DURATION).floatValue() * modifiers.get(SpellModifiers.POTENCY));

        EntityDisplacementCage cage = new EntityDisplacementCage(world, caster,
                spawn.x, spawn.y, spawn.z, radius, duration);
        world.spawnEntity(cage);

        this.playSound(world, caster, 0, 1, modifiers);
        return true;
    }

    private Vec3d moveToGround(World world, Vec3d start) {

        Vec3d pos = start;
        int up = 0;
        while (up < GROUND_SEARCH_UP
                && !world.isAirBlock(new BlockPos(pos.x, pos.y, pos.z))) {
            pos = pos.add(0, 1, 0);
            up++;
        }

        Vec3d down = pos.subtract(0, GROUND_SEARCH_DOWN, 0);
        RayTraceResult hit = world.rayTraceBlocks(pos, down);
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            return hit.hitVec;
        }
        return down;
    }
}
