package ru.craftlogic.crates.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.util.Json2NBT;
import ru.craftlogic.crates.CraftCratesDrops;
import ru.craftlogic.crates.CrateManager;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Crate {
    private final String name;
    private final Set<Drop> drops;
    private final ItemStack icon;
    private final EnumDyeColor parachuteColor;
    private final int weight;
    private final int lifetime;
    private final int spawnCooldown;
    private final int maxPerTime;

    public Crate(String name, Set<Drop> drops, ItemStack icon, EnumDyeColor parachuteColor, int weight, int lifetime, int spawnCooldown, int maxPerTime) {
        this.name = name;
        this.drops = drops;
        this.icon = icon;
        this.parachuteColor = parachuteColor;
        this.weight = weight;
        this.lifetime = lifetime;
        this.spawnCooldown = spawnCooldown;
        this.maxPerTime = maxPerTime;
    }

    public static Crate fromJson(String name, JsonObject json) {
        return new Crate(name,
            parseDrops(JsonUtils.getJsonArray(json, "drops")),
            parseItemStack(json, "icon"),
            parseColor(JsonUtils.getString(json, "parachute_color", "white")),
            JsonUtils.getInt(json, "weight", 1),
            JsonUtils.getInt(json, "lifetime", 10 * 60 * 20),
            JsonUtils.getInt(json, "spawn_cooldown", 2 * 60 * 60 * 20),
            JsonUtils.getInt(json, "max_per_time", 5)
        );
    }

    private static Set<Drop> parseDrops(JsonArray drops) {
        Set<Drop> result = new HashSet<>();
        for (JsonElement d : drops) {
            result.add(CraftCratesDrops.fromJson(d.getAsJsonObject()));
        }
        return result;
    }

    private static EnumDyeColor parseColor(String str) {
        for (EnumDyeColor color : EnumDyeColor.values()) {
            if (color.getName().equals(str)) {
                return color;
            }
        }
        throw new IllegalArgumentException("Illegal color code: " + str);
    }

    private static ItemStack parseItemStack(JsonObject obj, String key) {
        if (obj.get(key).isJsonPrimitive()) {
            Item item = parseItem(obj.get(key));
            return new ItemStack(item);
        } else {
            obj = JsonUtils.getJsonObject(obj, key);
            Item item = parseItem(obj.get("name"));
            if (item != null) {
                int meta = JsonUtils.getInt(obj, "meta", 0);
                ItemStack stack = new ItemStack(item, 1, meta);
                if (!parseNBT(obj, "nbt", stack)) {
                    return ItemStack.EMPTY;
                }
                return stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
    }

    private static Item parseItem(JsonElement e) {
        ResourceLocation type = new ResourceLocation(JsonUtils.getString(e, "name"));
        Item item = Item.REGISTRY.getObject(type);
        if (item == null) {
            CrateManager.LOGGER.error("No such item: " + type);
        }
        return item;
    }

    private static boolean parseNBT(JsonObject obj, String key, ItemStack stack) {
        if (obj.has(key)) {
            JsonElement raw = obj.get(key);
            try {
                stack.setTagCompound((NBTTagCompound) Json2NBT.jsonToNbt(raw));
                return true;
            } catch (IllegalArgumentException exc) {
                CrateManager.LOGGER.error("Failed to parse NBT: " + raw, exc);
                return false;
            }
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public Set<Drop> getAllPossibleDrops() {
        return drops;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public EnumDyeColor getParachuteColor() {
        return parachuteColor;
    }

    public int getWeight() {
        return weight;
    }

    public int getLifetime() {
        return lifetime;
    }

    public int getSpawnCooldown() {
        return spawnCooldown;
    }

    public int getMaxPerTime() {
        return maxPerTime;
    }

    @Override
    public String toString() {
        return "Crate{" +
                "name='" + name + '\'' +
                ", drops=" + drops +
                ", icon=" + icon +
                ", parachuteColor=" + parachuteColor +
                ", weight=" + weight +
                ", lifetime=" + lifetime +
                ", spawnCooldown=" + spawnCooldown +
                ", maxPerTime=" + maxPerTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Crate)) return false;
        Crate crate = (Crate) o;
        return weight == crate.weight &&
                lifetime == crate.lifetime &&
                spawnCooldown == crate.spawnCooldown &&
                maxPerTime == crate.maxPerTime &&
                name.equals(crate.name) &&
                drops.equals(crate.drops) &&
                icon.equals(crate.icon) &&
                parachuteColor == crate.parachuteColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, drops, icon, parachuteColor, weight, lifetime, spawnCooldown, maxPerTime);
    }

    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.addProperty("name", getName());
        JsonArray drops = new JsonArray();
        for (Drop d : getAllPossibleDrops()) {
            JsonObject drop = new JsonObject();
            drop.addProperty("type", d.getName());
            d.toJson(drop);
            drops.add(drop);
        }
        result.add("drops", drops);

        JsonObject i = new JsonObject();
        ItemStack icon = getIcon();
        i.addProperty("name", icon.getItem().getRegistryName().toString());
        i.addProperty("meta", icon.getMetadata());
        if (icon.hasTagCompound()) {
            i.add("nbt", Json2NBT.nbtToJson(icon.getTagCompound()));
        }
        result.add("icon", i);

        result.addProperty("parachute_color", getParachuteColor().getName());
        result.addProperty("weight", getWeight());
        result.addProperty("lifetime", getLifetime());
        result.addProperty("spawn_cooldown", getSpawnCooldown());
        result.addProperty("max_per_time", getMaxPerTime());
        return result;
    }
}
