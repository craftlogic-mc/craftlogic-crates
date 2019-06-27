package ru.craftlogic.crates.client.render.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import ru.craftlogic.crates.CraftCratesBlocks;
import ru.craftlogic.crates.client.render.model.ModelParachute;
import ru.craftlogic.crates.common.entity.EntitySupplyCrate;
import ru.craftlogic.crates.util.Crate;

import static java.lang.Math.sin;
import static net.minecraft.util.math.MathHelper.clamp;

@SideOnly(Side.CLIENT)
public class RenderSupplyCrate extends Render<EntitySupplyCrate> {
    private final ModelBase parachute = new ModelParachute();

    public RenderSupplyCrate(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.5F;
    }

    @Override
    public void doRender(EntitySupplyCrate crate, double x, double y, double z, float rotation, float partialTicks) {
        float lifetime = crate.getLifetime() - partialTicks + 1F;
        boolean hasParachute = crate.hasParachute();
        Crate variant = crate.getCrate();
        IBlockState state = CraftCratesBlocks.CRATE.getDefaultState();

        BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        GlStateManager.pushMatrix();

        GlStateManager.translate((float)x, (float)y + 0.5F, (float)z);
        if (lifetime < 10F) {
            float size = clamp(1F - lifetime / 10F, 0F, 1F);
            size *= size;
            size *= size;
            float scale = 1F + size * 0.3F;
            GlStateManager.scale(scale, scale, scale);
        }

        GlStateManager.rotate(-90F, 0F, 1F, 0F);
        GlStateManager.translate(-0.5F, -0.5F, 0.5F);


        if (variant != null) {
            GlStateManager.pushMatrix();

            if (hasParachute) {
                EnumDyeColor parachuteColor = variant.getParachuteColor();
                this.bindTexture(new ResourceLocation("textures/blocks/wool_colored_" + parachuteColor.getName() + ".png"));

                GlStateManager.translate(0.5, 2.5, -0.5);

                this.parachute.render(crate, 0F, 0F, 0F, 0F, 0F, 0.0625F);

                this.renderParachuteCords(crate);

                this.bindEntityTexture(crate);
            } else {
                this.bindEntityTexture(crate);

                GlStateManager.translate(0.5, 1.5 + sin(lifetime / 5.0) * 0.25, -0.5);
                GlStateManager.scale(1.5F, 1.5F, 1.5F);
                double angle = clamp((lifetime / 100.0) % 1.0, 0.0, 1.0) * 360.0;
                GlStateManager.rotate((float) angle, 0F, 1F, 0F);

                ItemStack icon = variant.getIcon();
                RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
                renderItem.renderItem(icon, ItemCameraTransforms.TransformType.GROUND);
            }

            GlStateManager.popMatrix();
        }


        blockRenderer.renderBlockBrightness(state, crate.getBrightness());

        GlStateManager.translate(0F, 0F, 1F);
        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(crate));
            blockRenderer.renderBlockBrightness(state, 1F);
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        super.doRender(crate, x, y, z, rotation, partialTicks);
    }

    public void renderParachuteCords(EntitySupplyCrate crate) {
        float b = crate.getBrightness();
        float[] x = new float[]{-8F, 0F, 8F, 0F, -8F, 0F, 8F, 0F, -8F, 0F, 8F, 0F, -8F, 0F, 8F, 0F, -8F, 0F, 8F, 0F, -8F, 0F, 8F, 0F};
        float[] y = new float[]{0.52F, 1.5F, 0.52F, 1.5F, 0.2F, 1.5F, 0.2F, 1.5F, 0.52F, 1.5F, 0.52F, 1.5F, 0.2F, 1.5F, 0.2F, 1.5F, 0.05F, 1.5F, 0.05F, 1.5F, 0.05F, 1.5F, 0.05F, 1.5F};
        float[] z = new float[]{-34F, -3F, -34F, -3F, -20F, -3F, -20F, -3F, 34F, 3F, 34F, 3F, 20F, 3F, 20F, 3F, -8F, -3F, -8F, -3F, 8F, 3F, 8F, 3F};
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.scale(0.0625F, -1F, 0.0625F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for(int k = 0; k < 16; ++k) {
            buffer.pos(x[k], y[k], z[k]).color(b * 0.5F, b * 0.5F, b * 0.65F, 1F).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySupplyCrate crate) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
