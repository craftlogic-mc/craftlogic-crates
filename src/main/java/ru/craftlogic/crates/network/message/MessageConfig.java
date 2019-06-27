package ru.craftlogic.crates.network.message;

import net.minecraft.item.EnumDyeColor;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.network.AdvancedNetwork;
import ru.craftlogic.crates.CraftCrates;
import ru.craftlogic.crates.CraftCratesDrops;
import ru.craftlogic.crates.util.Crate;
import ru.craftlogic.crates.util.Drop;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MessageConfig extends AdvancedMessage {
    private Map<String, Crate> registry;

    public MessageConfig() {}

    @Override
    public AdvancedNetwork getNetwork() {
        return CraftCrates.NETWORK;
    }

    @Override
    protected void read(AdvancedBuffer buffer) throws IOException {
        int count = buffer.readInt();
        this.registry = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            String name = buffer.readString(Short.MAX_VALUE);
            int c = buffer.readInt();
            Set<Drop> drops = new HashSet<>(c);
            for (int j = 0; j < c; j++) {
                drops.add(CraftCratesDrops.fromBuffer(buffer));
            }
            this.registry.put(name, new Crate(name, drops,
                buffer.readItemStack(),
                EnumDyeColor.byMetadata(buffer.readInt()),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt()
            ));
        }
    }

    @Override
    protected void write(AdvancedBuffer buffer) throws IOException {
        Map<String, Crate> registry = CraftCrates.REGISTRY;
        buffer.writeInt(registry.size());
        for (Crate crate : registry.values()) {
            buffer.writeString(crate.getName());
            Set<Drop> drops = crate.getAllPossibleDrops();
            buffer.writeInt(drops.size());
            for (Drop drop : drops) {
                buffer.writeString(drop.getName());
                drop.toBuffer(buffer);
            }
            buffer.writeItemStack(crate.getIcon());
            buffer.writeInt(crate.getParachuteColor().getMetadata());
            buffer.writeInt(crate.getWeight());
            buffer.writeInt(crate.getLifetime());
            buffer.writeInt(crate.getSpawnCooldown());
            buffer.writeInt(crate.getMaxPerTime());
        }
    }

    public Map<String, Crate> getRegistry() {
        return registry;
    }
}
