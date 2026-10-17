package legacyspell.entity.mob;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import legacyspell.registry.LegacyPotions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class EntitySpectralAerwhale extends EntityFlying {

    public static final double SPEED = 0.5;
    public static final double PATH_LENGTH = 44;
    public static final double START_BEHIND = 10;
    public static final double HEIGHT = 6;

    public static final int SONG_INTERVAL = 10;
    public static final float SONG_DEPTH = 14f;
    public static final int BLESSING_TICKS = 80;
    private static final int FADE_OUT = 16;
    private static final int SLOW_TICKS = 30;

    private static final double BOB_AMPLITUDE = 0.02;
    private static final double BOB_PERIOD = 0.15;

    private static final DataParameter<Integer> LIFETIME =
            EntityDataManager.createKey(EntitySpectralAerwhale.class, DataSerializers.VARINT);
    private static final DataParameter<Float> HEADING_X =
            EntityDataManager.createKey(EntitySpectralAerwhale.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> HEADING_Z =
            EntityDataManager.createKey(EntitySpectralAerwhale.class, DataSerializers.FLOAT);

    private int ownerId = -1;
    private double headingX;
    private double headingZ;
    private float damage;
    private float songRadius = 4f;
    private double speed = SPEED;
    private int lifetime;

    public EntitySpectralAerwhale(World world) {
        super(world);
        this.setSize(3.0f, 3.0f);
        this.noClip = true;
        this.setNoGravity(true);
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
    }

    public EntitySpectralAerwhale(World world, EntityLivingBase owner, Vec3d heading, int lifetime) {
        this(world);
        if (owner != null) this.ownerId = owner.getEntityId();
        Vec3d flat = new Vec3d(heading.x, 0, heading.z);
        double len = flat.length();
        if (len < 1.0E-4) {
            this.headingX = 0;
            this.headingZ = 1;
        } else {
            this.headingX = flat.x / len;
            this.headingZ = flat.z / len;
        }
        this.lifetime = lifetime;
        this.dataManager.set(LIFETIME, lifetime);
        this.dataManager.set(HEADING_X, (float) this.headingX);
        this.dataManager.set(HEADING_Z, (float) this.headingZ);
    }

    public static int getLifetime() {
        return (int) (PATH_LENGTH / SPEED);
    }

    public void configure(float damage, float songRadius, double speed) {
        this.damage = damage;
        this.songRadius = songRadius;
        this.speed = speed;
    }

    public float getSongRadius() {
        return this.songRadius;
    }

    public int getLifetimeTicks() {
        if (this.world != null && this.world.isRemote) {
            return Math.max(this.dataManager.get(LIFETIME), 1);
        }
        return this.lifetime > 0 ? this.lifetime : getLifetime();
    }

    public double getHeadingX() {
        if (this.world != null && this.world.isRemote) return this.dataManager.get(HEADING_X);
        return this.headingX;
    }

    public double getHeadingZ() {
        if (this.world != null && this.world.isRemote) return this.dataManager.get(HEADING_Z);
        return this.headingZ;
    }

    public float getFade(float partialTick) {
        float age = this.ticksExisted + partialTick;
        int total = this.getLifetimeTicks();
        return clamp(age / 12f, 0f, 1f) * clamp((total - age) / FADE_OUT, 0f, 1f);
    }

    public EntityLivingBase getOwner() {
        Entity entity = this.ownerId == -1 ? null : this.world.getEntityByID(this.ownerId);
        return entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LIFETIME, getLifetime());
        this.dataManager.register(HEADING_X, 0f);
        this.dataManager.register(HEADING_Z, 1f);
    }

    @Override
    protected void initEntityAI() {
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
    }

    @Override
    public void onUpdate() {

        super.onUpdate();

        if (this.world.isRemote) {
            this.spawnTrailParticles();
            return;
        }

        this.motionX = this.headingX * this.speed;
        this.motionZ = this.headingZ * this.speed;
        this.motionY = Math.cos(this.ticksExisted * BOB_PERIOD) * BOB_AMPLITUDE;

        this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

        float yaw = (float) Math.toDegrees(Math.atan2(this.headingZ, this.headingX));
        this.prevRotationYaw = yaw;
        this.rotationYaw = yaw;
        float pitch = (float) Math.sin(this.ticksExisted * BOB_PERIOD) * 6.0f;
        this.prevRotationPitch = pitch;
        this.rotationPitch = pitch;

        if (this.ticksExisted >= 6 && this.ticksExisted % SONG_INTERVAL == 0
                && this.ticksExisted < this.getLifetimeTicks() - FADE_OUT / 2) {
            this.sing();
        }

        if (this.ticksExisted >= this.getLifetimeTicks()) {
            this.setDead();
        }
    }

    private void sing() {

        EntityLivingBase owner = this.getOwner();
        Vec3d centre = this.getPositionVector();

        double groundY = this.findGroundY(centre);
        AxisAlignedBB area = new AxisAlignedBB(
                centre.x - this.songRadius, groundY - 1, centre.z - this.songRadius,
                centre.x + this.songRadius, centre.y + 1.5, centre.z + this.songRadius);

        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, area);
        for (Entity entity : entities) {
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase target = (EntityLivingBase) entity;
            if (!target.isEntityAlive()) continue;
            if (target instanceof EntityPlayer && ((EntityPlayer) target).isSpectator()) continue;
            double dx = target.posX - centre.x;
            double dz = target.posZ - centre.z;
            if (dx * dx + dz * dz > this.songRadius * this.songRadius) continue;

            boolean ally = owner != null && (target == owner || owner.isOnSameTeam(target));

            if (ally) {
                target.addPotionEffect(new PotionEffect(LegacyPotions.whale_blessing, BLESSING_TICKS, 0));
                target.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, BLESSING_TICKS, 1));
            } else {
                boolean hurt = target.attackEntityFrom(
                        MagicDamage.causeIndirectMagicDamage(this, owner, DamageType.SHOCK), this.damage);
                if (hurt) {
                    target.motionX *= 0.6;
                    target.motionY = Math.max(target.motionY, 0.55);
                    target.motionZ *= 0.6;
                    target.velocityChanged = true;
                    target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, SLOW_TICKS, 0));
                    for (int i = 0; i < 6; i++) {
                        this.world.spawnParticle(EnumParticleTypes.CLOUD,
                                target.posX + (this.rand.nextDouble() - 0.5) * 0.6,
                                target.posY + 0.3 + this.rand.nextDouble() * 0.4,
                                target.posZ + (this.rand.nextDouble() - 0.5) * 0.6,
                                0, 0.05, 0);
                    }
                }
            }
        }

        this.spawnSongParticles(centre, groundY);

        if (this.ticksExisted % (SONG_INTERVAL * 2) == 0) {
            this.world.playSound(null, centre.x, centre.y, centre.z,
                    SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.PLAYERS, 2.5f,
                    0.75f + this.rand.nextFloat() * 0.35f);
        }
    }

    private double findGroundY(Vec3d centre) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int startY = (int) Math.floor(centre.y);
        for (int i = 0; i <= (int) SONG_DEPTH; i++) {
            int cy = startY - i;
            pos.setPos((int) Math.floor(centre.x), cy, (int) Math.floor(centre.z));
            if (!this.world.isAirBlock(pos)) return cy + 1;
        }
        return centre.y - SONG_DEPTH;
    }

    private void spawnSongParticles(Vec3d centre, double groundY) {

        Vec3d ground = new Vec3d(centre.x, groundY, centre.z);

        for (int i = 0; i < 20; i++) {
            double angle = 2.0 * Math.PI * i / 20;
            double px = ground.x + Math.cos(angle) * this.songRadius * 0.5;
            double pz = ground.z + Math.sin(angle) * this.songRadius * 0.5;
            this.world.spawnParticle(EnumParticleTypes.PORTAL, px, ground.y + 0.2, pz,
                    (this.rand.nextDouble() - 0.5) * 0.1, 0.05, (this.rand.nextDouble() - 0.5) * 0.1);
        }

        int steps = (int) Math.max(4, (centre.y - groundY) * 1.5);
        for (int i = 0; i < steps; i++) {
            double t = (double) i / steps;
            this.world.spawnParticle(EnumParticleTypes.NOTE,
                    centre.x, centre.y - (centre.y - groundY) * t, centre.z,
                    this.rand.nextFloat() * 24, 0, 0);
        }

        Vec3d head = centre.add(new Vec3d(this.getHeadingX(), 0, this.getHeadingZ()).scale(2.2)).add(0, 1.2, 0);
        for (int i = 0; i < 3; i++) {
            this.world.spawnParticle(EnumParticleTypes.NOTE, head.x, head.y + i * 0.3, head.z,
                    this.rand.nextFloat() * 24, 0, 0);
        }
    }

    private void spawnTrailParticles() {
        float fade = this.getFade(0);
        if (fade <= 0.05f) return;

        double backX = this.posX - this.getHeadingX() * 2.5;
        double backZ = this.posZ - this.getHeadingZ() * 2.5;

        if (this.rand.nextFloat() < fade) {
            this.world.spawnParticle(EnumParticleTypes.CLOUD,
                    backX + (this.rand.nextDouble() - 0.5) * 2,
                    this.posY + 0.8 + (this.rand.nextDouble() - 0.5),
                    backZ + (this.rand.nextDouble() - 0.5) * 2,
                    0, 0.01, 0);
        }
        for (int i = 0; i < 2; i++) {
            this.world.spawnParticle(EnumParticleTypes.PORTAL,
                    this.posX + (this.rand.nextDouble() - 0.5) * 4,
                    this.posY + this.rand.nextDouble() * 2.5,
                    this.posZ + (this.rand.nextDouble() - 0.5) * 4,
                    0, 0.01, 0);
        }
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return true;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.ownerId = compound.getInteger("OwnerId");
        this.headingX = compound.getDouble("HeadingX");
        this.headingZ = compound.getDouble("HeadingZ");
        this.damage = compound.getFloat("Damage");
        this.songRadius = compound.getFloat("SongRadius");
        this.speed = compound.getDouble("Speed");
        this.lifetime = compound.getInteger("Lifetime");
        this.ticksExisted = compound.getInteger("Age");
        this.dataManager.set(LIFETIME, this.lifetime);
        this.dataManager.set(HEADING_X, (float) this.headingX);
        this.dataManager.set(HEADING_Z, (float) this.headingZ);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("OwnerId", this.ownerId);
        compound.setDouble("HeadingX", this.headingX);
        compound.setDouble("HeadingZ", this.headingZ);
        compound.setFloat("Damage", this.damage);
        compound.setFloat("SongRadius", this.songRadius);
        compound.setDouble("Speed", this.speed);
        compound.setInteger("Lifetime", this.lifetime);
        compound.setInteger("Age", this.ticksExisted);
    }
}
