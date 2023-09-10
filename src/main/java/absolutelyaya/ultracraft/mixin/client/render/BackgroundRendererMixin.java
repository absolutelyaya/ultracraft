package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin
{
	@Inject(method = "applyFog", at = @At(value = "TAIL"))
	private static void onBloodFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci)
	{
		World world = MinecraftClient.getInstance().world;
		if(world != null && world.getBlockState(camera.getBlockPos()).isOf(BlockRegistry.BLOOD))
		{
			RenderSystem.setShaderFogColor(0.56f, 0.09f, 0.01f);
			RenderSystem.setShaderFogStart(RenderSystem.getShaderFogStart() / 16f);
			RenderSystem.setShaderFogEnd(RenderSystem.getShaderFogEnd() / 16f);
			UltracraftClient.addBlood(tickDelta / 2f);
		}
		else if(world.getFluidState(camera.getBlockPos()).isIn(FluidTags.WATER))
			UltracraftClient.clearBlood();
	}
}
