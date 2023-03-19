package absolutelyaya.ultracraft.client.rendering.entity.feature;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Random;

public class EnragedModel<T extends LivingEntity> extends AnimalModel<T>
{
	private final ModelPart Root;
	private final ModelPart[] lightnightParts;
	private final Random rand;
	private final float[] lightningOpacities;
	
	public EnragedModel(ModelPart root)
	{
		Root = root.getChild("Main");
		rand = new Random();
		lightningOpacities = new float[3];
		lightnightParts = new ModelPart[3];
		lightnightParts[0] = root.getChild("L1");
		lightnightParts[1] = root.getChild("L2");
		lightnightParts[2] = root.getChild("L3");
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("Main",
				ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.001F,
						new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		modelPartData.addChild("L1",
				ModelPartBuilder.create().uv(32, 0).cuboid(-8.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.001F,
						new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, -0.03F));
		modelPartData.addChild("L2",
				ModelPartBuilder.create().uv(0, 16).cuboid(-8.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.001F,
						new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, -0.03F));
		modelPartData.addChild("L3",
				ModelPartBuilder.create().uv(32, 16).cuboid(-8.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.001F,
						new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, -0.03F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	
	@Override
	protected Iterable<ModelPart> getHeadParts()
	{
		return List.of();
	}
	
	@Override
	protected Iterable<ModelPart> getBodyParts()
	{
		return List.of();
	}
	
	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
	{
	
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha)
	{
		if(rand.nextFloat() > 0.99)
			lightningOpacities[rand.nextInt(3)] = 1.3f;
		Root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
		
		for (int i = 0; i < 2; i++)
		{
			lightningOpacities[i] -= 0.01;
			float a = MathHelper.clamp(lightningOpacities[i], 0f, 1f);
			if(a > 0f)
			{
				float c = MathHelper.clamp(lightningOpacities[i] - 0.1f, 0f, 1f);
				lightnightParts[i].render(matrices, vertices, light, overlay, 1f, c, c, a);
			}
		}
	}
}
