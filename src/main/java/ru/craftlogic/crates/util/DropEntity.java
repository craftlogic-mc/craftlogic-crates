package ru.craftlogic.crates.util;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.util.Json2NBT;
import ru.craftlogic.crates.common.entity.EntitySupplyCrate;

import javax.annotation.Nullable;
import java.io.IOException;

public class DropEntity extends Drop {
    private final ResourceLocation name;
    @Nullable
    private final NBTTagCompound nbt;

    public DropEntity(ResourceLocation name, @Nullable NBTTagCompound nbt) {
        this.name = name;
        this.nbt = nbt;
    }

    public DropEntity(AdvancedBuffer buffer) throws IOException {
        this(buffer.readResourceLocation(), buffer.readCompoundTag());
    }

    public DropEntity(JsonObject json) {
        this(
            new ResourceLocation(JsonUtils.getString(json, "name")),
            json.has("nbt") ? (NBTTagCompound) Json2NBT.jsonToNbt(json.get("nbt")) : null
        );
    }

    @Override
    public String getName() {
        return "entity";
    }

    @Override
    public void drop(EntitySupplyCrate crate, EntityPlayer player) {
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(name);
        if (entry != null) {
            Entity entity = entry.newInstance(crate.world);
            entity.setPosition(crate.posX, crate.posY, crate.posZ);
            if (nbt != null) {
                entity.readFromNBT(nbt);
            }
            crate.world.spawnEntity(entity);
        }
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("name", name.toString());
        if (nbt != null) {
            json.add("nbt", Json2NBT.nbtToJson(nbt));
        }
    }

    @Override
    public void toBuffer(AdvancedBuffer buffer) {
        buffer.writeString(name.toString());
        buffer.writeCompoundTag(nbt);
    }
}
