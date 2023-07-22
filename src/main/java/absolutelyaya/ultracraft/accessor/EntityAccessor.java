package absolutelyaya.ultracraft.accessor;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.function.Function;
import java.util.function.Supplier;

public interface EntityAccessor
{
	int getTargetPriority(Entity source);
	
	void setTargetpriorityFunction(Function<Entity, Integer> function);
	
	boolean isTargettable();
	
	void setTargettableSupplier(Supplier<Boolean> supplier);
	
	Vec3d getRelativeTargetPoint();
	
	//TODO: use this to aim for weakpoints, like the Street Cleaners Gas Tank, or Sentries Antenna
	void setRelativeTargetPointSupplier(Supplier<Vec3d> supplier);
}
