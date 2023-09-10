package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.ServerWorldAccessor;
import absolutelyaya.ultracraft.entity.demon.HideousMassEntity;
import absolutelyaya.ultracraft.entity.demon.HideousPart;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.ServerEntityHandler.class)
public class ServerEntityHandlerMixin
{
	@Inject(method = "startTracking(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateEventHandler(Ljava/util/function/BiConsumer;)V"))
	void onStartTracking(Entity entity, CallbackInfo ci)
	{
		if (entity instanceof HideousMassEntity hideousMassEntity) {
			HideousPart[] parts = hideousMassEntity.getParts();
			for (HideousPart part : parts)
				((ServerWorldAccessor)entity.getWorld()).getHideousParts().put(part.getId(), part);
		}
	}
	
	@Inject(method = "stopTracking(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateEventHandler(Ljava/util/function/BiConsumer;)V"))
	void onStopTracking(Entity entity, CallbackInfo ci)
	{
		if (entity instanceof HideousMassEntity hideousMassEntity) {
			HideousPart[] parts = hideousMassEntity.getParts();
			for (HideousPart part : parts)
				((ServerWorldAccessor)entity.getWorld()).getHideousParts().remove(part.getId(), part);
		}
	}
}
