package legacyspell.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelJadeLoong extends ModelBase {

    private static final int LINEAR = 0;
    private static final int EASE_IN_SINE = 1;
    private static final int EASE_OUT_SINE = 2;

    private final ModelRenderer loong;
    private final ModelRenderer connect;
    private final ModelRenderer head;
    private final ModelRenderer jaw;
    private final ModelRenderer nose;
    private final ModelRenderer moustache1;
    private final ModelRenderer moustache2;
    private final ModelRenderer body2;

    public ModelJadeLoong() {
        this.textureWidth = 128;
        this.textureHeight = 128;

        this.loong = this.bone(null, 0.0F, 1.0F, -12.0F, 0.0F, 0.0F, 0.0F);

        this.head = this.bone(this.loong, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        this.head.cubeList.add(new ModelBox(this.head, 0, 0, -5.0F, -1.0F, -4.0F, 10, 12, 14, 0.0F));
        this.head.cubeList.add(new ModelBox(this.head, 60, 52, -4.0F, 11.0F, -4.0F, 0, 6, 19, 0.0F));
        this.head.cubeList.add(new ModelBox(this.head, 60, 52, 4.0F, 11.0F, -4.0F, 0, 6, 19, 0.0F));
        this.head.cubeList.add(new ModelBox(this.head, 65, 85, 0.0F, 10.0F, 5.0F, 0, 5, 5, 0.0F));

        ModelRenderer whiskerL = this.bone(this.head, -6.0F, -1.0F, 5.0F, 0.0F, -25.0F, 0.0F);
        whiskerL.cubeList.add(new ModelBox(whiskerL, 48, 0, 1.0F, 0.0F, -1.0F, 0, 12, 11, 0.0F));
        ModelRenderer whiskerR = this.bone(this.head, 6.0F, -1.0F, 5.0F, 0.0F, 25.0F, 0.0F);
        whiskerR.cubeList.add(new ModelBox(whiskerR, 48, 0, -1.0F, 0.0F, -1.0F, 0, 12, 11, 0.0F));

        this.jaw = this.bone(this.head, 0.0F, 1.0F, -3.0F, 0.0F, 0.0F, 0.0F);
        ModelRenderer jawMain = this.bone(this.jaw, 0.0F, -2.0F, -2.0F, 20.0F, 0.0F, 0.0F);
        jawMain.cubeList.add(new ModelBox(jawMain, 70, 16, -4.0F, 0.0F, -3.0F, 8, 3, 6, 0.0F));
        ModelRenderer chin = this.bone(this.jaw, 0.0F, -4.0F, -2.0F, 25.0F, 0.0F, 0.0F);
        chin.cubeList.add(new ModelBox(chin, 20, 53, 0.0F, -3.0F, -2.0F, 0, 5, 5, 0.0F));

        this.nose = this.bone(this.head, 0.0F, 6.0F, -4.0F, 0.0F, 0.0F, 0.0F);
        this.nose.cubeList.add(new ModelBox(this.nose, 68, 48, -4.0F, -3.0F, -7.0F, 8, 5, 7, 0.0F));

        this.moustache1 = this.bone(this.nose, -4.0F, 1.0F, -4.0F, 0.0F, 0.0F, 0.0F);
        ModelRenderer moustache1Cube = this.bone(this.moustache1, 1.0F, -2.0F, 2.0F, 0.0F, -30.0F, 0.0F);
        moustache1Cube.cubeList.add(new ModelBox(moustache1Cube, 0, 68, -1.0F, 0.0F, -1.0F, 0, 6, 13, 0.0F));

        this.moustache2 = this.bone(this.nose, 4.0F, 1.0F, -2.0F, 0.0F, 0.0F, 0.0F);
        ModelRenderer moustache2Cube = this.bone(this.moustache2, 1.0F, -2.0F, 0.0F, 0.0F, 30.0F, 0.0F);
        moustache2Cube.cubeList.add(new ModelBox(moustache2Cube, 0, 68, -1.0F, 0.0F, -1.0F, 0, 6, 13, 0.0F));

        this.connect = this.bone(this.loong, 0.0F, 4.0F, 10.0F, 0.0F, 0.0F, 0.0F);

        ModelRenderer body1 = this.bone(this.connect, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        body1.cubeList.add(new ModelBox(body1, 46, 26, -5.0F, -5.0F, 0.0F, 10, 10, 12, 0.0F));
        body1.cubeList.add(new ModelBox(body1, 53, 68, 0.0F, 5.0F, 0.0F, 0, 4, 12, 0.0F));

        this.body2 = this.bone(this.connect, 0.0F, 0.0F, 12.0F, 0.0F, 0.0F, 0.0F);
        this.body2.cubeList.add(new ModelBox(this.body2, 0, 26, -5.0F, -5.0F, 0.0F, 10, 10, 13, 0.0F));
        this.body2.cubeList.add(new ModelBox(this.body2, 53, 73, 0.0F, 5.0F, 0.0F, 0, 4, 12, 0.0F));
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        float t = (ageInTicks / 20.0F) % 1.0F;
        if (t < 0.0F) {
            t += 1.0F;
        }

        this.loong.rotateAngleX = (float) Math.toRadians(sample(LOONG_ROT_X, t));
        this.loong.rotationPointY = 1.0F + sample(LOONG_POS_Y, t);

        this.jaw.rotateAngleX = (float) Math.toRadians(sample(JAW_ROT_X, t));
        this.nose.rotateAngleX = (float) Math.toRadians(sample(NOSE_ROT_X, t));
        this.moustache1.rotateAngleY = (float) Math.toRadians(sample(MOUSTACHE1_ROT_Y, t));
        this.moustache2.rotateAngleX = (float) Math.toRadians(sample(MOUSTACHE2_ROT_X, t));
        this.body2.rotateAngleX = (float) Math.toRadians(sample(BODY2_ROT_X, t));
        this.connect.rotateAngleX = (float) Math.toRadians(sample(CONNECT_ROT_X, t));
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.loong.render(scale);
    }

    private static final float[][] LOONG_ROT_X = {
            {0.0F, 13.0F, LINEAR}, {0.5F, -13.0F, EASE_IN_SINE},
            {0.75F, 5.38478F, LINEAR}, {1.0F, 13.0F, EASE_IN_SINE}};
    private static final float[][] LOONG_POS_Y = {
            {0.0F, 1.0F, LINEAR}, {0.5F, -1.0F, LINEAR},
            {0.75F, 0.0F, LINEAR}, {1.0F, 1.0F, LINEAR}};
    private static final float[][] JAW_ROT_X = {
            {0.0F, -15.0F, LINEAR}, {0.5F, -10.0F, LINEAR}, {1.0F, -15.0F, LINEAR}};
    private static final float[][] NOSE_ROT_X = {
            {0.0F, 5.0F, LINEAR}, {0.5F, -5.0F, LINEAR}, {1.0F, 5.0F, LINEAR}};
    private static final float[][] MOUSTACHE1_ROT_Y = {
            {0.0F, -5.0F, LINEAR}, {0.5F, 5.0F, LINEAR}, {1.0F, -5.0F, LINEAR}};
    private static final float[][] MOUSTACHE2_ROT_X = {
            {0.0F, -5.0F, LINEAR}, {0.5F, 5.0F, LINEAR}, {1.0F, -5.0F, LINEAR}};
    private static final float[][] BODY2_ROT_X = {
            {0.0F, -6.5F, LINEAR}, {0.5F, 6.5F, LINEAR}, {1.0F, -6.5F, LINEAR}};
    private static final float[][] CONNECT_ROT_X = {
            {0.0F, -6.5F, LINEAR}, {0.5F, 6.5F, EASE_IN_SINE}, {1.0F, -6.5F, EASE_OUT_SINE}};

    private static float sample(float[][] keys, float t) {
        int n = keys.length;
        if (t <= keys[0][0]) {
            return keys[0][1];
        }
        if (t >= keys[n - 1][0]) {
            return keys[n - 1][1];
        }
        for (int i = 0; i < n - 1; i++) {
            if (t >= keys[i][0] && t <= keys[i + 1][0]) {
                float span = keys[i + 1][0] - keys[i][0];
                if (span <= 0.0F) {
                    return keys[i + 1][1];
                }
                float local = (t - keys[i][0]) / span;
                return keys[i][1] + (keys[i + 1][1] - keys[i][1]) * ease(local, (int) keys[i][2]);
            }
        }
        return keys[n - 1][1];
    }

    private static float ease(float t, int type) {
        switch (type) {
            case EASE_IN_SINE:
                return 1.0F - (float) Math.cos(t * Math.PI / 2.0);
            case EASE_OUT_SINE:
                return (float) Math.sin(t * Math.PI / 2.0);
            default:
                return t;
        }
    }

    private ModelRenderer bone(ModelRenderer parent, float px, float py, float pz,
                               float rx, float ry, float rz) {
        ModelRenderer renderer = new ModelRenderer(this);
        renderer.setTextureSize(128, 128);
        renderer.setRotationPoint(px, py, pz);
        renderer.rotateAngleX = (float) Math.toRadians(rx);
        renderer.rotateAngleY = (float) Math.toRadians(ry);
        renderer.rotateAngleZ = (float) Math.toRadians(rz);
        if (parent != null) {
            parent.addChild(renderer);
        }
        return renderer;
    }
}
