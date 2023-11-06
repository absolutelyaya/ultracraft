package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.effects.RetaliationFogMultiplier;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.StatusEffectRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin
{
	private static final RetaliationFogMultiplier RETALIATION_FOG_MULTIPLIER = new RetaliationFogMultiplier();
	
	@Inject(method = "applyFog", at = @At("TAIL"))
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
	
	@Inject(method = "getFogModifier", at = @At("HEAD"), cancellable = true)
	private static void onGetFogMultiplier(Entity entity, float tickDelta, CallbackInfoReturnable<BackgroundRenderer.StatusEffectFogModifier> cir)
	{
		if (entity instanceof LivingEntity living && living.hasStatusEffect(StatusEffectRegistry.RETALIATION))
			cir.setReturnValue(RETALIATION_FOG_MULTIPLIER);
	}
}
