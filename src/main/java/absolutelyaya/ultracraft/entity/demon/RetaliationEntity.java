package absolutelyaya.ultracraft.entity.demon;

import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.registry.StatusEffectRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

import java.util.List;

public class RetaliationEntity extends AbstractUltraHostileEntity
{
	int purpose = 40;
	
	public RetaliationEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 20d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.6d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 128.0d);
	}
	
	@Override
	protected void initGoals()
	{
		targetSelector.add(0, new MeleeAttackGoal(this, 0.75, false));
		goalSelector.add(1, new PounceAtTargetGoal(this, 0.33f));
		
		targetSelector.add(0, new RetaliationTargetGoal(this));
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if((getTarget() == null || !getTarget().isAlive()) && !cannotDespawn() && !getWorld().isClient)
		{
			purpose--;
			if(purpose <= 0)
				discard();
		}
	}
	
	@Override
	public boolean tryAttack(Entity target)
	{
		float amount = (float)getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		boolean b = target.damage(DamageSources.get(getWorld(), DamageSources.RETALIATION, this), amount);
		if(b)
			onAttacking(target);
		return b;
	}
	
	static class RetaliationTargetGoal extends Goal
	{
		final RetaliationEntity retaliation;
		
		public RetaliationTargetGoal(RetaliationEntity retaliation)
		{
			this.retaliation = retaliation;
		}
		
		@Override
		public boolean canStart()
		{
			return retaliation.getTarget() == null;
		}
		
		@Override
		public void start()
		{
			List<Entity> list = retaliation.getWorld().getOtherEntities(retaliation, retaliation.getBoundingBox().expand(128),
					e -> e instanceof LivingEntity living && living.hasStatusEffect(StatusEffectRegistry.RETALIATION));
			if(list.size() == 0)
				return;
			for (Entity e : list)
			{
				if(e instanceof LivingEntity living)
				{
					retaliation.setTarget(living);
					return;
				}
			}
		}
	}
}
