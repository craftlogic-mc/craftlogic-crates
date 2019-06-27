package ru.craftlogic.crates;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.crates.util.Crate;

import java.util.HashSet;
import java.util.Set;

public class CraftCratesCommands implements CommandRegistrar {
    @Command(name = "crates", syntax = {
        "reload",
        "drop <crate:Crate>",
        "drop <crate:Crate> <countdown>"
    })
    public static void commandCrates(CommandContext ctx) throws CommandException {
        CrateManager manager = ctx.server().getManager(CrateManager.class);
        if (!manager.isEnabled()) {
            throw new CommandException("Crates are disabled!");
        }
        switch (ctx.action(0)) {
            case "reload": {
                try {
                    manager.load();
                    ctx.sendMessage(Text.string("Reload successful").green());
                } catch (Throwable t) {
                    t.printStackTrace();
                    throw new CommandException("Reload failed! See console for more details.");
                }
                break;
            }
            case "drop": {
                String type = ctx.get("crate").asString();
                Crate crate = CraftCrates.REGISTRY.get(type);
                if (crate != null) {
                    boolean delayed = ctx.has("countdown");
                    int countdown = delayed ? ctx.get("countdown").asInt(0, Integer.MAX_VALUE) : manager.getAnnounceTime() + 1;

                    manager.scheduleDrop(crate, countdown);

                    if (!delayed) {
                        ctx.sendMessage(Text.string("Scheduled immediate '" + type + "' crate drop"));
                    } else {
                        ctx.sendMessage(Text.string("Scheduled '" + type + "' crate drop in ").append(CraftMessages.parseDuration(countdown * 50)));
                    }
                } else {
                    throw new CommandException("Unknown crate: " + type);
                }
                break;
            }
        }
    }

    @ArgumentCompleter(type = "Crate")
    public static Set<String> completerCrate(ArgumentCompletionContext ctx) throws CommandException {
        CrateManager manager = ctx.server().getManager(CrateManager.class);
        if (!manager.isEnabled()) {
            throw new CommandException("Crates are disabled!");
        }
        return new HashSet<>(CraftCrates.REGISTRY.keySet());
    }
}
