package absolutelyaya.ultracraft.client.rendering.entity.other;

import absolutelyaya.ultracraft.entity.other.ShockwaveEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

// Made with Blockbench 4.6.5
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class ShockwaveModel extends EntityModel<ShockwaveEntity>
{
	private final ModelPart bb_main;
	
	public ShockwaveModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		bb_main.addChild("cube_r1", ModelPartBuilder.create().uv(0, -13).cuboid(0.0F, 0.0F, -11.0F, 0.0F, 9.0F, 22.0F, new Dilation(0.0F)), ModelTransform.of(8.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.1745F));
		bb_main.addChild("cube_r2", ModelPartBuilder.create().uv(0, -22).cuboid(0.0F, -9.0F, -11.0F, 0.0F, 9.0F, 22.0F, new Dilation(0.0F)), ModelTransform.of(8.0F, -8.0F, 0.0F, 0.0F, 0.0F, -0.1745F));
		bb_main.addChild("cube_r3", ModelPartBuilder.create().uv(0, -13).cuboid(0.0F, 0.0F, -11.0F, 0.0F, 9.0F, 22.0F, new Dilation(0.0F)), ModelTransform.of(-8.0F, -8.0F, 0.0F, 0.0F, 0.0F, -0.1745F));
		bb_main.addChild("cube_r4", ModelPartBuilder.create().uv(0, -22).cuboid(0.0F, -9.0F, -11.0F, 0.0F, 9.0F, 22.0F, new Dilation(0.0F)), ModelTransform.of(-8.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.1745F));
		bb_main.addChild("cube_r5", ModelPartBuilder.create().uv(0, 9).cuboid(-11.0F, 0.0F, 0.0F, 22.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -8.0F, 8.0F, -0.1745F, 0.0F, 0.0F));
		bb_main.addChild("cube_r6", ModelPartBuilder.create().uv(0, 0).cuboid(-11.0F, -9.0F, 0.0F, 22.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -8.0F, 8.0F, 0.1745F, 0.0F, 0.0F));
		bb_main.addChild("cube_r7", ModelPartBuilder.create().uv(0, 0).cuboid(-11.0F, -9.0F, 0.0F, 22.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -8.0F, -8.0F, -0.1745F, 0.0F, 0.0F));
		bb_main.addChild("cube_r8", ModelPartBuilder.create().uv(0, 9).cuboid(-11.0F, 0.0F, 0.0F, 22.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -8.0F, -8.0F, 0.1745F, 0.0F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	
	@Override
	public void setAngles(ShockwaveEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
	{
	
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha)
	{
		bb_main.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}
}
