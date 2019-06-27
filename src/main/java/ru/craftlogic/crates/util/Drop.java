package ru.craftlogic.crates.util;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.crates.common.entity.EntitySupplyCrate;

public abstract class Drop {
    public abstract String getName();
    public abstract void drop(EntitySupplyCrate crate, EntityPlayer player);
    public abstract void toJson(JsonObject json);
    public abstract void toBuffer(AdvancedBuffer buffer);
}
