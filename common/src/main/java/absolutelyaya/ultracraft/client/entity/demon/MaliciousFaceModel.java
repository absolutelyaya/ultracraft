package absolutelyaya.ultracraft.client.entity.demon;

import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

// Made with Blockbench 4.6.3
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class MaliciousFaceModel extends EntityModel<MaliciousFaceEntity>
{
	private final ModelPart bb_main;
	
	public MaliciousFaceModel()
	{
		bb_main = getTexturedModelData().createModel();
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	
	@Override
	public void setAngles(MaliciousFaceEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		bb_main.setAngles((float)Math.toRadians(headPitch), (float)Math.toRadians(netHeadYaw), 0f);
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha)
	{
		matrices.scale(1.5f, 1.5f, 1.5f);
		matrices.translate(0, 0.5, 0);
		bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}
