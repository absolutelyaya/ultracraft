package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class WorldMixin
{
	@Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
	void onTickEntity(Entity entity, CallbackInfo ci)
	{
		if(Ultracraft.isTimeFrozen())
			ci.cancel();
	}
}