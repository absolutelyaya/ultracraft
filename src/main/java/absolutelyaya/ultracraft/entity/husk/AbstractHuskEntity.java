package absolutelyaya.ultracraft.entity.husk;

import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.World;

public abstract class AbstractHuskEntity extends AbstractUltraHostileEntity
{
	protected AbstractHuskEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(getVelocity().y < 0f)
			amount *= 1.5;
		if(getBurningDuration() > 0 && !source.isIn(DamageTypeTags.IS_FIRE) && !source.isIn(DamageTypeTags.IS_EXPLOSION))
			amount *= 1.5;
		if(source.isOf(DamageSources.PROJBOOST))
			amount *= 2;
		return super.damage(source, amount);
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
