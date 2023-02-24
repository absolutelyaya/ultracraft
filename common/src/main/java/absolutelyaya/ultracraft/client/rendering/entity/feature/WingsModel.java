package absolutelyaya.ultracraft.client.rendering.entity.feature;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

import java.util.List;

// Made with Blockbench 4.6.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class WingsModel<T extends LivingEntity> extends AnimalModel<T>
{
	private final ModelPart Root;
	private final ModelPart LeftWing1Root;
	private final ModelPart LeftWing1;
	private final ModelPart RightWing1Root;
	private final ModelPart RightWing1;
	private final ModelPart LeftWing2Root;
	private final ModelPart LeftWing2;
	private final ModelPart RightWing2Root;
	private final ModelPart RightWing2;
	private final ModelPart LeftWing3Root;
	private final ModelPart LeftWing3;
	private final ModelPart RightWing3Root;
	private final ModelPart RightWing3;
	private final ModelPart LeftWing4Root;
	private final ModelPart LeftWing4;
	private final ModelPart RightWing4Root;
	private final ModelPart RightWing4;
	
	private final Vec3f[] InitialPose = new Vec3f[] {new Vec3f(0.0f, -27.5f, 0.0f), new Vec3f(18.63f, -50.02f, -23.75f), new Vec3f(0.0f, -25.0f, 0.0f), new Vec3f(0.0f, -40.0f, 0.0f), new Vec3f(0.0f, -17.5f, 0.0f), new Vec3f(-7.85f, -31.63f, 14.72f), new Vec3f(0.0f, -10.0f, 0.0f), new Vec3f(-15.0f, -32.0f, 26.82f)};
	private final Vec3f[] RestPose = new Vec3f[] {new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f)};
	private final Vec3f[] DashPose = new Vec3f[] {new Vec3f(-5.87f, 25.32f, 1.04f), new Vec3f(-41.78f, -26.66f, -6.44f), new Vec3f(-5.08f, 2.28f, 0.23f), new Vec3f(-45.48f, -3.48f, 7.06f), new Vec3f(-4.90f, -9.90f, 4.64f), new Vec3f(-41.38f, -2.08f, 5.19f), new Vec3f(-8.91f, -28.04f, 16.49f), new Vec3f(-45.84f, -3.34f, -7.08f)};
	
	private final Vec3f[] CurrentPose = new Vec3f[] {new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(0.0f, 0.0f, 0.0f)};
	
	private byte state;
	private float animProgress;
	
	public WingsModel(ModelPart root)
	{
		this.Root = root.getChild("Root");
		LeftWing1Root = Root.getChild("LeftWing1Root");
		LeftWing1 = LeftWing1Root.getChild("LeftWing1");
		RightWing1Root = Root.getChild("RightWing1Root");
		RightWing1 = RightWing1Root.getChild("RightWing1");
		LeftWing2Root = Root.getChild("LeftWing2Root");
		LeftWing2 = LeftWing2Root.getChild("LeftWing2");
		RightWing2Root = Root.getChild("RightWing2Root");
		RightWing2 = RightWing2Root.getChild("RightWing2");
		LeftWing3Root = Root.getChild("LeftWing3Root");
		LeftWing3 = LeftWing3Root.getChild("LeftWing3");
		RightWing3Root = Root.getChild("RightWing3Root");
		RightWing3 = RightWing3Root.getChild("RightWing3");
		LeftWing4Root = Root.getChild("LeftWing4Root");
		LeftWing4 = LeftWing4Root.getChild("LeftWing4");
		RightWing4Root = Root.getChild("RightWing4Root");
		RightWing4 = RightWing4Root.getChild("RightWing4");
		
		for (int i = 0; i < 8; i++)
		{
			CurrentPose[i] = InitialPose[i].copy();
		}
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData Root = modelPartData.addChild("Root", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0F, -22.0F, 2.0F, 6.0F, 7.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData LeftWing1Root = Root.addChild("LeftWing1Root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -20.0F, 2.0F));
		LeftWing1Root.addChild("cube_r1", ModelPartBuilder.create().uv(16, 18).mirrored().cuboid(-6.7183F, -0.5977F, 0.0F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(6.0F, -4.0F, 2.5F, 0.0F, -0.48F, -0.5236F));

		ModelPartData LeftWing1 = LeftWing1Root.addChild("LeftWing1", ModelPartBuilder.create(), ModelTransform.pivot(6.0F, -4.0F, 3.0F));
		LeftWing1.addChild("cube_r2", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(0.0F, -4.0F, 0.5F, 15.0F, 4.0F, 0.0F, new Dilation(0.0F)).mirrored(false)
		.uv(16, 20).mirrored().cuboid(-2.0F, -4.0F, 0.0F, 3.0F, 5.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(1.0F, -1.0F, -0.5F, 0.0F, 0.0F, -0.3491F));

		ModelPartData RightWing1Root = Root.addChild("RightWing1Root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -20.0F, 2.0F));
		 RightWing1Root.addChild("cube_r3", ModelPartBuilder.create().uv(16, 18).cuboid(-0.2817F, -0.5977F, 0.0F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-6.0F, -4.0F, 2.5F, 0.0F, 0.48F, 0.5236F));

		ModelPartData RightWing1 = RightWing1Root.addChild("RightWing1", ModelPartBuilder.create(), ModelTransform.pivot(-6.0F, -4.0F, 3.0F));
		RightWing1.addChild("cube_r4", ModelPartBuilder.create().uv(0, 0).cuboid(-15.0F, -4.0F, 0.5F, 15.0F, 4.0F, 0.0F, new Dilation(0.0F))
		.uv(16, 20).cuboid(-1.0F, -4.0F, 0.0F, 3.0F, 5.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-1.0F, -1.0F, -0.5F, 0.0F, 0.0F, 0.3491F));

		ModelPartData LeftWing2Root = Root.addChild("LeftWing2Root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -20.0F, 2.0F));
		LeftWing2Root.addChild("cube_r5", ModelPartBuilder.create().uv(16, 18).mirrored().cuboid(-7.001F, -0.0436F, -1.0F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(7.0F, -1.0F, 3.0F, 0.0364F, -0.4086F, -0.0515F));

		ModelPartData LeftWing2 = LeftWing2Root.addChild("LeftWing2", ModelPartBuilder.create(), ModelTransform.pivot(6.5F, -0.5F, 2.5F));
		LeftWing2.addChild("cube_r6", ModelPartBuilder.create().uv(0, 8).mirrored().cuboid(0.0F, -4.0F, 0.0F, 15.0F, 4.0F, 0.0F, new Dilation(0.0F)).mirrored(false)
		.uv(16, 20).mirrored().cuboid(-2.0F, -4.0F, -0.5F, 3.0F, 5.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(1.5F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0436F));

		ModelPartData RightWing2Root = Root.addChild("RightWing2Root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -20.0F, 2.0F));
		RightWing2Root.addChild("cube_r7", ModelPartBuilder.create().uv(16, 18).cuboid(0.001F, -0.0436F, -1.0F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-7.0F, -1.0F, 3.0F, 0.0364F, 0.4086F, 0.0515F));

		ModelPartData RightWing2 = RightWing2Root.addChild("RightWing2", ModelPartBuilder.create(), ModelTransform.pivot(-6.5F, -0.5F, 2.5F));
		RightWing2.addChild("cube_r8", ModelPartBuilder.create().uv(0, 8).cuboid(-15.0F, -4.0F, 0.0F, 15.0F, 4.0F, 0.0F, new Dilation(0.0F))
		.uv(16, 20).cuboid(-1.0F, -4.0F, -0.5F, 3.0F, 5.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-1.5F, -0.5F, 0.0F, 0.0F, 0.0F, -0.0436F));

		ModelPartData LeftWing3Root = Root.addChild("LeftWing3Root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -20.0F, 2.0F));
		LeftWing3Root.addChild("cube_r9", ModelPartBuilder.create().uv(16, 18).mirrored().cuboid(-7.1635F, -0.7418F, -1.0F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(6.0F, 4.5F, 2.5F, -0.109F, -0.3323F, 0.5855F));

		ModelPartData LeftWing3 = LeftWing3Root.addChild("LeftWing3", ModelPartBuilder.create(), ModelTransform.pivot(5.5F, 4.0F, 2.5F));
		LeftWing3.addChild("cube_r10", ModelPartBuilder.create().uv(0, 4).mirrored().cuboid(0.0F, -4.0F, 0.0F, 15.0F, 4.0F, 0.0F, new Dilation(0.0F)).mirrored(false)
		.uv(16, 20).mirrored().cuboid(-2.0F, -4.0F, -0.5F, 3.0F, 5.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(1.5F, 0.0F, -0.5F, 0.0F, 0.0F, 0.2618F));

		ModelPartData RightWing3Root = Root.addChild("RightWing3Root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -20.0F, 2.0F));
		RightWing3Root.addChild("cube_r11", ModelPartBuilder.create().uv(16, 18).cuboid(0.1635F, -0.7418F, -1.0F, 7.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-6.0F, 4.5F, 2.5F, -0.109F, 0.3323F, -0.5855F));

		ModelPartData RightWing3 = RightWing3Root.addChild("RightWing3", ModelPartBuilder.create(), ModelTransform.pivot(-5.5F, 4.0F, 2.5F));
		RightWing3.addChild("cube_r12", ModelPartBuilder.create().uv(0, 4).cuboid(-15.0F, -4.0F, 0.0F, 15.0F, 4.0F, 0.0F, new Dilation(0.0F))
		.uv(16, 20).cuboid(-1.0F, -4.0F, -0.5F, 3.0F, 5.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-1.5F, 0.0F, -0.5F, 0.0F, 0.0F, -0.2618F));

		ModelPartData LeftWing4Root = Root.addChild("LeftWing4Root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -20.0F, 2.0F));
		LeftWing4Root.addChild("cube_r13", ModelPartBuilder.create().uv(14, 16).mirrored().cuboid(-7.81F, -0.9131F, -1.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(4.5F, 8.0F, 2.0F, -0.0193F, -0.2173F, 1.0057F));

		ModelPartData LeftWing4 = LeftWing4Root.addChild("LeftWing4", ModelPartBuilder.create(), ModelTransform.pivot(4.5F, 7.5F, 2.0F));
		LeftWing4.addChild("cube_r14", ModelPartBuilder.create().uv(16, 20).mirrored().cuboid(-2.0F, -4.0F, -1.0F, 3.0F, 5.0F, 1.0F, new Dilation(0.0F)).mirrored(false)
		.uv(0, 12).mirrored().cuboid(0.0F, -4.0F, -0.5F, 13.0F, 4.0F, 0.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(1.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.6545F));

		ModelPartData RightWing4Root = Root.addChild("RightWing4Root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -20.0F, 2.0F));
		RightWing4Root.addChild("cube_r15", ModelPartBuilder.create().uv(14, 16).cuboid(-0.19F, -0.9131F, -1.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.5F, 8.0F, 2.0F, -0.0193F, 0.2173F, -1.0057F));

		ModelPartData RightWing4 = RightWing4Root.addChild("RightWing4", ModelPartBuilder.create(), ModelTransform.pivot(-4.5F, 7.5F, 2.0F));
		RightWing4.addChild("cube_r16", ModelPartBuilder.create().uv(16, 20).cuboid(-1.0F, -4.0F, -1.0F, 3.0F, 5.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 12).cuboid(-13.0F, -4.0F, -0.5F, 13.0F, 4.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-1.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.6545F));
		return TexturedModelData.of(modelData, 32, 32);
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
	
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, WingedPlayerEntity winged)
	{
		state = winged.getWingState();
		if(winged.getWingAnimTime() == 0f)
			animProgress = 0f;
		animProgress = Math.min(MathHelper.lerp(1f / 60f / getAnimLength(state), animProgress, 1f), 1f);
		setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
	}
	
	Vec3f[] getPoseFromIndex(byte idx)
	{
		return switch(idx) {
			case 0 -> InitialPose.clone();
			default -> RestPose.clone(); // 1 = rest! Default being here is intentional.
			case 2 -> DashPose.clone();
		};
	}
	
	float getAnimLength(byte idx)
	{
		return switch(idx) {
			case 0 -> 0.05f;
			case 2 -> 0.08f;
			default -> 0.16f;
		};
	}
	
	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
	{
		Vec3f[] desiredPose = getPoseFromIndex(state);
		
		for (int i = 0; i < 8; i++)
		{
			CurrentPose[i].lerp(desiredPose[i], 1f / 60f / getAnimLength(state));
		}
		
		//Left
		LeftWing1Root.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[0].getX(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[0].getY(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[0].getZ());
		LeftWing1.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[1].getX(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[1].getY(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[1].getZ());
		LeftWing2Root.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[2].getX(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[2].getY(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[2].getZ());
		LeftWing2.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[3].getX(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[3].getY(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[3].getZ());
		LeftWing3Root.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[4].getX(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[4].getY(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[4].getZ());
		LeftWing3.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[5].getX(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[5].getY(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[5].getZ());
		LeftWing4Root.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[6].getX(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[6].getY(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[6].getZ());
		LeftWing4.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[7].getX(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[7].getY(), MathHelper.RADIANS_PER_DEGREE * CurrentPose[7].getZ());
		//Right (left but mirrored)
		RightWing1Root.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[0].getX(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[0].getY(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[0].getZ());
		RightWing1.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[1].getX(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[1].getY(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[1].getZ());
		RightWing2Root.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[2].getX(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[2].getY(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[2].getZ());
		RightWing2.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[3].getX(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[3].getY(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[3].getZ());
		RightWing3Root.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[4].getX(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[4].getY(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[4].getZ());
		RightWing3.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[5].getX(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[5].getY(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[5].getZ());
		RightWing4Root.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[6].getX(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[6].getY(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[6].getZ());
		RightWing4.setAngles(MathHelper.RADIANS_PER_DEGREE * CurrentPose[7].getX(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[7].getY(), MathHelper.RADIANS_PER_DEGREE * -CurrentPose[7].getZ());
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha)
	{
		Root.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}
