package absolutelyaya.ultracraft.entity.husk;

import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

public abstract class AbstractHuskEntity extends AbstractUltraHostileEntity
{
	protected AbstractHuskEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	static class GetIntoSightGoal extends Goal
	{
		AbstractHuskEntity husk;
		LivingEntity target;
		
		public GetIntoSightGoal(AbstractHuskEntity husk)
		{
			this.husk = husk;
		}
		
		@Override
		public boolean canStart()
		{
			target = husk.getTarget();
			return target != null && !husk.canSee(target);
		}
		
		@Override
		public void tick()
		{
			husk.navigation.startMovingTo(target, 1f);
		}
		
		@Override
		public boolean shouldContinue()
		{
			return target != null && target.isAlive() && !husk.canSee(target);
		}
		
		@Override
		public void stop()
		{
			husk.navigation.stop();
		}
	}
}
