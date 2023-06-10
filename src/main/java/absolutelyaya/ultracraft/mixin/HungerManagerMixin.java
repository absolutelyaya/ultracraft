package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin
{
	@Shadow public abstract int getFoodLevel();
	@Shadow public abstract void setFoodLevel(int foodLevel);
	
	@Shadow public abstract void setSaturationLevel(float saturationLevel);
	
	@Shadow public abstract void setExhaustion(float exhaustion);
	
	@Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V"), cancellable = true)
	void onHeal(PlayerEntity player, CallbackInfo ci)
	{
		if(((WingedPlayerEntity)player).isWingsActive())
			ci.cancel();
	}
	
	@Inject(method = "update", at = @At("TAIL"))
	void onUpdate(PlayerEntity player, CallbackInfo ci)
	{
		if(getFoodLevel() < 0)
		{
			setFoodLevel(20);
			setSaturationLevel(0);
			setExhaustion(0f);
		}
	}
}
