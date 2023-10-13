package absolutelyaya.ultracraft.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

public class BossAntiCheeseTargetGoal<T extends LivingEntity> extends Goal
{
	final LivingEntity mob;
	final Class<T> targetClass;
	final int reciprocal;
	
	public BossAntiCheeseTargetGoal(LivingEntity mob, Class<T> targetClass, int reciprocal)
	{
		this.mob = mob;
		this.targetClass = targetClass;
		this.reciprocal = reciprocal;
	}
	
	@Override
	public boolean canStart()
	{
		return false;
	}
}
