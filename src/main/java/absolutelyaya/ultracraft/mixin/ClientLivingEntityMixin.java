package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class ClientLivingEntityMixin implements LivingEntityAccessor
{
	@Shadow public abstract void swingHand(Hand hand);
	
	@ModifyConstant(method = "tickMovement", constant = @Constant(floatValue = 0.98f))
	float modifySlowdown(float constant)
	{
		if(this instanceof WingedPlayerEntity winged && winged instanceof ClientPlayerEntity)
			return winged.shouldIgnoreSlowdown() ? 1f : constant;
		else
			return constant;
	}
	
	@Inject(method = "getJumpVelocity", at = @At("RETURN"), cancellable = true)
	void onGetJumpVelocity(CallbackInfoReturnable<Float> cir)
	{
		if(this instanceof WingedPlayerEntity winged && winged.isWingsActive())
			cir.setReturnValue(cir.getReturnValue() + 0.1f * Math.max(UltracraftClient.jumpBoost, 0));
	}
	
	@Override
	public boolean punch()
	{
		if((Object)this instanceof OtherClientPlayerEntity || MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson())
			swingHand(Hand.OFF_HAND);
		return false;
	}
	
	@Override
	public int getGravityReduction()
	{
		return UltracraftClient.gravityReduction;
	}
}
