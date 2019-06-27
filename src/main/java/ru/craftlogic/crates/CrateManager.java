package ru.craftlogic.crates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.server.PlayerManager;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.World;
import ru.craftlogic.common.command.CommandManager;
import ru.craftlogic.crates.common.entity.EntitySupplyCrate;
import ru.craftlogic.crates.network.message.MessageConfig;
import ru.craftlogic.crates.network.message.MessageCrateSpawn;
import ru.craftlogic.crates.util.Crate;

import java.nio.file.Path;
import java.util.*;

public class CrateManager extends ConfigurableManager implements ForgeChunkManager.LoadingCallback {
    public static final Logger LOGGER = LogManager.getLogger("CrateManager");

    private boolean enabled;
    private int announceTime;
    private Set<String> allowedWorlds = new HashSet<>();
    private Crate nextCrate;
    private int countdown;

    public CrateManager(Server server, Path settingsDirectory) {
        super(server, settingsDirectory.resolve("crates.json"), LOGGER);
        ForgeChunkManager.setForcedChunkLoadingCallback(CraftCrates.INSTANCE, this);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    protected void load(JsonObject config) {
        this.allowedWorlds.clear();
        this.nextCrate = null;
        this.countdown = 0;

        this.enabled = JsonUtils.getBoolean(config, "enabled", true);
        this.announceTime = JsonUtils.getInt(config, "announce_time", 10 * 20);

        if (config.has("allowed_worlds")) {
            for (JsonElement e : JsonUtils.getJsonArray(config, "allowed_worlds")) {
                this.allowedWorlds.add(e.getAsString());
            }
        } else {
            this.allowedWorlds.add("overworld");
        }

        if (this.enabled) {
            CraftCrates.REGISTRY.clear();
            if (config.has("crates")) {
                for (Map.Entry<String, JsonElement> e : config.getAsJsonObject("crates").entrySet()) {
                    String name = e.getKey();
                    CraftCrates.REGISTRY.put(name, Crate.fromJson(name, e.getValue().getAsJsonObject()));
                }
            }
            CraftCrates.NETWORK.broadcast(new MessageConfig());
        }
    }

    @Override
    protected void save(JsonObject config) {
        config.addProperty("enabled", this.enabled);

        /*JsonObject crates = new JsonObject();

        for (Crate crate : CraftCrates.REGISTRY.values()) {
            crates.add(crate.getName(), crate.toJson());
        }

        config.add("crates", crates);*/
    }

    @Override
    public void registerCommands(CommandManager commandManager) {
        commandManager.registerCommandContainer(CraftCratesCommands.class);
    }

    public int getAnnounceTime() {
        return announceTime;
    }

    public void scheduleDrop(Crate crate, int countdown) {
        this.nextCrate = crate;
        this.countdown = countdown;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        CraftCrates.NETWORK.sendTo(event.player, new MessageConfig());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        PlayerManager playerManager = this.server.getPlayerManager();
        if (event.phase == TickEvent.Phase.START && event.side == Side.SERVER && !playerManager.getAllOnline().isEmpty()) {
            if (nextCrate == null) {
                nextCrate = CraftCrates.getRandomCrate(new Random());
                countdown = nextCrate != null ? nextCrate.getSpawnCooldown() : 0;
            }
            if (nextCrate != null && countdown > 0) {
                if (--countdown == announceTime) {
                    server.broadcastCountdown(
                        "supply_crate",
                        Text.translation("tooltip.supply_crate.scheduled"),
                        announceTime / 20,
                        0xFF555555,
                        CraftSounds.WARNING
                    );
                } else if (countdown == 0) {
                    spawnInAllAllowedWorlds(nextCrate);
                    nextCrate = null;
                }
            }
        }
    }

    private void spawnInAllAllowedWorlds(Crate crate) {
        for (String world : allowedWorlds) {
            World w = server.getWorldManager().get(world);
            spawnInWorld(w, crate);
        }
    }

    private void spawnInWorld(World world, Crate crate) {
        Random random = world.getRandom();
        WorldBorder border = world.getBorder();
        int count = 1 + random.nextInt(crate.getMaxPerTime());
        System.out.println("Spawning " + count + " crates in world " + world.getName());
        for (int i = 0; i < count; i++) {
            double radius = border.getDiameter() / 2.0;
            double x = border.getCenterX() + random.nextDouble() * radius;
            double z = border.getCenterZ() + random.nextDouble() * radius;
            int blockX = MathHelper.floor(x);
            int blockZ = MathHelper.floor(z);
            Chunk chunk = world.unwrap().getChunkFromChunkCoords(blockX >> 4, blockZ >> 4);
            double y = chunk.getHeightValue(blockX & 15, blockZ & 15);
            double topY = world.unwrap().getHeight();

            EntitySupplyCrate c = new EntitySupplyCrate(world.unwrap(), x, topY, z, crate);

            CraftCrates.NETWORK.broadcastInWorld(world.getDimension(),
                new MessageCrateSpawn(c.getUniqueID(), x, y, z, c.getLifetime())
            );

            world.unwrap().spawnEntity(c);
        }
    }

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, net.minecraft.world.World world) {
        for (ForgeChunkManager.Ticket ticket : tickets) {
            Entity entity = ticket.getEntity();
            if (entity instanceof EntitySupplyCrate) {
                ((EntitySupplyCrate) entity).loadChunks(ticket);
            }
        }
    }
}
