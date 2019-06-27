package ru.craftlogic.crates.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.crates.CraftCrates;
import ru.craftlogic.crates.client.render.toast.ToastCrate;
import ru.craftlogic.crates.common.ProxyCommon;
import ru.craftlogic.crates.network.message.MessageConfig;
import ru.craftlogic.crates.network.message.MessageCrateDestroy;
import ru.craftlogic.crates.network.message.MessageCrateSpawn;
import ru.craftlogic.crates.util.Crate;
import ru.craftlogic.util.ReflectiveUsage;

import java.util.*;

@ReflectiveUsage
public class ProxyClient extends ProxyCommon {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Set<PendingCrate> crates = new HashSet<>();

    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void postInit() {
        super.postInit();
    }

    @Override
    protected AdvancedMessage handleConfig(MessageConfig message, MessageContext context) {
        syncTask(context, () -> {
            CraftCrates.REGISTRY.clear();
            Map<String, Crate> data = message.getRegistry();
            CraftCrates.REGISTRY.putAll(data);
        });
        return null;
    }

    @Override
    protected AdvancedMessage handleCrateSpawn(MessageCrateSpawn message, MessageContext context) {
        syncTask(context, () ->
            this.crates.add(new PendingCrate(message.getId(), message.getX(), message.getY(), message.getZ(), message.getDespawnTime()))
        );
        return null;
    }

    @Override
    protected AdvancedMessage handleCrateDestroy(MessageCrateDestroy message, MessageContext context) {
        syncTask(context, () -> {
            this.crates.removeIf(c -> c.id.equals(message.getId()));
            GuiToast toasts = this.mc.getToastGui();
            ToastCrate toast = toasts.getToast(ToastCrate.class, message.getId());
            if (toast != null) {
                toast.destroy();
            }
        });
        return null;
    }

    @SubscribeEvent
    protected void onClientTick(TickEvent.ClientTickEvent event) {
        GuiToast toasts = this.mc.getToastGui();
        for (Iterator<PendingCrate> iterator = this.crates.iterator(); iterator.hasNext(); ) {
            PendingCrate crate = iterator.next();
            if (System.currentTimeMillis() < crate.despawnTime) {
                if (toasts.getToast(ToastCrate.class, crate.id) == null) {
                    toasts.add(new ToastCrate(crate.id, crate.x, crate.y, crate.z, crate.despawnTime));
                }
            } else {
                iterator.remove();
            }
        }
    }

    private static class PendingCrate {
        private final UUID id;
        private final double x, y, z;
        private final long despawnTime;

        private PendingCrate(UUID id, double x, double y, double z, long despawnTime) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.despawnTime = despawnTime;
        }
    }
}
