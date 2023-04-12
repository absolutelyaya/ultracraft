package absolutelyaya.ultracraft.accessor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;

public interface ProjectileEntityAccessor
{
	void setParried(boolean val, PlayerEntity parrier);
	
	boolean isParried();
	
	boolean isParriable();
	
	void onParriedCollision(HitResult hitResult);
}
