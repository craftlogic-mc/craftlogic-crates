package ru.craftlogic.crates.network.message;

import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.network.AdvancedNetwork;
import ru.craftlogic.crates.CraftCrates;
import ru.craftlogic.util.ReflectiveUsage;

import java.io.IOException;
import java.util.UUID;

public class MessageCrateDestroy extends AdvancedMessage {
    private UUID id;

    @ReflectiveUsage
    public MessageCrateDestroy() {}

    public MessageCrateDestroy(UUID id) {
        this.id = id;
    }

    @Override
    public AdvancedNetwork getNetwork() {
        return CraftCrates.NETWORK;
    }


    @Override
    protected void read(AdvancedBuffer buffer) throws IOException {
        this.id = buffer.readUniqueId();
    }

    @Override
    protected void write(AdvancedBuffer buffer) throws IOException {
        buffer.writeUniqueId(this.id);
    }

    public UUID getId() {
        return id;
    }
}
