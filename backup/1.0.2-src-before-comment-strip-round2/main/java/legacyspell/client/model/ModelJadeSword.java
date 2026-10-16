package legacyspell.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * 玉剑模型 —— 逐 cube 移植自 ISS: Magic From The East 的 {@code jade_sword.geo.json}
 * （贴图 64x64，标识 {@code geometry.non_living_entity}）。
 *
 * <p>Bedrock 几何到 1.12.2 {@link ModelRenderer} 的对应关系（两者是同一套"旧版方块"格式，
 * UV 排布完全一致）：
 * <ul>
 *   <li>bone 的 {@code pivot} → {@link ModelRenderer#setRotationPoint}；</li>
 *   <li>cube 的 {@code origin} 是<b>模型空间绝对坐标</b>，加到父骨架上时要先减去骨架 pivot；</li>
 *   <li>cube 自带的 {@code rotation/pivot} 用一个"只装这一个 cube 的子 ModelRenderer"实现
 *       （旋转绕该 pivot 进行）；</li>
 *   <li>子骨架的 rotationPoint 是相对<b>父骨架 pivot</b> 的坐标。</li>
 * </ul>
 *
 * <p>模型朝向：-Z 为剑尖方向，与 1.12.2 实体的视线约定一致
 * （渲染时 {@code rotate(-yaw,0,1,0); rotate(pitch,1,0,0)} 即可让模型 -Z 指向运动方向）。
 */
public class ModelJadeSword extends ModelBase {

    private final ModelRenderer forcesword;
    private final ModelRenderer blade;

    public ModelJadeSword() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        // ============ forcesword：护手 + 剑柄 + 剑首，pivot [0,3,0] ============
        this.forcesword = this.bone(null, 0.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        // 剑柄（无旋转）：origin [-1,2.5,8.5] size [2,1,4] uv [2,11]
        this.forcesword.cubeList.add(new ModelBox(this.forcesword, 2, 11,
                -1.0F, -0.5F, 8.5F, 2, 1, 4, 0.0F));

        // 护手：绕 [0,3,7] 转 45°（相对 forcesword 的 pivot 为 [0,0,7]）
        ModelRenderer guard = this.bone(this.forcesword, 0.0F, 0.0F, 7.0F, 0.0F, 45.0F, 0.0F);
        // 十字护手本体：origin [-2,2,5] size [4,2,4] uv [0,0]
        guard.cubeList.add(new ModelBox(guard, 0, 0, -2.0F, -1.0F, -2.0F, 4, 2, 4, 0.0F));
        // 护手一角的装饰薄片：origin [-4,3,3] size [4,0,4] uv [38,12]
        guard.cubeList.add(new ModelBox(guard, 38, 12, -4.0F, 0.0F, -4.0F, 4, 0, 4, 0.0F));

        // 护手另一角的装饰薄片：同一个 origin，绕同一 pivot 转 -135°，uv [50,12]
        ModelRenderer guardFlip = this.bone(this.forcesword, 0.0F, 0.0F, 7.0F, 0.0F, -135.0F, 0.0F);
        guardFlip.cubeList.add(new ModelBox(guardFlip, 50, 12, -4.0F, 0.0F, -4.0F, 4, 0, 4, 0.0F));

        // 剑首：绕 [0,2.5,13.25] 转 45°（相对 forcesword 为 [0,-0.5,13.25]）
        ModelRenderer pommel = this.bone(this.forcesword, 0.0F, -0.5F, 13.25F, 0.0F, 45.0F, 0.0F);
        pommel.cubeList.add(new ModelBox(pommel, 2, 6, -1.5F, 0.0F, -1.5F, 3, 1, 3, 0.0F));

        // ============ blade：14 段剑身，pivot [0,2,0]，父骨架 forcesword ============
        this.blade = this.bone(this.forcesword, 0.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        // 14 段共用一个旋转轴 [0,2.5,4.95]（相对 blade 的 pivot 为 [0,0.5,4.95]），
        // 每段绕该轴转 45° 之后正好排成一条直线。
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

    /** 一段 3x1x3 的剑身，坐标已换算到旋转轴 [0,2.5,4.95] 的局部系。 */
    private void segment(ModelRenderer parent, float x, float z, int u, int v) {
        parent.cubeList.add(new ModelBox(parent, u, v, x, 0.0F, z, 3, 1, 3, 0.0F));
    }

    /** 建一个骨架；parent 为 null 时作为根骨架。坐标/角度均为模型单位与角度。 */
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
