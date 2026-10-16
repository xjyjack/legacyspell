package legacyspell.potion;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import legacyspell.LegacySpell;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class PotionAcrobatics extends Potion {

    public PotionAcrobatics() {
        super(false, 0x99ccff);
        this.setRegistryName(LegacySpell.MODID, "acrobatics");
        this.setPotionName("potion." + LegacySpell.MODID + ".acrobatics");
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase living, int amplifier) {
        World world = living.world;
        Vec3d movement = new Vec3d(living.motionX, living.motionY, living.motionZ);

        Vec3d from = living.getPositionVector();
        Vec3d to = new Vec3d(from.x + movement.x * 2.0, from.y + movement.y * 2.0, from.z + movement.z * 2.0);
        AxisAlignedBB swept = new AxisAlignedBB(from, to).grow(0.5, 0.5, 0.5)
                .union(living.getEntityBoundingBox().grow(0.5, 0.5, 0.5));

        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(living, swept);
        Entity hitTarget = null;
        for (Entity e : list) {
            if (e instanceof EntityLivingBase) {
                hitTarget = e;
                break;
            }
        }

        boolean hit = hitTarget != null;

        if (!world.isRemote) {
            if (hit) {
                EntityLivingBase target = (EntityLivingBase) hitTarget;
                target.attackEntityFrom(MagicDamage.causeIndirectMagicDamage(living, living, DamageType.FORCE), (float) amplifier);
                target.hurtResistantTime = 20;

                double explosionRadius = 4.0;
                double explosionRadiusSqr = explosionRadius * explosionRadius;
                for (Entity e : world.getEntitiesWithinAABBExcludingEntity(living, living.getEntityBoundingBox().grow(explosionRadius, explosionRadius, explosionRadius))) {
                    if (e == hitTarget || !(e instanceof EntityLivingBase)) continue;
                    double distSqr = living.getDistanceSq(e.posX, e.posY, e.posZ);
                    if (distSqr < explosionRadiusSqr) {
                        float aoeDamage = (float) ((double) amplifier * (1.0 - distSqr / explosionRadiusSqr) * 0.5);
                        ((EntityLivingBase) e).attackEntityFrom(MagicDamage.causeIndirectMagicDamage(living, living, DamageType.FORCE), aoeDamage);
                    }
                }

                living.motionY = 1.2;
                living.velocityChanged = true;
                world.playSound(null, living.posX, living.posY + 1.0, living.posZ, SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, living.getSoundCategory(), 4.0f, 1.2f);
            } else {
                living.fallDistance = 0.0f;
            }
        }

        if (world.isRemote) {
            double px = living.posX;
            double py = living.posY + 1.0;
            double pz = living.posZ;
            if (hit) {
                for (int i = 0; i < 5; i++) {
                    world.spawnParticle(EnumParticleTypes.CLOUD,
                            px + (Math.random() - 0.5) * 0.6, py + (Math.random() - 0.5) * 0.6, pz + (Math.random() - 0.5) * 0.6,
                            0.0, 0.1, 0.0);
                }
                world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, px, py, pz, 0.0, 0.0, 0.0);
            } else {
                world.spawnParticle(EnumParticleTypes.CLOUD, px, py, pz, 0.0, 0.05, 0.0);
            }
        }
    }
}
