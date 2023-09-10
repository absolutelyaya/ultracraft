package absolutelyaya.ultracraft.mixin.compat.sodium;

import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.compat.SodiumWorldRenderStuff;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultMaterials.class)
public class DefaultMaterialsMixin
{
	@Inject(method = "forRenderLayer", at = @At("HEAD"), cancellable = true)
	private static void onForRenderLayer(RenderLayer layer, CallbackInfoReturnable<Material> cir)
	{
		if(layer.equals(RenderLayers.FLESH))
			cir.setReturnValue(SodiumWorldRenderStuff.FLESH_MAT);
	}
}
