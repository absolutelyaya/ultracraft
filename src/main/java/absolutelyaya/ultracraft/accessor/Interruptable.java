package absolutelyaya.ultracraft.accessor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public interface Interruptable
{
	void onInterrupted(PlayerEntity interruptor);
	
	Vec3d getChargeOffset();
}
