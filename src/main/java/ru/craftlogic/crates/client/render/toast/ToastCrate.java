package ru.craftlogic.crates.client.render.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.text.TextTranslation;
import ru.craftlogic.client.screen.toast.AdvancedToast;

import java.util.UUID;

public class ToastCrate extends AdvancedToast {
    private final UUID id;
    private final double x, y, z;
    private final long despawnTime;
    private boolean destroyed;

    public ToastCrate(UUID id, double x, double y, double z, long despawnTime) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.despawnTime = despawnTime;
    }

    @Override
    public UUID getType() {
        return id;
    }

    public void destroy() {
        this.destroyed = true;
    }

    @Override
    public Visibility draw(GuiToast container, long displayTime) {
        Minecraft client = container.getMinecraft();
        EntityPlayerSP player = client.player;
        if (player != null && !destroyed) {
            Vec3d lookVec = player.getLookVec().normalize();
            Vec3d targetVec = new Vec3d(player.posX - x, player.posY - y, player.posZ - z).normalize();

            float transparency = 1F - (float) ((lookVec.dotProduct(targetVec) + 1.0) / 4.0);

            int distance = MathHelper.floor(Math.sqrt(player.getDistanceSq(x, y, z)));
            client.getTextureManager().bindTexture(TEXTURE_TOASTS);

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();

            GlStateManager.color(1F, 1F, 1F, transparency);

            container.drawTexturedModalRect(0, 0, 0, 96, 160, 32);
            TextTranslation title = Text.translation("tooltip.supply_crate.dropped").arg(distance);
            this.drawText(container, title, 7, 7, 0xff000000);
            long duration = despawnTime - System.currentTimeMillis();
            this.drawText(container, CraftMessages.parseDuration(duration), 7, 18, 0xff555555);
            Gui.drawRect(3, 28, 157, 29, -1);
            float progress = MathHelper.clamp(1F - (float)((double)displayTime / (double) duration), 0F, 1F);

            if (System.currentTimeMillis() < despawnTime) {
                Gui.drawRect(3, 28, (int)(3F + 154F * progress), 29, 0xFF555555);
            }

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();

            return System.currentTimeMillis() < despawnTime ? Visibility.SHOW : Visibility.HIDE;
        } else {
            return Visibility.HIDE;
        }
    }
}
