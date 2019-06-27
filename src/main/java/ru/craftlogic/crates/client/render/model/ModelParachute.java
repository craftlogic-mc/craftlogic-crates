package ru.craftlogic.crates.client.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelParachute extends ModelBase {
    public ModelRenderer[] sections = new ModelRenderer[6];

    public ModelParachute() {
        int x = 16;
        int y = 2;
        int z = 16;
        float d2r = (float)Math.toRadians(1.0);
        this.sections[0] = new ModelRenderer(this);
        this.sections[0].addBox(-8F, -8.25F, -5F, x, y, 4);
        this.sections[0].setRotationPoint(0F, 0F, -36F);
        this.sections[0].rotateAngleX = -45F * d2r;
        this.sections[1] = new ModelRenderer(this);
        this.sections[1].addBox(-8F, 1.6F, -32F, x, y, z);
        this.sections[1].rotateAngleX = -15F * d2r;
        this.sections[2] = new ModelRenderer(this);
        this.sections[2].addBox(-8F, -0.5F, -16F, x, y, z);
        this.sections[2].rotateAngleX = -7.5F * d2r;
        this.sections[3] = new ModelRenderer(this);
        this.sections[3].addBox(-8F, -0.5F, 0F, x, y, z);
        this.sections[3].rotateAngleX = 7.5F * d2r;
        this.sections[4] = new ModelRenderer(this);
        this.sections[4].addBox(-8F, 1.6F, 16F, x, y, z);
        this.sections[4].rotateAngleX = 15F * d2r;
        this.sections[5] = new ModelRenderer(this);
        this.sections[5].addBox(-8F, -8.25F, 1F, x, y, 4);
        this.sections[5].setRotationPoint(0F, 0F, 36F);
        this.sections[5].rotateAngleX = 45F * d2r;
    }

    @Override
    public void render(Entity entity, float x, float y, float z, float yaw, float pitch, float scale) {
        for (ModelRenderer pmr : this.sections) {
            pmr.render(scale);
        }
    }
}
