package ru.craftlogic.crates.common.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.sound.LoopingSoundSource;
import ru.craftlogic.api.world.Dimension;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.crates.CraftCrates;
import ru.craftlogic.crates.CraftCratesSounds;
import ru.craftlogic.crates.network.message.MessageCrateDestroy;
import ru.craftlogic.crates.util.Crate;
import ru.craftlogic.crates.util.Drop;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntitySupplyCrate extends Entity implements LoopingSoundSource {
    private static final DataParameter<Integer> LIFETIME = EntityDataManager.createKey(EntitySupplyCrate.class, DataSerializers.VARINT);
    private static final DataParameter<String> VARIANT = EntityDataManager.createKey(EntitySupplyCrate.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> HAS_PARACHUTE = EntityDataManager.createKey(EntitySupplyCrate.class, DataSerializers.BOOLEAN);

    private ForgeChunkManager.Ticket ticket;

    public EntitySupplyCrate(World world) {
        super(world);
        this.setSize(0.98F, 0.98F);
    }

    public EntitySupplyCrate(World world, double x, double y, double z, Crate crate) {
        this(world);
        this.setPosition(x, y, z);
        this.dataManager.set(LIFETIME, crate.getLifetime());
        this.dataManager.set(HAS_PARACHUTE, true);
        this.dataManager.set(VARIANT, crate.getName());
    }

    public EntitySupplyCrate(World world, BlockPos pos, Crate crate) {
        this(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, crate);
    }

    @Override
    public Location getLocation() {
        return new Location(this);
    }

    public int getLifetime() {
        return this.dataManager.get(LIFETIME);
    }

    public boolean hasParachute() {
        return this.dataManager.get(HAS_PARACHUTE);
    }

    public Crate getCrate() {
        return CraftCrates.REGISTRY.get(this.dataManager.get(VARIANT));
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return this.getEntityBoundingBox();
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.getEntityBoundingBox();
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public float getEyeHeight() {
        return 0F;
    }

    @Override
    protected void updateFallState(double distance, boolean done, IBlockState blockState, BlockPos blockPos) {
        if (!world.isRemote && done) {
            if (hasParachute()) {
                Location location = getLocation();
                location.playSound(CraftCratesSounds.CRATE_DROP_PARACHUTE, SoundCategory.AMBIENT, 1F, 1F);
                this.dataManager.set(HAS_PARACHUTE, false);
            }
        }
    }

    public void loadChunks(ForgeChunkManager.Ticket ticket) {
        if (this.ticket == null) {
            this.ticket = ticket;
        }
        Set<ChunkPos> loadedChunks = ticket.getChunkList();
        List<ChunkPos> chunks = new ArrayList<>();
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dy = -1; dy <= 1; ++dy) {
                chunks.add(new ChunkPos((int) posX / 16 + dx, (int) posZ / 16 + dy));
            }
        }
        for (ChunkPos chunk : chunks) {
            if (!loadedChunks.contains(chunk)) {
                ForgeChunkManager.forceChunk(ticket, chunk);
            }
        }
        for (ChunkPos chunk : loadedChunks) {
            if (!chunks.contains(chunk)) {
                ForgeChunkManager.unforceChunk(ticket, chunk);
            }
        }
    }

    @Override
    public void setDead() {
        super.setDead();
        if (!world.isRemote) {
            if (ticket != null) {
                ForgeChunkManager.releaseTicket(ticket);
                ticket = null;
            }
            CraftCrates.NETWORK.broadcastInWorld(Dimension.fromVanilla(world.provider.getDimensionType()),
                new MessageCrateDestroy(getUniqueID())
            );
        }
    }

    @Override
    public void onUpdate() {
        if (ticket == null && !world.isRemote) {
            ticket = ForgeChunkManager.requestTicket(CraftCrates.INSTANCE, world, ForgeChunkManager.Type.ENTITY);
            if (ticket != null) {
                ticket.bindEntity(this);
                ticket.setChunkListDepth(9);
                loadChunks(ticket);
            } else {
                System.out.println("Unable to get ticket for crate at " + getLocation());
            }
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (!this.hasNoGravity()) {
            this.motionY -= 0.04;
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.98;
        this.motionY *= hasParachute() ? 0.68 : 0.98;
        this.motionZ *= 0.98;
        Location location = getLocation();
        int lifetime = getLifetime();
        if (lifetime > 0 && getCrate() != null) {
            if (this.onGround) {
                this.dataManager.set(LIFETIME, lifetime - 1);

                this.motionX *= 0.7;
                this.motionZ *= 0.7;
                this.motionY *= -0.5;

                if (!this.world.isRemote) {
                    for (int i = 0; i < 4; ++i) {
                        double ox = this.world.rand.nextFloat();
                        double oy = this.world.rand.nextFloat();
                        double oz = this.world.rand.nextFloat();

                        location.spawnParticle(EnumParticleTypes.PORTAL, ox, oy, oz, -ox, -oy, -oz);
                    }
                }
            }
            this.handleWaterMovement();
        } else {
            location.playSound(CraftCratesSounds.CRATE_DISAPPEAR, SoundCategory.AMBIENT, 1F, 1F);
            location.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, 0, 0, 0, 0, 0, 0);
            this.setDead();
        }
    }

    protected void destroy(EntityPlayer player) {
        Location location = getLocation();
        Crate crate = getCrate();
        if (crate != null) {
            Set<Drop> drops = crate.getAllPossibleDrops();
            if (!drops.isEmpty()) {
                Drop drop = new ArrayList<>(drops).get(player.world.rand.nextInt(drops.size()));
                drop.drop(this, player);
            }
        }
        location.playSound(CraftCratesSounds.CRATE_BREAK, SoundCategory.AMBIENT, 1F, 1F);
        location.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, 0, 0, 0, 0, 0, 0);
        this.setDead();
    }

    @Override
    public boolean isSoundRepeatable(SoundEvent sound) {
        return sound == CraftCratesSounds.CRATE_TICK;
    }

    @Override
    public boolean isSoundActive(SoundEvent sound) {
        return sound == CraftCratesSounds.CRATE_TICK && !hasParachute() && this.onGround && !this.isDead;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> parameter) {
        if (world != null && world.isRemote) {
            if (parameter == HAS_PARACHUTE && !hasParachute()) {
                CraftSounds.playSound(this, CraftCratesSounds.CRATE_TICK, SoundCategory.AMBIENT);
            }
        }
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(LIFETIME, 10 * 60 * 20);
        this.dataManager.register(VARIANT, "");
        this.dataManager.register(HAS_PARACHUTE, true);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound data) {
        if (data.hasKey("lifetime")) {
            this.dataManager.set(LIFETIME, data.getInteger("lifetime"));
        }
        if (data.hasKey("variant")) {
            this.dataManager.set(VARIANT, data.getString("variant"));
        }
        if (data.hasKey("has_parachute")) {
            this.dataManager.set(HAS_PARACHUTE, data.getBoolean("has_parachute"));
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound data) {
        data.setInteger("lifetime", this.dataManager.get(LIFETIME));
        data.setString("variant", this.dataManager.get(VARIANT));
        data.setBoolean("has_parachute", this.dataManager.get(HAS_PARACHUTE));
    }

    @Override
    public boolean hitByEntity(Entity entity) {
        if (!this.world.isRemote && entity instanceof EntityPlayer) {
            this.destroy((EntityPlayer) entity);
        }
        return true;
    }
}
