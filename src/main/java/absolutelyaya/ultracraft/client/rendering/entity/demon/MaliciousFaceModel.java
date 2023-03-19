package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

// Made with Blockbench 4.6.3
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class MaliciousFaceModel<T extends MaliciousFaceEntity> extends EntityModel<T>
{
	private final ModelPart Root;
	private final ModelPart Charge;
	float charge;
	boolean cracked, enraged;
	
	public MaliciousFaceModel(ModelPart root)
	{
		Root = root.getChild("Root");
		Charge = Root.getChild("Charge");
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData Root = modelPartData.addChild("Root", ModelPartBuilder.create().uv(0, 0)
			.cuboid(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		Root.addChild("Charge", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0F,
				new Dilation(0.0F)), ModelTransform.pivot(0.0F, 7.0F, -9.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	
	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
	{
		Root.setAngles((float)Math.toRadians(headPitch), (float)Math.toRadians(headYaw), 0f);
		charge = entity.getChargePercent();
		cracked = entity.isCracked();
		enraged = entity.isEnraged();
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha)
	{
		matrices.push();
		matrices.scale(1.5f, 1.5f, 1.5f);
		matrices.translate(0, 0.5, 0);
		Charge.xScale = Charge.yScale = Charge.zScale = charge * 1.5f;
		float c = enraged ? 0.25f : 1f;
		c = red == 0f ? 1f : c;
		Root.render(matrices, vertexConsumer, light, overlay, 1f, c, c, alpha);
		matrices.pop();
	}
}
