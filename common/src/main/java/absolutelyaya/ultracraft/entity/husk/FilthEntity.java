package absolutelyaya.ultracraft.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.MeleeParriable;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.EnumSet;

public class FilthEntity extends HostileEntity implements IAnimatable, MeleeParriable
{
	private final AnimationFactory factory = new AnimationFactory(this);
	private static final AnimationBuilder IDLE_ANIM = new AnimationBuilder().addAnimation("idle", true);
	private static final AnimationBuilder RUN_ANIM = new AnimationBuilder().addAnimation("run", true);
	private static final AnimationBuilder ATTACK_ANIM = new AnimationBuilder().addAnimation("attack", false);
	private static final AnimationBuilder THROWBACK_ANIM = new AnimationBuilder().addAnimation("throwback", true);
	protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(FilthEntity.class, TrackedDataHandlerRegistry.BYTE);
	protected static final TrackedData<Integer> ANIM_TICKS = DataTracker.registerData(FilthEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_ATTACK = 1;
	private static final byte ANIMATION_THROWBACK = 2;
	static int throwbackTicks;
	
	public FilthEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0f);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new FilthLungeAttackGoal(this, 0.4f));
		goalSelector.add(1, new WanderAroundGoal(this, 1.0));
		goalSelector.add(2, new LookAtEntityGoal(this, LivingEntity.class, 5));
		goalSelector.add(3, new LookAroundGoal(this));
		goalSelector.add(4, new WanderAroundFarGoal(this, 1.0));
		
		targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0d)
					   .add(EntityAttributes.GENERIC_ARMOR, 2.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0d);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		this.dataTracker.startTracking(ANIMATION, ANIMATION_IDLE);
		this.dataTracker.startTracking(ANIM_TICKS, 0);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(ANIMATION))
			dataTracker.set(ANIM_TICKS, 0);
	}
	
	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
	{
		byte anim = dataTracker.get(ANIMATION);
		AnimationController<?> controller = event.getController();
		
		controller.setAnimationSpeed(1f);
		switch (anim)
		{
			case ANIMATION_IDLE ->
			{
				controller.setAnimationSpeed(getVelocity().horizontalLengthSquared() > 0.03 ? 2f : 1f);
				controller.setAnimation(event.isMoving() ? RUN_ANIM : IDLE_ANIM);
			}
			case ANIMATION_ATTACK -> controller.setAnimation(ATTACK_ANIM);
			case ANIMATION_THROWBACK -> controller.setAnimation(THROWBACK_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimationData animationData)
	{
		AnimationController<FilthEntity> controller = new AnimationController<>(this, "controller",
				2, this::predicate);
		animationData.addAnimationController(controller);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(throwbackTicks > 0)
		{
			throwbackTicks--;
			if(dataTracker.get(ANIMATION) != ANIMATION_THROWBACK)
				dataTracker.set(ANIMATION, ANIMATION_THROWBACK);
		}
		else if(dataTracker.get(ANIMATION) == ANIMATION_THROWBACK)
			dataTracker.set(ANIMATION, ANIMATION_IDLE);
		
		//setGlowing(isAttacking());
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(getAnimation() == ANIMATION_ATTACK && dataTracker.get(ANIM_TICKS) < 26 && !Ultracraft.isTimeFrozen())
		{
			dataTracker.set(ANIM_TICKS, dataTracker.get(ANIM_TICKS) + 1);
			if(getVelocity().length() > 0.1)
				lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, getPos().add(getVelocity().normalize()));
			else if(getTarget() != null)
				lookAtEntity(getTarget(), 180, 180);
			setBodyYaw(headYaw);
		}
		else if(dataTracker.get(ANIM_TICKS) >= 26)
			dataTracker.set(ANIMATION, ANIMATION_IDLE);
	}
	
	public static void throwback()
	{
		throwbackTicks += 50;
	}
	
	@Override
	public AnimationFactory getFactory()
	{
		return factory;
	}
	
	public byte getAnimation()
	{
		return dataTracker.get(ANIMATION);
	}
	
	public int getAnimationTicks()
	{
		return dataTracker.get(ANIM_TICKS);
	}
	
	@Override
	protected void pushAway(Entity entity)
	{
		if(!isAttacking())
			super.pushAway(entity);
	}
	
	@Override
	public void pushAwayFrom(Entity entity)
	{
		if(!isAttacking())
			super.pushAwayFrom(entity);
	}
	
	static class FilthLungeAttackGoal extends Goal
	{
		FilthEntity mob;
		float velocity;
		LivingEntity target;
		int time;
		boolean didDamage;
		
		public FilthLungeAttackGoal(FilthEntity entity, float velocity)
		{
			this.mob = entity;
			this.velocity = velocity;
			setControls(EnumSet.of(Control.LOOK, Control.MOVE));
		}
		
		@Override
		public boolean canStart()
		{
			this.target = mob.getTarget();
			if (this.target == null)
				return false;
			
			double d = mob.squaredDistanceTo(this.target);
			if (d > 16.0 * 16.0)
				return false;
			
			return mob.isOnGround() && mob.getAnimation() != ANIMATION_ATTACK;
		}
		
		@Override
		public void start()
		{
			time = 0;
			didDamage = false;
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public void tick()
		{
			this.mob.getLookControl().lookAt(target);
			
			double d = mob.squaredDistanceTo(this.target);
			if (d > 3.0 * 3.0 && mob.getAnimation() != ANIMATION_ATTACK)
			{
				mob.getNavigation().startMovingTo(this.target, 1.0);
				return;
			}
			mob.getNavigation().stop();
			
			mob.setAttacking(time > 8 && time < 16);
			
			if(time == 0)
				mob.dataTracker.set(ANIMATION, ANIMATION_ATTACK);
			if (time == 12)
			{
				if(target != null)
				{
					Vec3d vec3d = this.mob.getVelocity();
					Vec3d vec3d2 = new Vec3d(this.target.getX() - this.mob.getX(), 0.0, this.target.getZ() - this.mob.getZ());
					if (vec3d2.lengthSquared() > 1.0E-7)
						vec3d2 = vec3d2.normalize().multiply(0.8).add(vec3d.multiply(0.2));
					
					this.mob.setVelocity(vec3d2.x, this.velocity, vec3d2.z);
				}
			}
			
			if(!didDamage && !mob.onGround && mob.getBoundingBox().expand(0.1f).intersects(target.getBoundingBox()))
				didDamage = mob.tryAttack(target);
			
			time++;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return time < 26 && mob.squaredDistanceTo(target) < 24.0 * 24.0;
		}
		
		@Override
		public boolean canStop()
		{
			return !shouldContinue();
		}
		
		@Override
		public void stop()
		{
			if(mob.getAnimation() == ANIMATION_ATTACK)
				mob.dataTracker.set(ANIMATION, ANIMATION_IDLE);
			mob.setAttacking(false);
		}
	}
}
