package absolutelyaya.ultracraft.entity.husk;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

public abstract class AbstractHuskEntity extends HostileEntity
{
	protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(AbstractHuskEntity.class, TrackedDataHandlerRegistry.BYTE);
	
	protected AbstractHuskEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	public byte getAnimation()
	{
		return dataTracker.get(ANIMATION);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		this.dataTracker.startTracking(ANIMATION, (byte)0);
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
