package absolutelyaya.ultracraft.accessor;

import net.minecraft.entity.player.PlayerEntity;

public interface Interruptable
{
	void onInterrupted(PlayerEntity interruptor);
}
