package absolutelyaya.ultracraft.client.rendering.entity.feature;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Function;

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
	
	private final Vec3d[] DashPose = new Vec3d[] {new Vec3d(0.0f, -27.5f, 0.0f), new Vec3d(18.63f, -50.02f, -23.75f), new Vec3d(0.0f, -25.0f, 0.0f), new Vec3d(0.0f, -40.0f, 0.0f), new Vec3d(0.0f, -17.5f, 0.0f), new Vec3d(-7.85f, -31.63f, 14.72f), new Vec3d(0.0f, -10.0f, 0.0f), new Vec3d(-15.0f, -32.0f, 26.82f)};
	private final Vec3d[] RestPose = new Vec3d[] {new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f)};
	private final Vec3d[] SlidePose = new Vec3d[] {new Vec3d(0.0f + 70, -27.5f, 0.0f - 12), new Vec3d(18.63f, -50.02f + 20, -23.75f + 22.5), new Vec3d(0.0f + 70, -25.0f, 0.0f - 18), new Vec3d(0.0f,  -40.0f + 10, 0.0f - 5), new Vec3d(0.0f + 70, -17.5f, 0.0f - 24), new Vec3d(-7.85f, -31.63f + 7.5, 14.72f - 10), new Vec3d(0.0f + 70, -10.0f, 0.0f - 32), new Vec3d(-15.0f, -32.0f + 5, 26.82f - 15)};
	
	public WingsModel(ModelPart root, Function<Identifier, RenderLayer> renderLayerFactory)
	{
		super(renderLayerFactory, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
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
		winged.getWingState();
		setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
	}
	
	Vec3d[] getPoseFromIndex(byte idx)
	{
		return switch(idx) {
			case 0 -> DashPose.clone();
			default -> RestPose.clone(); // 1 = rest! Default being here is intentional.
			case 2 -> SlidePose.clone();
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
		Vec3d[] curPose = getPoseFromIndex((byte)0);
		if(entity instanceof WingedPlayerEntity winged)
		{
			Vec3d[] lastPose = winged.getWingPose();
			Vec3d[] desiredPose = getPoseFromIndex(winged.getWingState());
			
			for (int i = 0; i < 8; i++)
				curPose[i] = lastPose[i].lerp(desiredPose[i], 1f / 60f / getAnimLength(winged.getWingState()));
			winged.setWingPose(curPose);
		}
		
		//Left
		LeftWing1Root.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[0].x, MathHelper.RADIANS_PER_DEGREE * (float)curPose[0].y, MathHelper.RADIANS_PER_DEGREE * (float)curPose[0].z);
		LeftWing1.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[1].x, MathHelper.RADIANS_PER_DEGREE * (float)curPose[1].y, MathHelper.RADIANS_PER_DEGREE * (float)curPose[1].z);
		LeftWing2Root.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[2].x, MathHelper.RADIANS_PER_DEGREE * (float)curPose[2].y, MathHelper.RADIANS_PER_DEGREE * (float)curPose[2].z);
		LeftWing2.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[3].x, MathHelper.RADIANS_PER_DEGREE * (float)curPose[3].y, MathHelper.RADIANS_PER_DEGREE * (float)curPose[3].z);
		LeftWing3Root.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[4].x, MathHelper.RADIANS_PER_DEGREE * (float)curPose[4].y, MathHelper.RADIANS_PER_DEGREE * (float)curPose[4].z);
		LeftWing3.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[5].x, MathHelper.RADIANS_PER_DEGREE * (float)curPose[5].y, MathHelper.RADIANS_PER_DEGREE * (float)curPose[5].z);
		LeftWing4Root.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[6].x, MathHelper.RADIANS_PER_DEGREE * (float)curPose[6].y, MathHelper.RADIANS_PER_DEGREE * (float)curPose[6].z);
		LeftWing4.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[7].x, MathHelper.RADIANS_PER_DEGREE * (float)curPose[7].y, MathHelper.RADIANS_PER_DEGREE * (float)curPose[7].z);
		//Right (left but mirrored)
		RightWing1Root.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[0].x, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[0].y, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[0].z);
		RightWing1.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[1].x, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[1].y, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[1].z);
		RightWing2Root.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[2].x, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[2].y, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[2].z);
		RightWing2.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[3].x, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[3].y, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[3].z);
		RightWing3Root.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[4].x, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[4].y, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[4].z);
		RightWing3.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[5].x, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[5].y, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[5].z);
		RightWing4Root.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[6].x, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[6].y, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[6].z);
		RightWing4.setAngles(MathHelper.RADIANS_PER_DEGREE * (float)curPose[7].x, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[7].y, MathHelper.RADIANS_PER_DEGREE * (float)-curPose[7].z);
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha)
	{
		Root.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}
