package absolutelyaya.ultracraft.accessor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;

import java.util.function.Consumer;

public interface ProjectileEntityAccessor
{
	void setParried(boolean val, PlayerEntity parrier);
	
	boolean isParried();
	
	boolean isParriable();
	
	void onParriedCollision(HitResult hitResult);
	
	boolean isHitscanHittable(byte type);
	
	boolean isBoostable();
	
	PlayerEntity getParrier();
	
	void setParrier(PlayerEntity p);
	
	default void setOnParried(Consumer<Integer> consumer) {}
}
