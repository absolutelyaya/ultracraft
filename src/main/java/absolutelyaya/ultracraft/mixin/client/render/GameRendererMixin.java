package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.components.IWingDataComponent;
import absolutelyaya.ultracraft.components.IWingedPlayerComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
	@Shadow @Final MinecraftClient client;
	
	@Shadow @Final private Camera camera;
	
	float slideViewTilt = 0f, lastFovBonus = 0f;
	
	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	public void onBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci)
	{
		if(client.player == null)
			return;
		IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(client.player);
		IWingDataComponent wings = UltraComponents.WING_DATA.get(client.player);
		if(wings.isVisible() && (client.player.isSprinting() || winged.isDashing()))
			ci.cancel();
		if(Ultracraft.isTimeFrozen())
			ci.cancel();
	}
	
	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"))
	void onViewTilt(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci)
	{
		float f = UltracraftClient.getConfigHolder().get().slideTilt;
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if(UltracraftClient.isHiVelEnabled() && player != null && !camera.isThirdPerson() && player.isSprinting() && f > 0)
		{
			float side = MinecraftClient.getInstance().player.input.movementSideways;
			slideViewTilt = MathHelper.lerp(tickDelta, slideViewTilt, f * -side);
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(slideViewTilt), 0f, 0f, 1f)));
		}
	}
	
	@ModifyVariable(method = "tiltViewWhenHurt", at = @At("HEAD"), argsOnly = true)
	float modifyTickDelta(float tickDelta)
	{
		if(Ultracraft.isTimeFrozen())
			return 0.5f;
		return tickDelta;
	}
	
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir)
	{
		if(UltracraftClient.isParryVisualsActive())
		{
			lastFovBonus = 5;
			cir.setReturnValue(cir.getReturnValueD() + 5);
		}
		else if(lastFovBonus > 0f)
		{
			lastFovBonus = MathHelper.lerp(tickDelta / 4f, lastFovBonus, 0f);
			cir.setReturnValue(cir.getReturnValueD() + lastFovBonus);
		}
	}
}
