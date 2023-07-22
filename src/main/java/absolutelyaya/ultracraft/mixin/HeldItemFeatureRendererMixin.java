package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.item.SwordsmachinePlushieItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererMixin<T extends LivingEntity, M extends EntityModel<T> & ModelWithArms> extends FeatureRenderer<T, M>
{
	@Shadow protected abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
	
	@Shadow @Final private HeldItemRenderer heldItemRenderer;
	
	public HeldItemFeatureRendererMixin(FeatureRendererContext<T, M> context)
	{
		super(context);
	}
	
	@Redirect(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/HeldItemFeatureRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
	void onRender(HeldItemFeatureRenderer<T, M> instance, LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		if((!entity.getMainArm().equals(arm) && stack.getItem() instanceof SwordsmachinePlushieItem &&
				   !(entity.getMainArm().equals(Arm.LEFT) && !entity.getStackInHand(Hand.MAIN_HAND).isEmpty())) &&
				   (entity.getPose().equals(EntityPose.CROUCHING) || entity.getPose().equals(EntityPose.STANDING)))
		{
			matrices.push();
			Vec3d rot = new Vec3d(Math.toRadians(20), Math.toRadians(-80), Math.toRadians(215));
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)rot.x, 1, 0, 0)));
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)rot.y, 0, 1, 0)));
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)rot.z, 0, 0, 1)));
			Vec3d offset = new Vec3d(0.15 - (MinecraftClient.getInstance().player.isSneaking() ? 0.2 : 0), -0.4 - (entity.isSneaking() ? 0.2 : 0), -0.1);
			matrices.translate(offset.x, offset.y, offset.z);
			matrices.scale(1.75f, 1.75f, 1.75f);
			renderItem(entity, stack, matrices, vertexConsumers, light);
			matrices.pop();
		}
		else
			renderItem(entity, stack, transformationMode, arm, matrices, vertexConsumers, light);
	}
	
	protected void renderItem(LivingEntity entity, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		if (!stack.isEmpty()) {
			matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
			heldItemRenderer.renderItem(entity, stack, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, false, matrices, vertexConsumers, light);
			matrices.pop();
		}
	}
}
