package absolutelyaya.ultracraft.accessor;

import net.minecraft.entity.player.PlayerEntity;

public interface ProjectileEntityAccessor
{
	void setParried(boolean val, PlayerEntity parrier);
	
	boolean isParried();
}
