package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.entity.demon.HideousMassEntity;
import absolutelyaya.ultracraft.entity.demon.HideousPart;
import absolutelyaya.ultracraft.entity.machine.SwordsmachineEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin
{
	@Inject(method = "renderFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
	void onRenderFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, CallbackInfo ci)
	{
		if(entity instanceof SwordsmachineEntity)
			matrices.scale(0.75f, 0.75f, 0.75f);
	}
	
	@Inject(method = "renderHitbox", at = @At(value = "TAIL"))
	private static void onRenderHitboxes(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta, CallbackInfo ci)
	{
		if(entity instanceof HideousMassEntity mass)
		{
			double x = -MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
			double y = -MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
			double z = -MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
			HideousPart[] parts = mass.getParts();
			for (HideousPart part : parts)
			{
				matrices.push();
				double dx = x + MathHelper.lerp(tickDelta, part.lastRenderX, part.getX());
				double dy = y + MathHelper.lerp(tickDelta, part.lastRenderY, part.getY());
				double dz = z + MathHelper.lerp(tickDelta, part.lastRenderZ, part.getZ());
				matrices.translate(dx, dy, dz);
				Vector4f color = part.getBoxColor();
				Box box = part.getBoundingBox().offset(-part.getX(), -part.getY(), -part.getZ());
				WorldRenderer.drawBox(matrices, vertices, box,
						color.x, color.y, color.z, color.w);
				matrices.pop();
			}
		}
	}
}
