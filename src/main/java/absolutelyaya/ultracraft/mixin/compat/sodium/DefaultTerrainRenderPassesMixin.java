package absolutelyaya.ultracraft.mixin.compat.sodium;

import absolutelyaya.ultracraft.compat.SodiumWorldRenderStuff;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(DefaultTerrainRenderPasses.class)
public class DefaultTerrainRenderPassesMixin
{
	@Final @Shadow @Mutable public static TerrainRenderPass[] ALL;
	
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void onClinit(CallbackInfo ci)
	{
		List<TerrainRenderPass> passes = new ArrayList<>(List.of(ALL));
		passes.add(SodiumWorldRenderStuff.FLESH_PASS);
		ALL = passes.toArray(new TerrainRenderPass[0]);
	}
}
