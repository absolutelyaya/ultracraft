package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.entity.other.BackTank;
import absolutelyaya.ultracraft.entity.projectile.EjectedCoreEntity;
import absolutelyaya.ultracraft.entity.projectile.FlameProjectileEntity;
import absolutelyaya.ultracraft.entity.projectile.ShotgunPelletEntity;
import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
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
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
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

import java.util.List;

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
	protected static final TrackedData<Integer> COUNTER_COOLDOWN = DataTracker.registerData(StreetCleanerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> DODGE_COOLDOWN = DataTracker.registerData(StreetCleanerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> ANIM_TIME = DataTracker.registerData(StreetCleanerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> ATTACK_TIME = DataTracker.registerData(StreetCleanerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(StreetCleanerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	BackTank tank;
	
	public StreetCleanerEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		lookControl = new StreetCleanerLookControl(this);
		moveControl = new StreetCleanerMoveControl(this);
		tank = BackTank.spawn(world, this);
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 9.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
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
		dataTracker.startTracking(COUNTER_COOLDOWN, 0);
		dataTracker.startTracking(DODGE_COOLDOWN, 0);
		dataTracker.startTracking(ANIM_TIME, 0);
		dataTracker.startTracking(ATTACK_TIME, 0);
		dataTracker.startTracking(ATTACK_COOLDOWN, 0);
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
		if(dataTracker.get(COUNTER_COOLDOWN) > 0)
			dataTracker.set(COUNTER_COOLDOWN, dataTracker.get(COUNTER_COOLDOWN) - 1);
		if(dataTracker.get(DODGE_COOLDOWN) > 0)
			dataTracker.set(DODGE_COOLDOWN, dataTracker.get(DODGE_COOLDOWN) - 1);
		if(dataTracker.get(ANIMATION) != ANIMATION_IDLE)
			dataTracker.set(ANIM_TIME, dataTracker.get(ANIM_TIME) + 1);
		if(dataTracker.get(ATTACK_COOLDOWN) > 0)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
		if(!getWorld().isClient && isAlive() && dataTracker.get(ATTACKING))
		{
			dataTracker.set(ATTACK_TIME, dataTracker.get(ATTACK_TIME) + 1);
			if(dataTracker.get(ATTACK_TIME) > 8 && age % 4 == 0)
			{
				for (int i = 0; i < 2; i++)
				{
					FlameProjectileEntity flame = FlameProjectileEntity.spawn(this, getWorld());
					flame.setPosition(getBoundingBox().getCenter());
					Vec3d dir = getRotationVector();
					flame.setVelocity(dir.x, dir.y, dir.z, 0.6f + getRandom().nextFloat() * 0.5f, 10);
					flame.setGriefing(false);
					getWorld().spawnEntity(flame);
				}
			}
		}
		else if(!getWorld().isClient && dataTracker.get(ATTACK_TIME) > 0)
		{
			dataTracker.set(ATTACK_TIME, 0);
			dataTracker.set(ATTACK_COOLDOWN, 10);
		}
		if(getWorld().isClient && dataTracker.get(ATTACK_TIME) == 1)
		{
			Vec3d pos = getEyePos().add(getRotationVector().multiply(1.5f));
			getWorld().addParticle(new ParryIndicatorParticleEffect(true), pos.x, pos.y, pos.z, 0, 0, 0);
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
		if(isCanCounter())
		{
			List<EjectedCoreEntity> counterCandidates = getWorld().getEntitiesByType(TypeFilter.instanceOf(EjectedCoreEntity.class),
					getBoundingBox(), (e) -> true);
			if(counterCandidates.size() > 0)
			{
				EjectedCoreEntity target = counterCandidates.get(0);
				target.setVelocity(getRotationVector().rotateY(-90).add(0, 0.1, 0));
				dataTracker.set(ANIMATION, ANIMATION_COUNTER);
				dataTracker.set(ANIM_TIME, 0);
				dataTracker.set(COUNTER_COOLDOWN, 100);
			}
		}
		if(isCanDodge())
		{
			List<ShotgunPelletEntity> dodgeCandidates = getWorld().getEntitiesByType(TypeFilter.instanceOf(ShotgunPelletEntity.class),
					getBoundingBox().expand(5f), (e) -> true);
			if(dodgeCandidates.size() > 0)
			{
				Entity target = dodgeCandidates.get(0);
				boolean b = random.nextBoolean();
				Vec3d dir = target.getVelocity().normalize().multiply(3f).rotateY(b ? 75 : -75);
				setVelocity(dir);
				dataTracker.set(ANIMATION, ANIMATION_DODGE);
				dataTracker.set(ANIM_TIME, 0);
				dataTracker.set(DODGE_COOLDOWN, 100);
			}
		}
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(source.isOf(DamageSources.FLAMETHROWER))
			return false;
		if(source.isOf(DamageTypes.FALL) && getHealth() - amount <= 0)
			ExplosionHandler.explosion(this, getWorld(), getPos(), DamageSources.get(getWorld(), DamageTypes.EXPLOSION, this, this), 6, 4, 3, true);
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
					event.setAnimation(getVelocity().horizontalLength() < 0.15 ? WALK_ANIM : RUN_ANIM);
				}
				else
					event.setAnimation(dataTracker.get(ATTACKING) ? IDLE_AIM_ANIM : IDLE_ANIM);
			}
			case ANIMATION_DODGE -> {
				event.setAnimation(DODGE_ANIM);
				if(dataTracker.get(ANIM_TIME) > 15)
					dataTracker.set(ANIMATION, ANIMATION_IDLE);
			}
			case ANIMATION_COUNTER -> {
				event.setAnimation(COUNTER_ANIM);
				if(dataTracker.get(ANIM_TIME) > 14)
					dataTracker.set(ANIMATION, ANIMATION_IDLE);
			}
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
	
	@Override
	public boolean isFireImmune()
	{
		return true;
	}
	
	public boolean isCanDodge()
	{
		return dataTracker.get(DODGE_COOLDOWN) <= 0;
	}
	
	public boolean isCanCounter()
	{
		return dataTracker.get(COUNTER_COOLDOWN) <= 0;
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
				float targetYaw = -((float) MathHelper.atan2(e, f)) * MathHelper.DEGREES_PER_RADIAN;
				float phi = Math.abs(targetYaw - entity.getYaw()) % 360;
				float angleDelta = phi > 180 ? 360 - phi : phi;
				if(entity.getDataTracker().get(ROTATION_DELAY_COOLDOWN) == 0 && angleDelta > 60f)
				{
					entity.getDataTracker().set(ROTATION_DELAY, 10);
					entity.getDataTracker().set(ROTATION_DELAY_COOLDOWN, 40);
					entity.getNavigation().stop();
					return;
				}
				entity.headYaw = changeAngle(entity.headYaw, targetYaw, 20f);
				entity.setYaw(entity.headYaw);
			}
		}
	}
	
	static class StreetCleanerMoveControl extends MoveControl
	{
		public StreetCleanerMoveControl(MobEntity entity)
		{
			super(entity);
		}
		
		@Override
		public void tick()
		{
			 if (this.state == MoveControl.State.MOVE_TO)
			 {
				this.state = MoveControl.State.WAIT;
				double dx = targetX - entity.getX();
				double dz = targetZ - entity.getZ();
				double dy = targetY - entity.getY();
				double dist = dx * dx + dy * dy + dz * dz;
				if (dist < 0.1)
				{
					entity.setForwardSpeed(0.0F);
					return;
				}
				
				entity.setMovementSpeed((float)(speed * entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
				BlockPos blockPos = entity.getBlockPos();
				BlockState blockState = entity.getWorld().getBlockState(blockPos);
				VoxelShape voxelShape = blockState.getCollisionShape(entity.getWorld(), blockPos);
				if (dy > (double)entity.getStepHeight() && dx * dx + dz * dz < (double)Math.max(1.0F, entity.getWidth()) ||
							!voxelShape.isEmpty() && entity.getY() < voxelShape.getMax(Direction.Axis.Y) + (double)blockPos.getY() &&
									!blockState.isIn(BlockTags.DOORS) && !blockState.isIn(BlockTags.FENCES))
				{
					entity.getJumpControl().setActive();
					state = MoveControl.State.JUMPING;
				}
			 }
			 else if (state == MoveControl.State.JUMPING)
			 {
				entity.setMovementSpeed((float)(speed * entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
				if (entity.isOnGround())
					state = MoveControl.State.WAIT;
			 }
			 else
				entity.setForwardSpeed(0.0F);
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
			
			if(!cleaner.dataTracker.get(ATTACKING) && cleaner.dataTracker.get(ATTACK_COOLDOWN) == 0 && distance < 6f)
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
