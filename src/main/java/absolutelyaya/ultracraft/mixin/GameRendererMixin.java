package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
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
	
	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	public void onBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci)
	{
		WingedPlayerEntity winged = (WingedPlayerEntity)client.player;
		if(client.player != null && winged.isWingsVisible() && (client.player.isSprinting() || winged.isDashing()))
			ci.cancel();
	}
}
