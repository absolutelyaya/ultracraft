package absolutelyaya.ultracraft.accessor;

import net.minecraft.entity.player.PlayerEntity;

public interface MeleeInterruptable
{
	void onInterrupt(PlayerEntity interrupter);
}
