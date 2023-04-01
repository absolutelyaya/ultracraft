package absolutelyaya.ultracraft.client.rendering.entity.other;

import absolutelyaya.ultracraft.entity.other.InterruptableCharge;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

// Made with Blockbench 4.6.5
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class InterruptableChargeModel extends EntityModel<InterruptableCharge>
{
	private final ModelPart bb_main;
	
	public InterruptableChargeModel(ModelPart root)
	{
		this.bb_main = root.getChild("bb_main");
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("bb_main",
				ModelPartBuilder.create().uv(0, 0)
						.cuboid(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F,
								new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		return TexturedModelData.of(modelData, 16, 16);
	}
	
	@Override
	public void setAngles(InterruptableCharge entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
	
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha)
	{
		bb_main.render(matrices, vertexConsumer, 15728880, overlay, red, green, blue, alpha);
	}
}
