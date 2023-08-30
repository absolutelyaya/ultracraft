package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class IngameHudMixin
{
	@Shadow @Final private MinecraftClient client;
	
	@Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
	void onRenderCrosshair(DrawContext context, CallbackInfo ci)
	{
		if(client.player instanceof WingedPlayerEntity winged && winged.getFocusedTerminal() != null)
			ci.cancel();
	}
}
