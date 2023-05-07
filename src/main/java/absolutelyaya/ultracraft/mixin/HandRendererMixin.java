package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HandRendererMixin
{
	@Shadow protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);
	
	@Shadow public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
	
	@Shadow protected abstract void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress);
	
	@Shadow protected abstract void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress);
	
	@Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
	void onRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
	{
		LivingEntityAccessor playerAccessor = ((LivingEntityAccessor)player);
		if(hand == Hand.OFF_HAND && (playerAccessor.IsPunching() || !item.isEmpty()))
		{
			float swing = playerAccessor.GetPunchProgress(Ultracraft.isTimeFrozen() ? 0f : tickDelta);
			matrices.push();
			renderArmHoldingItem(matrices, vertexConsumers, light, 0f, swing, player.getMainArm().getOpposite());
			matrices.pop();
			matrices.push();
			boolean right = player.getMainArm() == Arm.RIGHT;
			float x = 0.8f * MathHelper.sin((float)(MathHelper.sqrt(swing) * Math.PI));
			float y = 0.2f * MathHelper.sin((float)(MathHelper.sqrt(swing) * Math.PI * 2));
			float z = -0.2f * MathHelper.sin((float)(swing * Math.PI));
			int o = right ? 1 : -1;
			if(item.isOf(ItemRegistry.BLUE_SKULL) || item.isOf(ItemRegistry.RED_SKULL))
				matrices.translate(((float)o * x) - 0.1, y + 0.4f, z - 0.4f);
			else
				matrices.translate(((float)o * x) - 0.1, y + 0.1f, z - 0.2f);
			applyEquipOffset(matrices, player.getMainArm().getOpposite(), 0f);
			applySwingOffset(matrices, player.getMainArm().getOpposite(), swing);
			renderItem(player, item, right ? ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND : ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND,
					!right, matrices, vertexConsumers, light);
			matrices.pop();
			ci.cancel();
		}
	}
}
