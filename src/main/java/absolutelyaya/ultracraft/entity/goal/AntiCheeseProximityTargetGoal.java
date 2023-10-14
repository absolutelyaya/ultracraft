package absolutelyaya.ultracraft.entity.goal;

import absolutelyaya.ultracraft.entity.IAntiCheeseBoss;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class AntiCheeseProximityTargetGoal<T extends LivingEntity> extends Goal
{
	final MobEntity mob;
	final Class<T> targetClass;
	final int reciprocal, frustrationThreshold;
	final float radius;
	
	LivingEntity target;
	boolean forceTargetSwitch;
	
	public AntiCheeseProximityTargetGoal(MobEntity mob, Class<T> targetClass, int reciprocal, float radius)
	{
		this(mob, targetClass, reciprocal, radius, 600); //30 seconds frustration threshold
	}
	
	public AntiCheeseProximityTargetGoal(MobEntity mob, Class<T> targetClass, int reciprocal, float radius, int frustrationThreshold)
	{
		this.mob = mob;
		this.targetClass = targetClass;
		this.reciprocal = reciprocal;
		this.radius = radius;
		this.frustrationThreshold = frustrationThreshold;
	}
	
	@Override
	public boolean canStart()
	{
		if(reciprocal > 0 && mob.getRandom().nextInt(reciprocal) != 0)
			return false;
		if(target != null && mob instanceof IAntiCheeseBoss boss && boss.getFrustration() > frustrationThreshold)
			forceTargetSwitch = true;
		return true;
	}
	
	@Override
	public void start()
	{
		List<T> list = mob.getWorld().getEntitiesByClass(targetClass, mob.getBoundingBox().expand(radius), i -> true);
		Vec3d pos = mob.getPos();
		target = mob.getWorld().getClosestEntity(list, TargetPredicate.DEFAULT.ignoreVisibility(), mob, pos.x, pos.y, pos.z);
		if(!forceTargetSwitch || (forceTargetSwitch && target != null))
		{
			mob.setTarget(target);
			if(forceTargetSwitch && mob instanceof IAntiCheeseBoss boss)
				boss.resetFrustration();
		}
		forceTargetSwitch = false;
	}
}
