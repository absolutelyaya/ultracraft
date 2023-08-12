package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.entity.projectile.FlameProjectileEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class StreetCleanerEntity extends AbstractUltraHostileEntity implements GeoEntity
{
	static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	static final RawAnimation IDLE_AIM_ANIM = RawAnimation.begin().thenLoop("idle_aim");
	static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
	static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
	static final RawAnimation DODGE_ANIM = RawAnimation.begin().thenLoop("dodge");
	static final RawAnimation COUNTER_ANIM = RawAnimation.begin().thenLoop("counter");
	AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	static final byte ANIMATION_IDLE = 0;
	static final byte ANIMATION_DODGE = 1;
	static final byte ANIMATION_COUNTER = 2;
	protected static final TrackedData<Integer> ROTATION_DELAY = DataTracker.registerData(StreetCleanerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> ROTATION_DELAY_COOLDOWN = DataTracker.registerData(StreetCleanerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Boolean> ATTACKING = DataTracker.registerData(StreetCleanerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public StreetCleanerEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		lookControl = new StreetCleanerLookControl(this);
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 9.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ROTATION_DELAY, 0);
		dataTracker.startTracking(ROTATION_DELAY_COOLDOWN, 0);
		dataTracker.startTracking(ATTACKING, false);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new StreetCleanerChaseGoal(this));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(dataTracker.get(ROTATION_DELAY) > 0)
			dataTracker.set(ROTATION_DELAY, dataTracker.get(ROTATION_DELAY) - 1);
		if(dataTracker.get(ROTATION_DELAY_COOLDOWN) > 0)
			dataTracker.set(ROTATION_DELAY_COOLDOWN, dataTracker.get(ROTATION_DELAY_COOLDOWN) - 1);
		if(!getWorld().isClient && dataTracker.get(ATTACKING) && age % 4 == 0)
		{
			for (int i = 0; i < 2; i++)
			{
				FlameProjectileEntity fireball = FlameProjectileEntity.spawn(this, getWorld());
				fireball.setPosition(getBoundingBox().getCenter());
				Vec3d dir = getRotationVector();
				fireball.setVelocity(dir.x, dir.y, dir.z, 0.6f + getRandom().nextFloat() * 0.5f, 15);
				fireball.setGriefing(false);
				getWorld().spawnEntity(fireball);
			}
		}
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(!getWorld().isClient && age % 20 == 0 && random.nextFloat() < 0.33f)
		{
			getWorld().playSound(null, getBlockPos(), SoundEvents.ENTITY_PLAYER_BREATH,
					SoundCategory.HOSTILE, 1f, 0.75f + random.nextFloat() * 0.15f);
		}
		setBodyYaw(headYaw);
		if(touchingWater)
			damage(DamageSources.get(getWorld(), DamageSources.SHORT_CIRCUIT), 999f);
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(source.isOf(DamageSources.FLAMETHROWER))
			return false;
		return super.damage(source, source.isIn(DamageTypeTags.IS_EXPLOSION) ? amount * 0.5f : amount);
	}
	
	private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event)
	{
		byte anim = dataTracker.get(ANIMATION);
		
		event.setControllerSpeed(1f);
		switch(anim)
		{
			case ANIMATION_IDLE -> {
				if(event.isMoving())
				{
					event.setControllerSpeed(1.25f);
					event.setAnimation(getVelocity().horizontalLength() < 0.1 ? WALK_ANIM : RUN_ANIM);
				}
				else
					event.setAnimation(dataTracker.get(ATTACKING) ? IDLE_AIM_ANIM : IDLE_ANIM);
			}
			case ANIMATION_DODGE -> event.setAnimation(DODGE_ANIM);
			case ANIMATION_COUNTER -> event.setAnimation(COUNTER_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "streetcleaner", 2, this::predicate));
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	static class StreetCleanerLookControl extends LookControl
	{
		public StreetCleanerLookControl(MobEntity entity)
		{
			super(entity);
		}
		
		@Override
		public void tick()
		{
			if(entity.getDataTracker().get(ROTATION_DELAY) == 0 && entity.getTarget() != null)
			{
				double e = entity.getTarget().getX() - entity.getX();
				double f = entity.getTarget().getZ() - entity.getZ();
				float targetYaw = -((float) MathHelper.atan2(e, f)) * 57.295776f;
				if(entity.getDataTracker().get(ROTATION_DELAY_COOLDOWN) == 0 && Math.abs(targetYaw - entity.getYaw()) > 45f)
				{
					entity.getDataTracker().set(ROTATION_DELAY, 10);
					entity.getDataTracker().set(ROTATION_DELAY_COOLDOWN, 40);
					return;
				}
				entity.headYaw = changeAngle(entity.headYaw, targetYaw, 20f);
				entity.setYaw(entity.headYaw);
			}
		}
	}
	
	static class StreetCleanerChaseGoal extends Goal
	{
		StreetCleanerEntity cleaner;
		LivingEntity target;
		
		public StreetCleanerChaseGoal(StreetCleanerEntity cleaner)
		{
			this.cleaner = cleaner;
		}
		
		@Override
		public boolean canStart()
		{
			return cleaner.getTarget() != null;
		}
		
		@Override
		public void start()
		{
			target = cleaner.getTarget();
		}
		
		@Override
		public boolean shouldContinue()
		{
			return cleaner.getTarget() != null;
		}
		
		@Override
		public boolean canStop()
		{
			return cleaner.getTarget() == null;
		}
		
		@Override
		public void tick()
		{
			float distance = cleaner.distanceTo(target);
			if(cleaner.dataTracker.get(ROTATION_DELAY) == 0 && distance > 5f)
				cleaner.navigation.startMovingTo(target, distance < 6f ? 1f : 1.25f);
			
			if(!cleaner.dataTracker.get(ATTACKING) && distance < 6f)
				cleaner.dataTracker.set(ATTACKING, true);
			if(cleaner.dataTracker.get(ATTACKING) && distance > 6f)
				cleaner.dataTracker.set(ATTACKING, false);
		}
		
		@Override
		public void stop()
		{
			cleaner.dataTracker.set(ATTACKING, false);
		}
	}
}
