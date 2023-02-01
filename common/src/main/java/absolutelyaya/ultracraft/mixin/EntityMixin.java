package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin
{
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	void onTick(CallbackInfo ci)
	{
		if (Ultracraft.isTimeFrozen())
			ci.cancel();
	}
	
	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	void onMove(CallbackInfo ci)
	{
		if (Ultracraft.isTimeFrozen())
			ci.cancel();
	}
}
