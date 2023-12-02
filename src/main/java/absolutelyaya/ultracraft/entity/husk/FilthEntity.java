package absolutelyaya.ultracraft.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.MeleeInterruptable;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;

import java.util.EnumSet;
import java.util.List;

public class FilthEntity extends AbstractHuskEntity implements GeoEntity, MeleeInterruptable
{
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
	private static final RawAnimation ATTACK_LUNGE_ANIM = RawAnimation.begin().thenLoop("lunge");
	private static final RawAnimation ATTACK_MOVING_ANIM = RawAnimation.begin().thenLoop("attackMoving");
	private static final RawAnimation ATTACK_STATIONARY_ANIM = RawAnimation.begin().thenLoop("attackStationary");
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	protected static final TrackedData<Boolean> RARE = DataTracker.registerData(FilthEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(FilthEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_ATTACK_LUNGE = 1;
	private static final byte ANIMATION_ATTACK_MOVING = 2;
	private static final byte ANIMATION_ATTACK_STATIONARY = 3;
	
	public FilthEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0f);
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
		goalSelector.add(0, new FilthLungeAttackGoal(this, 1.5f));
		goalSelector.add(0, new FilthMovingAttackGoal(this, 1.25f));
		goalSelector.add(0, new FilthStationaryAttackGoal(this, 1f));
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
			case ANIMATION_ATTACK_LUNGE -> controller.setAnimation(ATTACK_LUNGE_ANIM);
			case ANIMATION_ATTACK_MOVING -> controller.setAnimation(ATTACK_MOVING_ANIM);
			case ANIMATION_ATTACK_STATIONARY -> controller.setAnimation(ATTACK_STATIONARY_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(dataTracker.get(ATTACK_COOLDOWN) > 0)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(getAnimation() == ANIMATION_ATTACK_LUNGE && !Ultracraft.isTimeFrozen())
		{
			if(getVelocity().length() > 0.1)
				lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, getPos().add(getVelocity().normalize()));
			else if(getTarget() != null)
				lookAtEntity(getTarget(), 180, 180);
			setBodyYaw(headYaw);
		}
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(source.isOf(DamageSources.NAIL))
			amount *= 2f;
		return super.damage(source, amount);
	}
	
	@Override
	protected void pushAway(Entity entity)
	{
		if(!isInAttackAnimation())
			super.pushAway(entity);
	}
	
