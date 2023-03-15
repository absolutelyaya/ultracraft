package absolutelyaya.ultracraft.entity.husk;

import absolutelyaya.ultracraft.accessor.Interruptable;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
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

public class SchismEntity extends AbstractHuskEntity implements GeoEntity, Interruptable
{
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(SchismEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
	private static final RawAnimation ATTACK_VERTICAL_ANIM = RawAnimation.begin().thenLoop("attackVert");
	private static final RawAnimation ATTACK_HORIZONTAL_ANIM = RawAnimation.begin().thenLoop("attackHor");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_ATTACK_VERTICAL = 1;
	private static final byte ANIMATION_ATTACK_HORIZONTAL = 2;
	
	public SchismEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	//TODO: Interuptable Attack Charge Time
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0d)
					   .add(EntityAttributes.GENERIC_ARMOR, 2.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0d);
	}
	
	@Override
	protected void initGoals()
	{
		targetSelector.add(0, new GetIntoSightGoal(this));
		targetSelector.add(1, new AttackGoal(this));
		targetSelector.add(2, new WanderAroundGoal(this, 1f));
		
		targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ATTACK_COOLDOWN, 10);
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
				if(event.isMoving())
					controller.setAnimation(WALK_ANIM);
				else
					return PlayState.STOP;
			}
			case ANIMATION_ATTACK_VERTICAL -> controller.setAnimation(ATTACK_VERTICAL_ANIM);
			case ANIMATION_ATTACK_HORIZONTAL -> controller.setAnimation(ATTACK_HORIZONTAL_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		AnimationController<SchismEntity> controller = new AnimationController<>(this, "controller",
				2, this::predicate);
		controllerRegistrar.add(controller);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
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
		setBodyYaw(headYaw);
	}
	
	private void ShootBullet(float degrees, boolean vertical)
	{
		HellBulletEntity bullet = HellBulletEntity.spawn(this, world);
		Vec3d dir = new Vec3d(0f, 0f, 1f);
		if(vertical)
			dir = dir.rotateX((float)Math.toRadians(-degrees));
		dir = dir.rotateY((float)Math.toRadians(-getBodyYaw()));
		if(!vertical)
			dir = dir.rotateY((float)Math.toRadians(degrees));
		bullet.setVelocity(dir.x, dir.y, dir.z, 1f, 0.0f);
		bullet.setNoGravity(true);
		bullet.setIgnored(getClass());
		playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0f, 0.4f / (getRandom().nextFloat() * 0.4f + 0.8f));
		world.spawnEntity(bullet);
	}
	
	@Override
	public void onInterrupted(PlayerEntity interruptor)
	{
	
	}
	
	static class AttackGoal extends Goal
	{
		SchismEntity schism;
		LivingEntity target;
		boolean vertical;
		int shot, timer;
		
		public AttackGoal(SchismEntity schism)
		{
			this.schism = schism;
		}
		
		@Override
		public boolean canStart()
		{
			target = schism.getTarget();
			if (target == null || schism.dataTracker.get(ATTACK_COOLDOWN) > 0 || schism.navigation.isFollowingPath())
				return false;
			return schism.canSee(target);
		}
		
		@Override
		public void start()
		{
			if(target.getY() - 2f > schism.getY())
				vertical = true;
			else
				vertical = schism.getRandom().nextBoolean();
			shot = timer = 0;
			schism.dataTracker.set(ANIMATION, vertical ? ANIMATION_ATTACK_VERTICAL : ANIMATION_ATTACK_HORIZONTAL);
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public void tick()
		{
			schism.navigation.stop();
			//.7 / 5 = .14 = 2
			if(++timer > 30 && timer < 44)
			{
				if((timer / 20f) % 0.14f < 0.05f)
				{
					float degrees = 30 * (shot - 2);
					schism.ShootBullet(degrees, vertical);
					shot++;
				}
			}
			else if(timer < 30)
				schism.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ());
		}
		
		@Override
		public boolean shouldContinue()
		{
			if(vertical)
				return timer < 44;
			else
				return timer < 50;
		}
		
		@Override
		public boolean canStop()
		{
			return !shouldContinue();
		}
		
		@Override
		public void stop()
		{
			if(schism.getAnimation() == ANIMATION_ATTACK_HORIZONTAL || schism.getAnimation() == ANIMATION_ATTACK_VERTICAL)
				schism.dataTracker.set(ANIMATION, ANIMATION_IDLE);
			schism.dataTracker.set(ATTACK_COOLDOWN, (int)(40 + 50 * schism.getRandom().nextFloat()));
		}
	}
}
