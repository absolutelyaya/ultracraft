package absolutelyaya.ultracraft.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.Interruptable;
import absolutelyaya.ultracraft.entity.other.InterruptableCharge;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

public class StrayEntity extends AbstractHuskEntity implements GeoEntity, Interruptable
{
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(StrayEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
	private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenLoop("attack");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_ATTACK = 1;
	
	public StrayEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.5d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new GetOutOfMyPersonalSpaceGoal(this));
		goalSelector.add(1, new ThrowAttackGoal(this));
		goalSelector.add(2, new GetIntoSightGoal(this));
		
		targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ATTACK_COOLDOWN, 10);
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(getVelocity().y < 0f)
			amount *= 1.5;
		if(source.isFire())
			amount *= 0.5;
		return super.damage(source, amount);
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
				controller.setAnimation(event.isMoving() ? WALK_ANIM : IDLE_ANIM);
			}
			case ANIMATION_ATTACK -> controller.setAnimation(ATTACK_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		AnimationController<StrayEntity> controller = new AnimationController<>(this, "controller",
				2, this::predicate);
		controllerRegistrar.add(controller);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	private void ThrowBullet(LivingEntity target)
	{
		HellBulletEntity bullet = HellBulletEntity.spawn(this, world);
		double d = target.getEyeY() - 0f;
		double e = target.getX() - getX();
		double f = d - bullet.getY();
		double g = target.getZ() - getZ();
		bullet.setVelocity(e, f, g, 1f, 0.0f);
		bullet.setNoGravity(true);
		bullet.setIgnored(getClass());
		playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0f, 0.4f / (getRandom().nextFloat() * 0.4f + 0.8f));
		world.spawnEntity(bullet);
	}
	
	private InterruptableCharge addInterruptableCharge()
	{
		return InterruptableCharge.spawn(world, this, 26, 0.5f, 1f);
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
		LivingEntity target = getTarget();
		if(target != null && !isNavigating())
			getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ());
	}
	
	@Override
	public void onInterrupted(PlayerEntity interruptor)
	{
		world.playSound(null, interruptor.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.75f, 2f);
		Ultracraft.freeze((ServerWorld)world, 10);
		world.createExplosion(interruptor, getX(), getY(), getZ(), 2, World.ExplosionSourceType.NONE);
	}
	
	@Override
	public Vec3d getChargeOffset()
	{
		return new Vec3d(-0.4, 2.25, 0.5).rotateY((float)Math.toRadians(-bodyYaw));
	}
	
	static class GetOutOfMyPersonalSpaceGoal extends Goal
	{
		StrayEntity stray;
		PlayerEntity tooClose;
		int checkTimer = 10;
		
		public GetOutOfMyPersonalSpaceGoal(StrayEntity stray)
		{
			this.stray = stray;
		}
		
		@Override
		public boolean canStart()
		{
			if(checkTimer-- >= 0)
				return false;
			else
				checkTimer = 10;
			tooClose = stray.world.getClosestEntity(PlayerEntity.class, TargetPredicate.DEFAULT, stray,
					stray.getX(), stray.getY(), stray.getZ(), stray.getBoundingBox().expand(5));
			return tooClose != null && stray.getAnimation() != ANIMATION_ATTACK;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return tooClose != null && tooClose.isAlive() && stray.squaredDistanceTo(tooClose) < 5 * 5;
		}
		
		@Override
		public boolean canStop()
		{
			return !shouldContinue();
		}
		
		@Override
		public void tick()
		{
			Vec3d dir = stray.getPos().subtract(tooClose.getPos()).normalize().multiply(7);
			stray.navigation.startMovingTo(stray.getX() + dir.x, stray.getY() + dir.y, stray.getZ() + dir.z, 1);
		}
		
		@Override
		public void stop()
		{
			tooClose = null;
		}
	}
	
	static class ThrowAttackGoal extends Goal
	{
		StrayEntity stray;
		LivingEntity target;
		InterruptableCharge charge;
		int time;
		
		public ThrowAttackGoal(StrayEntity stray)
		{
			this.stray = stray;
			setControls(EnumSet.of(Control.LOOK));
		}
		
		@Override
		public boolean canStart()
		{
			target = stray.getTarget();
			if (target == null || stray.dataTracker.get(ATTACK_COOLDOWN) > 0 || stray.navigation.isFollowingPath())
				return false;
			return stray.isOnGround() && stray.getAnimation() != ANIMATION_ATTACK && stray.canSee(target);
		}
		
		@Override
		public void start()
		{
			time = 0;
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public void tick()
		{
			stray.getNavigation().stop();
			stray.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ());
			
			if(time == 0)
				stray.dataTracker.set(ANIMATION, ANIMATION_ATTACK);
			if(time == 12)
				charge = stray.addInterruptableCharge();
			if (time == 27)
			{
				stray.ThrowBullet(target);
				if(charge != null && !charge.isRemoved())
					charge.discard();
			}
			
			time++;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return time < 42;
		}
		
		@Override
		public boolean canStop()
		{
			return !shouldContinue();
		}
		
		@Override
		public void stop()
		{
			if(stray.getAnimation() == ANIMATION_ATTACK)
				stray.dataTracker.set(ANIMATION, ANIMATION_IDLE);
			stray.dataTracker.set(ATTACK_COOLDOWN, (int)(40 + stray.getRandom().nextFloat() * 40));
			if(charge != null && !charge.isRemoved())
				charge.discard();
		}
	}
}
