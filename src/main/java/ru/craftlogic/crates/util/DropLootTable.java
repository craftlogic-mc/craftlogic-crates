package ru.craftlogic.crates.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.crates.common.entity.EntitySupplyCrate;

public class DropLootTable extends Drop {
    private final ResourceLocation name;

    public DropLootTable(ResourceLocation name) {
        this.name = name;
    }

    public DropLootTable(AdvancedBuffer buffer) {
        this(buffer.readResourceLocation());
    }

    public DropLootTable(JsonObject json) {
        this(new ResourceLocation(JsonUtils.getString(json, "name")));
    }

    @Override
    public String getName() {
        return "loot_table";
    }

    @Override
    public void drop(EntitySupplyCrate crate, EntityPlayer player) {
        LootTable table = crate.world.getLootTableManager().getLootTableFromLocation(this.name);
        LootContext.Builder ctxBuilder = new LootContext.Builder((WorldServer)crate.world)
                .withLootedEntity(crate)
                .withPlayer(player)
                .withLuck(player.getLuck())
                .withDamageSource(DamageSource.causePlayerDamage(player));

        for (ItemStack item : table.generateLootForPools(crate.world.rand, ctxBuilder.build())) {
            crate.entityDropItem(item, 0F);
        }
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("name", name.toString());
    }


    @Override
    public void toBuffer(AdvancedBuffer buffer) {
        buffer.writeResourceLocation(name);
    }
}