	@Override
	public void pushAwayFrom(Entity entity)
	{
		if(!isInAttackAnimation())
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
	
	public boolean isInAttackAnimation()
	{
		byte anim = getAnimation();
		return anim == ANIMATION_ATTACK_LUNGE || anim == ANIMATION_ATTACK_MOVING || anim == ANIMATION_ATTACK_STATIONARY;
	}
	
	void delayNearby() //prevent insta-death through Filth Swarms
	{
		List<Entity> nearby = getWorld().getOtherEntities(this, getBoundingBox().expand(6), e -> e instanceof FilthEntity);
		nearby.forEach(e -> {
			if(e instanceof FilthEntity filth)
				filth.dataTracker.set(ATTACK_COOLDOWN, filth.dataTracker.get(ATTACK_COOLDOWN) + 5);
		});
	}
	
	static class FilthAttackGoal extends Goal
	{
		final protected FilthEntity mob;
		final protected float velocity;
		final byte animationID;
		final boolean hop, stopMoving;
		final Vector2i interruptPeriod, damagePeriod;
		LivingEntity target;
		int time;
		boolean didDamage;
		
		public FilthAttackGoal(FilthEntity mob, float velocity, byte animationID, boolean hop, boolean stopMoving, Vector2i interruptPeriod, Vector2i damagePeriod)
		{
			this.mob = mob;
			this.velocity = velocity;
			this.animationID = animationID;
			this.hop = hop;
			this.stopMoving = stopMoving;
			this.interruptPeriod = interruptPeriod;
			this.damagePeriod = damagePeriod;
			setControls(EnumSet.of(Control.LOOK, Control.MOVE));
		}
		
		@Override
		public boolean canStart()
		{
			target = mob.getTarget();
			if (target == null || mob.dataTracker.get(ATTACK_COOLDOWN) > 0)
				return false;
			double d = mob.squaredDistanceTo(target);
			if (d > 16.0 * 16.0)
				return false;
			return mob.isOnGround() && !mob.isInAttackAnimation();
		}
		
		protected int getApplyVelocityFrame()
		{
			return -1;
		}
		
		protected int getAnimLength()
		{
			return 0;
		}
		
		@Override
		public void start()
		{
			mob.delayNearby();
			time = 0;
			didDamage = false;
		}
		
		@Override
		public void tick()
		{
			if(time < getApplyVelocityFrame())
			{
				mob.lookAtEntity(target, 360, 360);
				double d = mob.squaredDistanceTo(target);
				if (d > 3.0 * 3.0 && mob.getAnimation() != animationID)
				{
					mob.getNavigation().startMovingTo(target, 1.0);
					return;
				}
				if(stopMoving)
					mob.getNavigation().stop();
			}
			mob.setBodyYaw(mob.headYaw);
			mob.setAttacking(time > interruptPeriod.x && time < interruptPeriod.y);
			if(time == 0)
				mob.dataTracker.set(ANIMATION, animationID);
			if (time == getApplyVelocityFrame())
			{
				mob.playSound(SoundRegistry.FILTH_ATTACK, 1f, 1f);
				if(target != null)
				{
					Vec3d vec3d2 = new Vec3d(target.getX() - mob.getX(), 0.0, target.getZ() - mob.getZ());
					vec3d2 = vec3d2.normalize().multiply(velocity);
					mob.setVelocity(vec3d2.x, hop ? velocity / 5f : 0f, vec3d2.z);
				}
			}
			if(!didDamage && (time > damagePeriod.x && time < damagePeriod.y) && mob.getBoundingBox().expand(0.2f).intersects(target.getBoundingBox()))
				didDamage = mob.tryAttack(target);
			time++;
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return time < getAnimLength() && mob.squaredDistanceTo(target) < 24.0 * 24.0;
		}
		
		@Override
		public boolean canStop()
		{
			return !shouldContinue();
		}
		
		@Override
		public void stop()
		{
			if(mob.getAnimation() == animationID)
				mob.dataTracker.set(ANIMATION, ANIMATION_IDLE);
			mob.dataTracker.set(ATTACK_COOLDOWN, 10 + mob.random.nextInt(10));
			mob.setAttacking(false);
		}
	}
	
	static class FilthStationaryAttackGoal extends FilthAttackGoal
	{
		public FilthStationaryAttackGoal(FilthEntity entity, float velocity)
		{
			super(entity, velocity, ANIMATION_ATTACK_STATIONARY, false, true, new Vector2i(8, 15), new Vector2i(15, 25));
		}
		
		@Override
		protected int getApplyVelocityFrame()
		{
			return 15;
		}
		
		@Override
		protected int getAnimLength()
		{
			return 34;
		}
		
		@Override
		public boolean canStart()
		{
			return super.canStart() && mob.random.nextInt(1) == 0;
		}
	}
	
	static class FilthMovingAttackGoal extends FilthAttackGoal
	{
		public FilthMovingAttackGoal(FilthEntity entity, float velocity)
		{
			super(entity, velocity, ANIMATION_ATTACK_MOVING, false, true, new Vector2i(5, 12), new Vector2i(10, 20));
		}
		
		@Override
		protected int getApplyVelocityFrame()
		{
			return 5;
		}
		
		@Override
		protected int getAnimLength()
		{
			return 31;
		}
		
		@Override
		public boolean canStart()
		{
			return super.canStart() && mob.random.nextInt(2) == 0;
		}
	}
	
	static class FilthLungeAttackGoal extends FilthAttackGoal
	{
		public FilthLungeAttackGoal(FilthEntity entity, float velocity)
		{
			super(entity, velocity, ANIMATION_ATTACK_LUNGE, true, true, new Vector2i(8, 16), new Vector2i(15, 25));
		}
		
		@Override
		public boolean canStart()
		{
			return super.canStart() && mob.random.nextInt(6) == 0;
		}
		
		@Override
		protected int getApplyVelocityFrame()
		{
			return 12;
		}
		
		@Override
		protected int getAnimLength()
		{
			return 70;
		}
	}
}
