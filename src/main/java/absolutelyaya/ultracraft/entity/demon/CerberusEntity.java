package absolutelyaya.ultracraft.entity.demon;

import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CerberusEntity extends HostileEntity implements GeoEntity
{
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(CerberusEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
	private static final RawAnimation THROW_ANIM = RawAnimation.begin().thenLoop("throw");
	private static final RawAnimation RAM_ANIM = RawAnimation.begin().thenLoop("ram");
	private static final RawAnimation STOMP_ANIM = RawAnimation.begin().thenLoop("stomp");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(CerberusEntity.class, TrackedDataHandlerRegistry.BYTE);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_THROW = 1;
	private static final byte ANIMATION_RAM = 2;
	private static final byte ANIMATION_STOMP = 3;
	
	public CerberusEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	//TODO: interruptable attack charge time
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0d)
					   .add(EntityAttributes.GENERIC_ARMOR, 6.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0d);
	}
	
	@Override
	protected void initGoals()
	{
		
		targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ANIMATION, (byte)0);
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
				controller.setAnimation(event.isMoving() ? WALK_ANIM : IDLE_ANIM);
			}
			case ANIMATION_THROW -> controller.setAnimation(THROW_ANIM);
			case ANIMATION_RAM -> controller.setAnimation(RAM_ANIM);
			case ANIMATION_STOMP -> controller.setAnimation(STOMP_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		AnimationController<CerberusEntity> controller = new AnimationController<>(this, "controller",
				2, this::predicate);
		controllerRegistrar.add(controller);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	public byte getAnimation()
	{
		return dataTracker.get(ANIMATION);
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
}
