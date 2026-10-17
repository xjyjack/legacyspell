package legacyspell.spell;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import legacyspell.Reference;
import legacyspell.entity.projectile.EntityEarthEdge;
import legacyspell.entity.projectile.EntityGaiaPortal;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpellGaiaTreasury extends Spell {

    private static final int BURST_INTERVAL = 5;
    private static final int MAX_CHANNEL_TICKS = 200;
    private static final double TARGETING_RANGE = 32;
    private static final float BASE_SPAWN_RADIUS = 3f;
    private static final float SPAWN_RADIUS_SCALE = 3f;
    private static final float HAIL_HEIGHT = 4f;
    private static final double TARGETED_DAMAGE_SHARE = 0.5;

    public SpellGaiaTreasury() {
        super(Reference.MOD_ID, "gaia_treasury", EnumAction.BOW, true);
        addProperties(Spell.DAMAGE);
        soundValues(0.8f, 1.0f, 0.2f);
    }

    @Override
    public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {

        if (world.isRemote) return true;

        if (ticksInUse >= MAX_CHANNEL_TICKS) {
            caster.stopActiveHand();
            return true;
        }

        if (ticksInUse % BURST_INTERVAL == 0) {
            this.doBurst(world, caster, null, modifiers);
        }

        return true;
    }

    @Override
    public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers) {

        if (world.isRemote) return true;

        if (ticksInUse % BURST_INTERVAL == 0) {
            this.doBurst(world, caster, target, modifiers);
        }

        return true;
    }

    private void doBurst(World world, EntityLivingBase caster, EntityLivingBase presetTarget, SpellModifiers modifiers) {

        float potency = modifiers.get(SpellModifiers.POTENCY);
        float baseDamage = this.getProperty(Spell.DAMAGE).floatValue() * potency;
        int blades = (int) (1.5f * potency) + 1;
        float radius = this.getSpawnRadius(potency);

        EntityLivingBase targeted = presetTarget;
        if (targeted == null || targeted == caster || caster.isOnSameTeam(targeted)) {
            RayTraceResult hit = RayTracer.standardEntityRayTrace(world, caster, TARGETING_RANGE, false);
            if (hit != null && hit.typeOfHit == RayTraceResult.Type.ENTITY
                    && hit.entityHit instanceof EntityLivingBase) {
                targeted = (EntityLivingBase) hit.entityHit;
            } else {
                targeted = null;
            }
        }
        if (targeted == caster) targeted = null;

        if (targeted != null) {
            for (int i = 0; i < blades / 2; i++) {
                this.spawnTargetedGate(world, caster, targeted, baseDamage * (float) TARGETED_DAMAGE_SHARE, BURST_INTERVAL + i);
            }
        } else if (caster.isSneaking()) {
            for (int i = 0; i < blades; i++) {
                this.spawnHailGate(world, caster, baseDamage, radius);
            }
        } else {
            for (int i = 0; i < blades; i++) {
                this.spawnRandomGate(world, caster, baseDamage, radius, BURST_INTERVAL + i);
            }
        }
    }

    private float getSpawnRadius(float potency) {
        return (float) (0.5f + SPAWN_RADIUS_SCALE * Math.sqrt(potency * 15.0 * 0.25 / Math.PI));
    }

    private void spawnTargetedGate(World world, EntityLivingBase caster, EntityLivingBase target,
                                   float damage, int delay) {

        float radius = BASE_SPAWN_RADIUS + Math.max(target.width, target.height);
        double angle = Math.random() * 2.0 * Math.PI;
        Vec3d spawnPos = new Vec3d(
                target.posX + Math.cos(angle) * radius,
                target.posY + Math.random() * radius,
                target.posZ + Math.sin(angle) * radius);

        Vec3d centre = new Vec3d(target.posX, target.posY + target.height / 2.0, target.posZ);
        Vec3d aim = centre.subtract(spawnPos).normalize();

        this.spawnGate(world, caster, spawnPos, aim, damage, delay, false);
    }

    private void spawnRandomGate(World world, EntityLivingBase caster, float damage, float radius, int delay) {

        double degree = Math.random() * Math.PI * 1.5 - Math.PI * 0.25;
        double dist = (Math.random() * 3.0 + 1.0) * radius / BASE_SPAWN_RADIUS;

        double xOffset = Math.cos(degree) * dist;
        double yOffset = Math.sin(degree) * dist + 2.0;

        double cosPsi = Math.cos(Math.toRadians(caster.rotationYaw));
        double sinPsi = Math.sin(Math.toRadians(caster.rotationYaw));
        double cosTheta = Math.cos(Math.toRadians(caster.rotationPitch));
        double sinTheta = Math.sin(Math.toRadians(caster.rotationPitch));

        Vec3d origin = new Vec3d(
                caster.posX + xOffset * cosPsi - yOffset * sinTheta * sinPsi,
                caster.posY + yOffset * cosTheta,
                caster.posZ + xOffset * sinPsi + yOffset * sinTheta * cosPsi);

        this.spawnGate(world, caster, origin, caster.getLookVec(), damage, delay, false);
    }

    private void spawnHailGate(World world, EntityLivingBase caster, float damage, float radius) {

        double degree = Math.random() * Math.PI * 1.5 - Math.PI * 0.25;
        double dist = (Math.random() * 3.0 + 1.0) * radius / BASE_SPAWN_RADIUS;

        double xOffset = Math.cos(degree) * dist;
        double yOffset = Math.sin(degree) * dist + HAIL_HEIGHT;

        double cosPsi = Math.cos(Math.toRadians(caster.rotationYaw));
        double sinPsi = Math.sin(Math.toRadians(caster.rotationYaw));
        double cosTheta = Math.cos(Math.toRadians(-90.0));
        double sinTheta = Math.sin(Math.toRadians(-90.0));

        Vec3d origin = new Vec3d(
                caster.posX + xOffset * cosPsi - yOffset * sinTheta * sinPsi,
                caster.posY + yOffset * cosTheta + 1.0,
                caster.posZ + xOffset * sinPsi + yOffset * sinTheta * cosPsi);

        this.spawnGate(world, caster, origin, new Vec3d(0, 1, 0), damage, BURST_INTERVAL, true);
    }

    private void spawnGate(World world, EntityLivingBase caster, Vec3d pos, Vec3d aim,
                           float damage, int delay, boolean homing) {

        EntityGaiaPortal gate = new EntityGaiaPortal(world, caster, delay);
        gate.setPosition(pos.x, pos.y, pos.z);
        gate.setAim(aim);
        gate.setDamage(damage);
        gate.setBladeHoming(homing);
        world.spawnEntity(gate);
    }
}
