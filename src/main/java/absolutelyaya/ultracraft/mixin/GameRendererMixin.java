package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
	@Shadow @Final MinecraftClient client;
	
	@Shadow @Final private Camera camera;
	
	float slideViewTilt = 0f;
	
	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	public void onBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci)
	{
		if(client.player == null)
			return;
		WingedPlayerEntity winged = (WingedPlayerEntity)client.player;
		if(winged.isWingsActive() && (client.player.isSprinting() || winged.isDashing()))
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
}
