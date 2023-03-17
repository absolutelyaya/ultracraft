package absolutelyaya.ultracraft.entity.demon;

import absolutelyaya.ultracraft.accessor.IAnimatedEnemy;
import absolutelyaya.ultracraft.entity.goal.TimedAttackGoal;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
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

public class CerberusEntity extends HostileEntity implements GeoEntity, IAnimatedEnemy
{
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(CerberusEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
	private static final RawAnimation THROW_ANIM = RawAnimation.begin().thenLoop("throw");
	private static final RawAnimation RAM_ANIM = RawAnimation.begin().thenLoop("ram");
	private static final RawAnimation STOMP_ANIM = RawAnimation.begin().thenLoop("stomp");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(CerberusEntity.class, TrackedDataHandlerRegistry.BYTE);
	protected static final TrackedData<Boolean> ENRAGED = DataTracker.registerData(CerberusEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_THROW = 1;
	private static final byte ANIMATION_RAM = 2;
	private static final byte ANIMATION_STOMP = 3;
	
	public CerberusEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		stepHeight = 1f;
	}
	
	//TODO: Throw Attack
	//TODO: Stomp Attack
	//TODO: Cracked and Enraged Textures
	//TODO: Ball Projectile
	//TODO: Add Ball to Model
	//TODO: Sitting Cerberus Block & Transition Animation to Entity
	//TODO: approach target Goal;
	
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
		targetSelector.add(0, new ThrowAttackGoal(this));
		targetSelector.add(1, new RamAttackGoal(this));
		targetSelector.add(2, new StepOnMeUwUGoal(this));
		
		targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ANIMATION, (byte)0);
		dataTracker.startTracking(ATTACK_COOLDOWN, 10);
		dataTracker.startTracking(ENRAGED, false);
	}
	
	private <E extends GeoEntity> PlayState predicate(AnimationState<E> event)
	{
		byte anim = dataTracker.get(ANIMATION);
		AnimationController<?> controller = event.getController();
		
		controller.setAnimationSpeed(getAnimSpeedMult());
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
	
	@Override
	public byte getAnimation()
	{
		return dataTracker.get(ANIMATION);
	}
	
	@Override
	public void setAnimation(byte id)
	{
		dataTracker.set(ANIMATION, id);
	}
	
	public void enrage()
	{
		dataTracker.set(ENRAGED, true);
		playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1f);
	}
	
	public boolean isEnraged()
	{
		return dataTracker.get(ENRAGED);
	}
	
	@Override
	public float getAnimSpeedMult()
	{
		return isEnraged() ? 2f : 1f;
	}
	
	@Override
	public void setCooldown(int cooldown)
	{
		dataTracker.set(ATTACK_COOLDOWN, cooldown);
	}
	
	@Override
	public int getCooldown()
	{
		return dataTracker.get(ATTACK_COOLDOWN);
	}
	
	@Override
	public boolean isHeadFixed()
	{
		return getAnimation() != ANIMATION_IDLE;
	}
	
	@Override
	public int getMaxHeadRotation()
	{
		return isHeadFixed() ? 0 : 75;
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
		playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0f, 0.2f / (getRandom().nextFloat() * 0.2f + 0.4f));
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
		if(isHeadFixed())
			bodyYaw = headYaw;
	}
	
	@Override
	public void onDeath(DamageSource damageSource)
	{
		super.onDeath(damageSource);
		world.getEntitiesByType(TypeFilter.instanceOf(CerberusEntity.class), getBoundingBox().expand(64),
						e -> !e.isEnraged() && e != this).forEach(CerberusEntity::enrage);
	}
	
	static class ThrowAttackGoal extends TimedAttackGoal<CerberusEntity>
	{
		public ThrowAttackGoal(CerberusEntity cerb)
		{
			super(cerb, ANIMATION_IDLE, ANIMATION_THROW, 34);
		}
		
		@Override
		public boolean canStart()
		{
			return super.canStart() && mob.getRandom().nextInt(10) == 0;
		}
		
		@Override
		public void tick()
		{
			super.tick();
			mob.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ());
		}
	}
	
	static class RamAttackGoal extends TimedAttackGoal<CerberusEntity>
	{
		Vec3d ramDir;
		
		public RamAttackGoal(CerberusEntity cerb)
		{
			super(cerb, ANIMATION_IDLE, ANIMATION_RAM, 40);
			setControls(EnumSet.of(Control.LOOK));
		}
		
		@Override
		public boolean canStart()
		{
			return super.canStart() && mob.getRandom().nextInt(1) == 0;
		}
		
		@Override
		public void tick()
		{
			super.tick();
			if(timer > 15 && timer < 22)
				mob.setVelocity(ramDir);
			else if (timer == 14)
				ramDir = target.getPos().subtract(mob.getPos()).multiply(1, 0, 1).normalize();
			else if(timer < 15)
				mob.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ(), 90, 90);
			
			if(timer > 15 && timer < 30)
			{
				mob.world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), mob.getBoundingBox().expand(0.2), p -> true)
						.forEach(p -> {
							p.damage(DamageSource.mob(mob), 8f);
							p.setVelocity(ramDir.multiply(3).add(0.0, 0.5, 0.0));
						});
				Vec3d lookPos = mob.getPos().add(ramDir);
				mob.getLookControl().lookAt(lookPos.x, mob.getEyeY(), lookPos.z, 90, 90);
			}
		}
	}
	
	static class StepOnMeUwUGoal extends TimedAttackGoal<CerberusEntity>
	{
		public StepOnMeUwUGoal(CerberusEntity cerb)
		{
			super(cerb, ANIMATION_IDLE, ANIMATION_STOMP, 26);
		}
		
		@Override
		public boolean canStart()
		{
			return super.canStart() && mob.getRandom().nextInt(10) == 0;
		}
		
		@Override
		public void tick()
		{
			super.tick();
			mob.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ());
		}
	}
}
