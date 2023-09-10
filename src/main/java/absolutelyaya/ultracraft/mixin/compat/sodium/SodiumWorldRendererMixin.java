package absolutelyaya.ultracraft.mixin.compat.sodium;

import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.compat.SodiumWorldRenderStuff;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererMixin
{
	@Shadow private RenderSectionManager renderSectionManager;
	
	@Inject(method = "drawChunkLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V", at = @At("TAIL"))
	void onDrawChunkLayer(RenderLayer renderLayer, MatrixStack matrixStack, double x, double y, double z, CallbackInfo ci)
	{
		ChunkRenderMatrices matrices = ChunkRenderMatrices.from(matrixStack);
		if(renderLayer == RenderLayers.getFlesh())
			renderSectionManager.renderLayer(matrices, SodiumWorldRenderStuff.FLESH_PASS, x, y, z);
	}
}
