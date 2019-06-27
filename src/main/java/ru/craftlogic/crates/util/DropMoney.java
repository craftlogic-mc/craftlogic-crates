package ru.craftlogic.crates.util;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import ru.craftlogic.api.economy.EconomyManager;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.crates.common.entity.EntitySupplyCrate;

import java.util.Random;

public class DropMoney extends Drop {
    private final float min, max;

    public DropMoney(float min, float max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public DropMoney(AdvancedBuffer buffer) {
        this(buffer.readFloat(), buffer.readFloat());
    }

    public DropMoney(JsonObject json) {
        if (json.has("value")) {
            float value = JsonUtils.getFloat(json, "value");
            this.min = value;
            this.max = value;
        } else {
            this.min = JsonUtils.getFloat(json, "min");
            this.max = JsonUtils.getFloat(json, "max");
        }
    }

    @Override
    public String getName() {
        return "money";
    }

    @Override
    public void drop(EntitySupplyCrate crate, EntityPlayer _player) {
        Random random = _player.world.rand;
        Player player = Player.from((EntityPlayerMP) _player);
        Server server = player.getServer();
        EconomyManager economyManager = server.getEconomyManager();
        if (economyManager.isEnabled()) {
            economyManager.give(player, min + random.nextFloat() * (max - min));
        }
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("min", min);
        json.addProperty("max", max);
    }

    @Override
    public void toBuffer(AdvancedBuffer buffer) {
        buffer.writeFloat(min);
        buffer.writeFloat(max);
    }
}
