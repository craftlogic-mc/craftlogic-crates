package ru.craftlogic.crates;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;

import static ru.craftlogic.api.CraftSounds.registerSound;

public class CraftCratesSounds {
    public static SoundEvent CRATE_REVEAL, CRATE_OPEN, CRATE_BREAK, CRATE_DROP_PARACHUTE, CRATE_DISAPPEAR, CRATE_TICK;

    static void init(Side side) {
        CRATE_REVEAL = registerSound("create_reveal");
        CRATE_OPEN = registerSound("crate_open");
        CRATE_BREAK = registerSound("crate_break");
        CRATE_DROP_PARACHUTE = registerSound("crate_drop_parachute");
        CRATE_DISAPPEAR = registerSound("crate_disappear");
        CRATE_TICK = registerSound("crate_tick");
    }
}
