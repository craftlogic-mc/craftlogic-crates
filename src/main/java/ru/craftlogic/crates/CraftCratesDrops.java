package ru.craftlogic.crates;

import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.util.CheckedFunction;
import ru.craftlogic.crates.util.Drop;
import ru.craftlogic.crates.util.DropEntity;
import ru.craftlogic.crates.util.DropLootTable;
import ru.craftlogic.crates.util.DropMoney;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CraftCratesDrops {
    private static final Map<String, CheckedFunction<AdvancedBuffer, ? extends Drop, IOException>> FROM_BUFFER = new HashMap<>();
    private static final Map<String, Function<JsonObject, ? extends Drop>> FROM_JSON = new HashMap<>();

    static void init(Side side) {
        registerDrop("loot_table", DropLootTable::new, DropLootTable::new);
        registerDrop("money", DropMoney::new, DropMoney::new);
        registerDrop("entity", DropEntity::new, DropEntity::new);
    }

    public static <D extends Drop> void registerDrop(String type, CheckedFunction<AdvancedBuffer, D, IOException> fromBuffer, Function<JsonObject, D> fromJson) {
        FROM_BUFFER.put(type, fromBuffer);
        FROM_JSON.put(type, fromJson);
    }

    public static Drop fromBuffer(AdvancedBuffer buffer) throws IOException {
        String type = buffer.readString(Short.MAX_VALUE);
        CheckedFunction<AdvancedBuffer, ? extends Drop, IOException> result = FROM_BUFFER.get(type);
        if (result != null) {
            return result.apply(buffer);
        }
        throw new IllegalArgumentException("Unknown drop type: " + type);
    }

    public static Drop fromJson(JsonObject json) {
        String type = JsonUtils.getString(json, "type");
        Function<JsonObject, ? extends Drop> result = FROM_JSON.get(type);
        if (result != null) {
            return result.apply(json);
        }
        throw new IllegalArgumentException("Unknown drop type: " + type);
    }
}
