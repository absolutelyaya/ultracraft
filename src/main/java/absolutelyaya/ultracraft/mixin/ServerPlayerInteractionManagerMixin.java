package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin
{
	@Shadow @Final protected ServerPlayerEntity player;
	int foodChecker = -1;
	
	@Inject(method = "setGameMode", at = @At("TAIL"))
	void onSetGameMode(GameMode gameMode, GameMode previousGameMode, CallbackInfo ci)
	{
		if(gameMode == previousGameMode || previousGameMode == null)
			return;
		HungerManager hunger = player.getHungerManager();
		if(gameMode.equals(GameMode.CREATIVE) || gameMode.equals(GameMode.SPECTATOR))
			foodChecker = hunger.getFoodLevel();
		else if(foodChecker >= 0 && foodChecker != hunger.getFoodLevel())
		{
			hunger.setFoodLevel(foodChecker);
			hunger.setExhaustion(0f);
			Ultracraft.LOGGER.info(player.getUuid() + " (" + player.getName() + ") seems to have suffered the hunger glitch. Restoring Food Level to " + foodChecker);
		}
	}
}
