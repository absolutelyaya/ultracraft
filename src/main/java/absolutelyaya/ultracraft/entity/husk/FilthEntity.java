package absolutelyaya.ultracraft.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.MeleeInterruptable;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class FilthEntity extends AbstractHuskEntity implements GeoEntity, MeleeInterruptable
{
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
	private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenLoop("attack");
	private static final RawAnimation THROWBACK_ANIM = RawAnimation.begin().thenLoop("throwback");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	protected static final TrackedData<Boolean> RARE = DataTracker.registerData(FilthEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(FilthEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_ATTACK = 1;
	private static final byte ANIMATION_THROWBACK = 2;
	static int throwbackTicks;
	
	public FilthEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0f);
		if(!world.isClient)
			dataTracker.set(RARE, random.nextInt(10000) == 0);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(RARE, false);
		dataTracker.startTracking(ATTACK_COOLDOWN, 0);
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
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 2d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d);
	}
	
	private <E extends GeoEntity> PlayState predicate(AnimationState<E> event)
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
		if(dataTracker.get(ATTACK_COOLDOWN) > 0)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(getAnimation() == ANIMATION_ATTACK && !Ultracraft.isTimeFrozen())
		{
			if(getVelocity().length() > 0.1)
				lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, getPos().add(getVelocity().normalize()));
			else if(getTarget() != null)
				lookAtEntity(getTarget(), 180, 180);
			setBodyYaw(headYaw);
		}
	}
	
	public void throwback()
	{
		throwbackTicks += 50;
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
	
	@Override
	public void onInterrupt(PlayerEntity parrier)
	{
	
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		AnimationController<FilthEntity> controller = new AnimationController<>(this, "controller",
				2, this::predicate);
		controllerRegistrar.add(controller);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	public boolean isRare()
	{
		return dataTracker.get(RARE);
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		if(isRare())
			nbt.putBoolean("oddValue", true);
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if(nbt.contains("oddValue"))
			dataTracker.set(RARE, nbt.getBoolean("oddValue"));
	}
	
	@Override
	public boolean cannotDespawn()
	{
		return super.cannotDespawn() || isRare();
	}
	
	static class FilthLungeAttackGoal extends Goal
	{
		final FilthEntity mob;
		final float velocity;
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
			if (this.target == null || mob.dataTracker.get(ATTACK_COOLDOWN) > 0)
				return false;
			
			double d = mob.squaredDistanceTo(target);
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
			
			double d = mob.squaredDistanceTo(target);
			if (d > 3.0 * 3.0 && mob.getAnimation() != ANIMATION_ATTACK)
			{
				mob.getNavigation().startMovingTo(target, 1.0);
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
					Vec3d vec3d = mob.getVelocity();
					Vec3d vec3d2 = new Vec3d(target.getX() - mob.getX(), 0.0, target.getZ() - mob.getZ());
					if (vec3d2.lengthSquared() > 1.0E-7)
						vec3d2 = vec3d2.normalize().multiply(0.8).add(vec3d.multiply(0.2));
					
					mob.setVelocity(vec3d2.x, velocity, vec3d2.z);
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
			mob.dataTracker.set(ATTACK_COOLDOWN, 10 + mob.random.nextInt(10));
			mob.setAttacking(false);
		}
	}
}
