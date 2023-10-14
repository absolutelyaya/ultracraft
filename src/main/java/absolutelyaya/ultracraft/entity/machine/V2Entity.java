package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.entity.IAntiCheeseBoss;
import absolutelyaya.ultracraft.entity.goal.AntiCheeseProximityTargetGoal;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class V2Entity extends AbstractUltraHostileEntity implements IAntiCheeseBoss, GeoEntity, Enrageable
{
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("idle");
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenPlay("walk");
	private static final RawAnimation FALL_ANIM = RawAnimation.begin().thenPlay("fall");
	private static final RawAnimation INTRO_ANIM = RawAnimation.begin().thenPlay("intro").thenPlay("idle");
	private static final RawAnimation SLIDE_ANIM = RawAnimation.begin().thenPlay("slide_start").thenPlay("slide_loop");
	private static final RawAnimation SLIDE_END_ANIM = RawAnimation.begin().thenPlay("slide_stop").thenPlay("idle");
	static protected final TrackedData<Integer> FRUSTRATION = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	static protected final TrackedData<Integer> DISTANCE = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	static protected final TrackedData<Integer> IDLE_TIMER = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	static protected final TrackedData<Integer> INTRO_TICKS = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	static protected final TrackedData<Byte> MOVEMENT_MODE = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.BYTE);
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	protected static final byte ANIMATION_IDLE = 0;
	protected static final byte ANIMATION_INTRO = 1;
	protected static final byte ANIMATION_SLIDE = 2;
	
	int counter, wallJumps = 3;
	
	public V2Entity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		((LivingEntityAccessor)this).setTakePunchKnockbackSupplier(() -> false); //disable knockback
		moveControl = new V2MoveControl(this);
	}
	
	public static DefaultAttributeContainer getDefaultAttributes()
	{
        return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d).build();
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(FRUSTRATION, 0);
		dataTracker.startTracking(DISTANCE, 0);
		dataTracker.startTracking(IDLE_TIMER, 0);
		dataTracker.startTracking(INTRO_TICKS, 0);
		dataTracker.startTracking(MOVEMENT_MODE, (byte)1);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(INTRO_TICKS) && getAnimation() == ANIMATION_INTRO && dataTracker.get(INTRO_TICKS) >= 90)
		{
			dataTracker.set(INTRO_TICKS, -1);
			dataTracker.set(ANIMATION, ANIMATION_IDLE);
		}
	}
	
	@Override
	protected void initGoals()
	{
		super.initGoals();
		goalSelector.add(0, new YellowMovementGoal(this));
		goalSelector.add(0, new BlueMovementGoal(this));
		goalSelector.add(0, new RedMovementGoal(this));
		
		targetSelector.add(0, new AntiCheeseProximityTargetGoal<>(this, PlayerEntity.class, 20, 32));
	}
	
	@Override
	public void tick()
	{
		super.tick();
		dataTracker.set(FRUSTRATION, dataTracker.get(FRUSTRATION) + 1);
		if(getTarget() == null && getHealth() < getMaxHealth())
		{
			dataTracker.set(IDLE_TIMER, dataTracker.get(IDLE_TIMER) + 1);
			if(dataTracker.get(IDLE_TIMER) > 1200)
			{
				if(age % 20 == 0)
					heal(1f);
			}
		}
		else if(dataTracker.get(IDLE_TIMER) > 0)
			dataTracker.set(IDLE_TIMER, 0);
		if(dataTracker.get(INTRO_TICKS) != -1)
		{
			if(isOnGround() && getAnimation() != ANIMATION_INTRO)
				dataTracker.set(ANIMATION, ANIMATION_INTRO);
			if(getAnimation() == ANIMATION_INTRO)
				dataTracker.set(INTRO_TICKS, dataTracker.get(INTRO_TICKS) + 1);
		}
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(getAnimation() != ANIMATION_SLIDE)
		{
			prevYaw = getYaw();
			setYaw(MathHelper.lerp(0.1f, getYaw(), getHeadYaw()));
		}
		if(counter-- <= 0)
		{
			dataTracker.set(MOVEMENT_MODE, (byte)((dataTracker.get(MOVEMENT_MODE) + 1) % 3));
			counter = 600;
		}
		if(wallJumps < 3 && groundCollision)
			wallJumps = 3;
	}
	
	@Override
	protected void jump()
	{
		if(!groundCollision)
		{
			if(wallJumps-- <= 0)
				return;
		}
		super.jump();
	}
	
	@Override
	public int getFrustration()
	{
		return dataTracker.get(FRUSTRATION);
	}
	
	@Override
	public void resetFrustration()
	{
		dataTracker.set(FRUSTRATION, 0);
	}
	
	private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> ctx)
	{
		switch(getAnimation())
		{
			case ANIMATION_IDLE -> ctx.setAnimation(isOnGround() ? IDLE_ANIM : FALL_ANIM);
			case ANIMATION_INTRO -> ctx.setAnimation(INTRO_ANIM);
			case ANIMATION_SLIDE -> ctx.setAnimation(SLIDE_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	private <T extends GeoAnimatable> PlayState legPredicate(AnimationState<T> ctx)
	{
		if(getAnimation() == ANIMATION_IDLE && ctx.isMoving() && isOnGround())
		{
			AnimationController<?> controller = ctx.getController();
			controller.setAnimationSpeed(1.65f);
			ctx.setAnimation(WALK_ANIM);
			return PlayState.CONTINUE;
		}
		return PlayState.STOP;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(
				new AnimationController<>(this, "main", 2, this::predicate),
				new AnimationController<>(this, "legs", 2, this::legPredicate));
	}
	
	double getSpeedAttribute()
	{
		return getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	protected boolean getBossDefault()
	{
		return true;
	}
	
	@Override
	public boolean cannotDespawn()
	{
		return true;
	}
	
	@Override
	public boolean isEnraged()
	{
		return false;
	}
	
	@Override
	public boolean isFireImmune()
	{
		return true;
	}
	
	boolean isPlayingIntro()
	{
		return dataTracker.get(INTRO_TICKS) >= 0;
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(isPlayingIntro() && !source.isOf(DamageSources.COIN_PUNCH))
			return false;
		if(source.isOf(DamageTypes.FALL))
			return false;
		return super.damage(source, amount);
	}
	
	@Override
	public Vec3d getEnrageFeatureSize()
	{
		return new Vec3d(1f, -1f, -1f);
	}
	
	@Override
	public Vec3d getEnragedFeatureOffset()
	{
		return new Vec3d(0f, -2f, 0f);
	}
	
	@Override
	public Box getBoundingBox(EntityPose pose)
	{
		if(getAnimation() == ANIMATION_SLIDE)
			return Box.of(getPos(), 0.3f, 0.5f, 0.3f);
		return super.getBoundingBox(pose);
	}
	
	static class V2MoveControl extends MoveControl
	{
		public V2MoveControl(MobEntity entity)
		{
			super(entity);
		}
		
		public void strafeTo(float forward, float sideways, float speed)
		{
			this.state = State.STRAFE;
			this.forwardMovement = forward;
			this.sidewaysMovement = sideways;
			this.speed = speed;
		}
	}
	
	static class V2MovementGoal extends Goal
	{
		protected final V2Entity mob;
		
		public V2MovementGoal(V2Entity mob)
		{
			this.mob = mob;
			setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
		}
		
		@Override
		public boolean canStart()
		{
			return !mob.isPlayingIntro();
		}
	}
	
	static class YellowMovementGoal extends V2MovementGoal //Random Direction + Jumping
	{
		int cooldown;
		Vec3d dir;
		
		public YellowMovementGoal(V2Entity mob)
		{
			super(mob);
		}
		
		@Override
		public boolean canStart()
		{
			if(!super.canStart())
				return false;
			return mob.getTarget() != null && mob.dataTracker.get(MOVEMENT_MODE) == 0;
		}
		
		@Override
		public void start()
		{
			super.start();
			changeDirection();
		}
		
		void changeDirection() //TODO: force direction to lead away from nearby walls
		{
			dir = new Vec3d(mob.random.nextFloat() - 0.5f, 0f, mob.random.nextFloat() - 0.5f).normalize();
			cooldown = 300 + (int)(300 * mob.random.nextFloat());
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public void tick()
		{
			if(dir == null)
			{
				changeDirection();
				return;
			}
			super.tick();
			mob.getLookControl().lookAt(mob.getTarget());
			mob.setVelocity(dir.multiply(mob.getSpeedAttribute() * 1.5f).add(0, mob.getVelocity().y, 0));
			cooldown--;
			if(mob.getRandom().nextFloat() < 0.005f)
				mob.jump();
			if(mob.horizontalCollision || cooldown <= 0)
			{
				changeDirection();
				if(mob.getRandom().nextFloat() < 0.1f)
					mob.jump();
			}
			if(mob.distanceTo(mob.getTarget()) > 24f)
				dir = mob.getTarget().getPos().subtract(mob.getPos()).normalize();
		}
		
		@Override
		public boolean shouldContinue()
		{
			return mob.dataTracker.get(MOVEMENT_MODE) == 0 && mob.getTarget() != null;
		}
	}
	
	static class BlueMovementGoal extends V2MovementGoal //Circle Target
	{
		float dir;
		
		public BlueMovementGoal(V2Entity mob)
		{
			super(mob);
		}
		
		@Override
		public boolean canStart()
		{
			if(!super.canStart())
				return false;
			return mob.getTarget() != null && mob.dataTracker.get(MOVEMENT_MODE) == 1;
		}
		
		@Override
		public void start()
		{
			super.start();
			dir = mob.getRandom().nextBoolean() ? 1 : -1;
		}
		
		@Override
		public void tick()
		{
			mob.getLookControl().lookAt(mob.getTarget(), 30.0f, 30.0f);
			float forward = 0.2f;
			float dirChangeChance = 0.01f;
			if(mob.horizontalCollision)
			{
				forward += 0.5f;
				dirChangeChance = 0.1f;
				if(mob.getRandom().nextFloat() < 0.1f)
					mob.jump();
			}
			if(mob.getRandom().nextFloat() < dirChangeChance)
				dir = (dir == 1 ? -1 : 1);
			if(mob.getMoveControl() instanceof V2MoveControl moveControl)
				moveControl.strafeTo(mob.distanceTo(mob.getTarget()) < 5f ? 0f : forward, 0.8f * dir, 0.5f);
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return mob.dataTracker.get(MOVEMENT_MODE) == 1 && mob.getTarget() != null;
		}
		
		@Override
		public void stop()
		{
			super.stop();
			mob.getMoveControl().strafeTo(0f, 0f);
		}
	}
	
	static class RedMovementGoal extends V2MovementGoal //Rapid, Aggressive approach
	{
		Vec3d slideDir;
		int slideTimer;
		boolean circleDir;
		
		public RedMovementGoal(V2Entity mob)
		{
			super(mob);
		}
		
		@Override
		public boolean canStart()
		{
			if(!super.canStart())
				return false;
			return mob.getTarget() != null && mob.dataTracker.get(MOVEMENT_MODE) == 2;
		}
		
		@Override
		public void start()
		{
			super.start();
			circleDir = mob.random.nextBoolean();
		}
		
		@Override
		public void tick()
		{
			super.tick();
			float targetDistance = mob.distanceTo(mob.getTarget());
			mob.getLookControl().lookAt(mob.getTarget(), 20.0f, 20.0f);
			if(mob.getAnimation() != ANIMATION_SLIDE) //Not Sliding
			{
				if (targetDistance > 10f) //Start sliding
				{
					if(mob.canSee(mob.getTarget()) && mob.getRandom().nextFloat() < 0.025f)
					{
						mob.getDataTracker().set(ANIMATION, ANIMATION_SLIDE);
						slideDir = mob.getTarget().getPos().add(mob.getTarget().getVelocity().multiply(1.5f)).subtract(mob.getPos()).normalize();
						slideTimer = 200 + mob.getRandom().nextInt(100);
					}
				}
				if(targetDistance > 4f)
				{
					Vec3d dest = mob.getTarget().getPos();
					mob.moveControl.moveTo(dest.x, dest.y, dest.z, 1.4f);
				}
				else //Circle Target
				{
					if(mob.getMoveControl() instanceof V2MoveControl moveControl)
						moveControl.strafeTo(targetDistance < 3f ? 0f : 0.2f, 0.8f * (circleDir ? 1 : -1), 0.5f);
				}
			}
			else //Sliding
			{
				if(targetDistance < 5f || slideTimer-- <= 0 || mob.horizontalCollision)
					mob.dataTracker.set(ANIMATION, ANIMATION_IDLE);
				mob.setVelocity(slideDir.multiply(mob.getSpeedAttribute() * 1.9f).add(0, mob.getVelocity().y, 0));
			}
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return mob.dataTracker.get(MOVEMENT_MODE) == 2 && mob.getTarget() != null;
		}
		
		@Override
		public void stop()
		{
			super.stop();
			if(mob.getAnimation() == ANIMATION_SLIDE)
				mob.getDataTracker().set(ANIMATION, ANIMATION_IDLE);
		}
	}
}
