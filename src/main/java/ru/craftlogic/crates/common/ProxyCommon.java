package ru.craftlogic.crates.common;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.event.server.ServerAddManagersEvent;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.network.AdvancedMessageHandler;
import ru.craftlogic.crates.CraftCrates;
import ru.craftlogic.crates.CrateManager;
import ru.craftlogic.crates.network.message.MessageConfig;
import ru.craftlogic.crates.network.message.MessageCrateDestroy;
import ru.craftlogic.crates.network.message.MessageCrateSpawn;

public class ProxyCommon extends AdvancedMessageHandler {
    public void preInit() {

    }

    public void init() {
        CraftCrates.NETWORK.registerMessage(this::handleConfig, MessageConfig.class, Side.CLIENT);
        CraftCrates.NETWORK.registerMessage(this::handleCrateSpawn, MessageCrateSpawn.class, Side.CLIENT);
        CraftCrates.NETWORK.registerMessage(this::handleCrateDestroy, MessageCrateDestroy.class, Side.CLIENT);
    }

    public void postInit() {

    }

    protected AdvancedMessage handleConfig(MessageConfig message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleCrateSpawn(MessageCrateSpawn message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleCrateDestroy(MessageCrateDestroy message, MessageContext context) {
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onServerAddManagers(ServerAddManagersEvent event) {
        event.addManager(CrateManager.class, CrateManager::new);
    }
}
