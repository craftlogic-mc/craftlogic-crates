package ru.craftlogic.crates;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.crates.client.render.entity.RenderSupplyCrate;
import ru.craftlogic.crates.common.entity.EntitySupplyCrate;

import static ru.craftlogic.api.CraftEntities.registerEntity;
import static ru.craftlogic.api.CraftEntities.registerEntityRenderer;

public class CraftCratesEntities {
    static void init(Side side) {
        registerEntity(EntitySupplyCrate.class, "supply_crate", 80, 3, true);
        if (side == Side.CLIENT) {
            initClient();
        }
    }

    @SideOnly(Side.CLIENT)
    private static void initClient() {
        registerEntityRenderer(EntitySupplyCrate.class, RenderSupplyCrate::new);
    }
}
