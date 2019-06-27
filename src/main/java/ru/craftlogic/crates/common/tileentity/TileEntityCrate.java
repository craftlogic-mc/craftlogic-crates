package ru.craftlogic.crates.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.ListInventoryManager;
import ru.craftlogic.api.tile.TileEntityBase;

public class TileEntityCrate extends TileEntityBase implements InventoryHolder {
    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    public TileEntityCrate(World world, IBlockState state) {
        super(world, state);
    }

    @Override
    public InventoryManager getInventoryManager() {
        return new ListInventoryManager(this.items);
    }
}
