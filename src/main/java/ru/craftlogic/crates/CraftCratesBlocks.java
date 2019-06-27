package ru.craftlogic.crates;

import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.crates.common.block.BlockCrate;

public class CraftCratesBlocks {
    public static Block CRATE;

    static void init(Side side) {
        CRATE = CraftBlocks.registerBlock(new BlockCrate());
    }
}
