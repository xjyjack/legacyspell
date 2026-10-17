package legacyspell.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelJadeSword extends ModelBase {

    private final ModelRenderer forcesword;
    private final ModelRenderer blade;

    public ModelJadeSword() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.forcesword = this.bone(null, 0.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        this.forcesword.cubeList.add(new ModelBox(this.forcesword, 2, 11,
                -1.0F, -0.5F, 8.5F, 2, 1, 4, 0.0F));

        ModelRenderer guard = this.bone(this.forcesword, 0.0F, 0.0F, 7.0F, 0.0F, 45.0F, 0.0F);
        guard.cubeList.add(new ModelBox(guard, 0, 0, -2.0F, -1.0F, -2.0F, 4, 2, 4, 0.0F));
        guard.cubeList.add(new ModelBox(guard, 38, 12, -4.0F, 0.0F, -4.0F, 4, 0, 4, 0.0F));

        ModelRenderer guardFlip = this.bone(this.forcesword, 0.0F, 0.0F, 7.0F, 0.0F, -135.0F, 0.0F);
        guardFlip.cubeList.add(new ModelBox(guardFlip, 50, 12, -4.0F, 0.0F, -4.0F, 4, 0, 4, 0.0F));

        ModelRenderer pommel = this.bone(this.forcesword, 0.0F, -0.5F, 13.25F, 0.0F, 45.0F, 0.0F);
        pommel.cubeList.add(new ModelBox(pommel, 2, 6, -1.5F, 0.0F, -1.5F, 3, 1, 3, 0.0F));

        this.blade = this.bone(this.forcesword, 0.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        ModelRenderer spin = this.bone(this.blade, 0.0F, 0.5F, 4.95F, 0.0F, 45.0F, 0.0F);
        this.segment(spin, -1.5F, -1.5F, 16, 0);
        this.segment(spin, -0.5F, -2.5F, 16, 4);
        this.segment(spin, 0.5F, -3.5F, 16, 8);
        this.segment(spin, 1.5F, -4.5F, 16, 12);
        this.segment(spin, 2.5F, -5.5F, 40, 0);
        this.segment(spin, 3.5F, -6.5F, 40, 4);
        this.segment(spin, 4.5F, -7.5F, 28, 0);
        this.segment(spin, 5.5F, -8.5F, 28, 4);
        this.segment(spin, 6.5F, -9.5F, 28, 8);
        this.segment(spin, 7.5F, -10.5F, 28, 12);
        this.segment(spin, 8.5F, -11.5F, 52, 0);
        this.segment(spin, 9.5F, -12.5F, 52, 4);
        this.segment(spin, 10.5F, -13.5F, 40, 8);
        this.segment(spin, 11.5F, -14.5F, 52, 8);
    }

    private void segment(ModelRenderer parent, float x, float z, int u, int v) {
        parent.cubeList.add(new ModelBox(parent, u, v, x, 0.0F, z, 3, 1, 3, 0.0F));
    }

    private ModelRenderer bone(ModelRenderer parent, float px, float py, float pz,
                               float rx, float ry, float rz) {
        ModelRenderer renderer = new ModelRenderer(this);
        renderer.setTextureSize(64, 64);
        renderer.setRotationPoint(px, py, pz);
        renderer.rotateAngleX = (float) Math.toRadians(rx);
        renderer.rotateAngleY = (float) Math.toRadians(ry);
        renderer.rotateAngleZ = (float) Math.toRadians(rz);
        if (parent != null) {
            parent.addChild(renderer);
        }
        return renderer;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        this.forcesword.render(scale);
    }
}
