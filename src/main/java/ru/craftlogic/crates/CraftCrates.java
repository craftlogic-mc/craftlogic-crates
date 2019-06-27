package ru.craftlogic.crates;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.network.AdvancedNetwork;
import ru.craftlogic.crates.common.ProxyCommon;
import ru.craftlogic.crates.util.Crate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Mod(modid = CraftCrates.MOD_ID, version = CraftCrates.VERSION, dependencies = "required-after:" + CraftAPI.MOD_ID)
public class CraftCrates {
    public static final String MOD_ID = CraftAPI.MOD_ID + "-crates";
    public static final String VERSION = "0.2.0-BETA";

    @Mod.Instance
    public static CraftCrates INSTANCE;
    @SidedProxy(clientSide = "ru.craftlogic.crates.client.ProxyClient", serverSide = "ru.craftlogic.crates.common.ProxyCommon")
    public static ProxyCommon PROXY;
    public static final AdvancedNetwork NETWORK = new AdvancedNetwork(MOD_ID);
    public static final Map<String, Crate> REGISTRY = new HashMap<>();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PROXY);

        CraftCratesDrops.init(event.getSide());
        CraftCratesSounds.init(event.getSide());
        CraftCratesBlocks.init(event.getSide());
        CraftCratesEntities.init(event.getSide());
        PROXY.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NETWORK.openChannel();
        PROXY.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PROXY.postInit();
    }

    public static Crate getRandomCrate(Random random) {
        int totalWeight = getTotalCrateWeight();
        if (!REGISTRY.isEmpty()) {
            while (true) {
                for (Crate variant : REGISTRY.values()) {
                    if (random.nextFloat() >= (float)variant.getWeight() / (float)totalWeight) {
                        return variant;
                    }
                }
            }
        }
        return null;
    }

    public static int getTotalCrateWeight() {
        int result = 0;
        for (Crate variant : REGISTRY.values()) {
            result += variant.getWeight();
        }
        return result;
    }
}
