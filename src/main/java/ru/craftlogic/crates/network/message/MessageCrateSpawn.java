package ru.craftlogic.crates.network.message;

import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.network.AdvancedNetwork;
import ru.craftlogic.crates.CraftCrates;
import ru.craftlogic.util.ReflectiveUsage;

import java.io.IOException;
import java.util.UUID;

public class MessageCrateSpawn extends AdvancedMessage {
    private UUID id;
    private double x, y, z;
    private long despawnTime;

    @ReflectiveUsage
    public MessageCrateSpawn() {}

    public MessageCrateSpawn(UUID id, double x, double y, double z, int lifetime) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.despawnTime = System.currentTimeMillis() + lifetime * 50L;
    }

    @Override
    public AdvancedNetwork getNetwork() {
        return CraftCrates.NETWORK;
    }


    @Override
    protected void read(AdvancedBuffer buffer) throws IOException {
        this.id = buffer.readUniqueId();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.despawnTime = buffer.readLong();
    }

    @Override
    protected void write(AdvancedBuffer buffer) throws IOException {
        buffer.writeUniqueId(this.id);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
        buffer.writeLong(this.despawnTime);
    }

    public UUID getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public long getDespawnTime() {
        return despawnTime;
    }
}
