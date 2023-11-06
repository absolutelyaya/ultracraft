package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldRenderer.class)
public class LivingEntityRendererMixin
{
	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"), index = 4)
	float adjustTickDelta(float tickDelta)
	{
		return Ultracraft.isTimeFrozen() ? 0f : tickDelta;
	}
}
