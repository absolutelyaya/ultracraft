package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin
{
	@Inject(method = "getBlockLayers", at = @At("RETURN"), cancellable = true)
	private static void onGetBlockLayers(CallbackInfoReturnable<List<RenderLayer>> cir)
	{
		if(UltracraftClient.SODIUM)
			return;
		List<RenderLayer> layers = new ArrayList<>(cir.getReturnValue());
		layers.add(RenderLayers.getFlesh());
		cir.setReturnValue(layers);
	}
}
