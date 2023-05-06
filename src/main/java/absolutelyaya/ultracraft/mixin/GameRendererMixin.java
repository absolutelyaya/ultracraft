package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
	@Shadow @Final MinecraftClient client;
	
	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	public void onBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci)
	{
		WingedPlayerEntity winged = (WingedPlayerEntity)client.player;
		if(client.player != null && winged.isWingsVisible() && (client.player.isSprinting() || winged.isDashing()))
			ci.cancel();
		if(Ultracraft.isTimeFrozen())
			ci.cancel();
	}
}
