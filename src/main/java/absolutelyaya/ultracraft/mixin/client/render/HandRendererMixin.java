package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.client.rendering.entity.feature.ArmFeature;
import absolutelyaya.ultracraft.components.player.IArmComponent;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.item.PlushieItem;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HandRendererMixin
{
	@Shadow public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
	
	@Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;
	
	@Shadow @Final private MinecraftClient client;
	
	@Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
	void onRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
	{
		LivingEntityAccessor playerAccessor = ((LivingEntityAccessor)player);
		if(hand == Hand.OFF_HAND && (playerAccessor.IsPunching() || !item.isEmpty()))
		{
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			float swing = playerAccessor.getPunchProgress(Ultracraft.isTimeFrozen() || MinecraftClient.getInstance().isPaused() ? 0f : tickDelta);
			if(item.getItem() instanceof AbstractWeaponItem)
				matrices.translate(0f, -0.2f, 0f);
			matrices.push();
			PlayerEntityRenderer renderer = (PlayerEntityRenderer)entityRenderDispatcher.getRenderer(client.player);
			PlayerEntityModel<?> model = renderer.getModel();
			boolean right = player.getMainArm().equals(Arm.RIGHT);
			
			boolean arm, sleeve;
			if(right)
			{
				arm = model.leftArm.visible;
				sleeve = model.leftSleeve.visible;
			}
			else
			{
				arm = model.rightArm.visible;
				sleeve = model.rightSleeve.visible;
			}
			setArmVisibility(model, right ? Arm.LEFT : Arm.RIGHT, true, true);
			
			positionPunchArm(matrices, equipProgress, !right, !item.isEmpty()); //TODO: switch on active arm
			
			IArmComponent arms = UltraComponents.ARMS.get(player);
			VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(ArmFeature.getTexture(arms.getActiveArm(), player.getModel().equals("slim"))));
			if(right)
			{
				model.leftArm.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV);
				model.leftSleeve.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV);
			}
			else
			{
				model.rightArm.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV);
				model.rightSleeve.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV);
			}
			setArmVisibility(model, right ? Arm.LEFT : Arm.RIGHT, arm, sleeve);
			matrices.push();
			boolean transform = true;
			int flip = right ? 1 : -1;
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110 * flip));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-130));
			matrices.translate(0.5 * flip, 0.45, -0.45);
			matrices.scale(0.75f, 0.75f, 0.75f);
			if(item.isOf(ItemRegistry.BLUE_SKULL) || item.isOf(ItemRegistry.RED_SKULL))
			{
				matrices.translate(0, 0.4, -0.01 * flip);
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20f * flip));
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-40f));
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-50f));
			}
			else if(item.getItem() instanceof AbstractWeaponItem && !item.isOf(ItemRegistry.SOAP))
			{
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-85f * flip));
				matrices.translate(0.15 * flip, 0.05, -0.1);
			}
			else if (item.getItem() instanceof PlushieItem)
			{
				matrices.translate(-0.2 * flip, 0.25, 0.1);
			}
			else if (item.getItem() instanceof BlockItem)
			{
				matrices.translate(-0.1 * flip, 0.2, 0.125);
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f * flip));
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(5.0f));
			}
			//else if(item.getItem() instanceof ToolItem)
			//{
			//	matrices.translate(((float)flip * x - 1.15f * (right ? 1 : -1)), y - 1.75f, z - 1f);
			//	matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(35f), new Vector3f(1, 0, 0))));
			//	matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(180f), new Vector3f(0, 0, 1))));
			//}
			//else
			//{
			//	matrices.translate(((float)flip * x) - 0.35f * (right ? 1 : -1) + (right ? 0 : 0.05), y + 0.05f + (right ? 0 : -0.05), z - 0.9f + (right ? 0 : 0.25));
			//	matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(-20), new Vector3f(right ? 1 : 0, 0, 0))));
			//	matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(25), new Vector3f(0, right ? 1 : 0, 0))));
			//	matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(15), new Vector3f(0, 0, right ? 1 : -0.5f))));
			//	matrices.scale(0.35f, 0.35f, 0.35f);
			//	transform = false;
			//}
			renderItem(player, item,
					transform ? (right ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND) :
							ModelTransformationMode.NONE, !right, matrices, vertexConsumers, light);
			matrices.pop();
			matrices.pop();
			ci.cancel();
		}
	}
	
	void setArmVisibility(PlayerEntityModel<?> model, Arm side, boolean arm, boolean sleeve)
	{
		if(side.equals(Arm.RIGHT))
		{
			model.rightArm.visible = arm;
			model.rightSleeve.visible = sleeve;
		}
		else
		{
			model.leftArm.visible = arm;
			model.leftSleeve.visible = sleeve;
		}
	}
	
	void positionPunchArm(MatrixStack matrices, float equipProgress, boolean flipped, boolean item)
	{
		if (!(MinecraftClient.getInstance().player instanceof LivingEntityAccessor living))
			return;
		float punch = living.getPunchProgress(MinecraftClient.getInstance().getTickDelta());
		int flip = flipped ? 1 : -1;
		float swing = MathHelper.sqrt(punch);
		float x = -0.2f * Math.min(MathHelper.sin(swing * (float)Math.PI), 0.5f) * 2f;
		float y = 0.3f * (MathHelper.sin(swing * (float)Math.PI));
		float z = -0.4f * Math.min(MathHelper.sin(punch * (float)Math.PI), 0.5f) * 2f;
		matrices.translate(flip * (x + 0.64000005f), y - 0.6f + equipProgress * -0.6f, z - 0.71999997f);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * (45.0f + (item ? 0f : 15f))));
		float roll = MathHelper.sin(punch * punch * (float)Math.PI) / 2f;
		float yaw = MathHelper.sin(swing * (float)Math.PI) / 1.5f;
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * yaw * 30.0f));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flip * roll * -20.0f));
		matrices.translate(flip * -1.0f, 3.6f, 3.5f);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flip * 120.0f));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0f));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip * -135.0f));
		matrices.translate(flip * 5.6f, 0.0f, 0.0f);
	}
}
