package ru.craftlogic.crates.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.crates.common.tileentity.TileEntityCrate;

public class BlockCrate extends BlockBase implements TileEntityHolder<TileEntityCrate> {
    public BlockCrate() {
        super(Material.WOOD, "crate", 1F, CreativeTabs.DECORATIONS);
        this.setSoundType(SoundType.WOOD);
    }

    @Override
    public TileEntityInfo<TileEntityCrate> getTileEntityInfo(IBlockState state) {
        return new TileEntityInfo<>(TileEntityCrate.class, state, TileEntityCrate::new);
    }
}
