package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.ChainParryAccessor;
import absolutelyaya.ultracraft.client.rendering.entity.other.ParryAuraRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity>
{
	@Inject(method = "render", at = @At("HEAD"))
	void onRender(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
	{
		if(entity instanceof ProjectileEntity projectile && ((ChainParryAccessor)projectile).getParryCount() > 0)
			ParryAuraRenderer.render(projectile, matrices, vertexConsumers);
	}
}
