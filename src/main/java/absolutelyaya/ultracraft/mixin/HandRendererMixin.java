package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.item.PlushieItem;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HandRendererMixin
{
	@Shadow protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);
	
	@Shadow public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
	
	@Shadow protected abstract void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress);
	
	@Shadow protected abstract void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress);
	
	@Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
	void onRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
	{
		LivingEntityAccessor playerAccessor = ((LivingEntityAccessor)player);
		if(hand == Hand.OFF_HAND && (playerAccessor.IsPunching() || !item.isEmpty()))
		{
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			float swing = playerAccessor.GetPunchProgress(Ultracraft.isTimeFrozen() ? 0f : tickDelta);
			if(item.getItem() instanceof AbstractWeaponItem)
				matrices.translate(0f, -0.2f, 0f);
			matrices.push();
			renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swing, player.getMainArm().getOpposite());
			matrices.pop();
			matrices.push();
			boolean transform = true;
			boolean right = player.getMainArm() == Arm.RIGHT;
			float x = 0.8f * MathHelper.sin((float)(MathHelper.sqrt(swing) * Math.PI));
			float y = 0.2f * MathHelper.sin((float)(MathHelper.sqrt(swing) * Math.PI * 2));
			float z = -0.2f * MathHelper.sin((float)(swing * Math.PI));
			int o = right ? 1 : -1;
			if(item.isOf(ItemRegistry.BLUE_SKULL) || item.isOf(ItemRegistry.RED_SKULL))
				matrices.translate(((float)o * x) - 0.1 * (right ? 1 : -1), y + 0.4f, z - 0.4f);
			else if(item.getItem() instanceof AbstractWeaponItem)
			{
				matrices.translate(((float)o * x) - 0.1 * (right ? 1 : -1), y - 0.25, z - 0.5);
				matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(25f), new Vector3f(1, 0, 0))));
			}
			else if (item.getItem() instanceof PlushieItem)
			{
				matrices.translate(((float)o * x - 0.05 * (right ? 1 : -1)), y + 0.05, z - 0.15);
				return;
			}
			else if (item.getItem() instanceof BlockItem)
				matrices.translate(((float)o * x) - 0.1 * (right ? 1 : -1), y + 0.15f, z - 0.2f);
			else if(item.getItem() instanceof ToolItem)
			{
				matrices.translate(((float)o * x - 1.15f * (right ? 1 : -1)), y - 1.75f, z - 1f);
				matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(35f), new Vector3f(1, 0, 0))));
				matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(180f), new Vector3f(0, 0, 1))));
			}
			else
			{
				matrices.translate(((float)o * x) - 0.35f * (right ? 1 : -1) + (right ? 0 : 0.05), y + 0.05f + (right ? 0 : -0.05), z - 0.9f + (right ? 0 : 0.25));
				matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(-20), new Vector3f(right ? 1 : 0, 0, 0))));
				matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(25), new Vector3f(0, right ? 1 : 0, 0))));
				matrices.multiply(new Quaternionf(new AxisAngle4f().set((float)Math.toRadians(15), new Vector3f(0, 0, right ? 1 : -0.5f))));
				matrices.scale(0.35f, 0.35f, 0.35f);
				transform = false;
			}
			applyEquipOffset(matrices, player.getMainArm().getOpposite(), item.getItem() instanceof ToolItem ? 1f - equipProgress : equipProgress);
			applySwingOffset(matrices, player.getMainArm().getOpposite(), swing);
			renderItem(player, item,
					transform ? (right ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND) :
							ModelTransformationMode.NONE, !right, matrices, vertexConsumers, light);
			matrices.pop();
			ci.cancel();
		}
	}
}
