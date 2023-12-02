package absolutelyaya.ultracraft.entity.husk;

import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.accessor.IAnimatedEnemy;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.entity.goal.TimedAttackGoal;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GreaterFilthEntity extends AbstractHuskEntity implements GeoEntity, IAnimatedEnemy, Enrageable
{
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
	private static final RawAnimation HOOK_ANIM = RawAnimation.begin().thenLoop("hook");
	private static final RawAnimation COMBO_ANIM = RawAnimation.begin().thenLoop("combo");
	private static final RawAnimation ONEPUNCH_ANIM = RawAnimation.begin().thenLoop("onepunch");
	private static final RawAnimation DODGE_ANIM = RawAnimation.begin().thenLoop("dodge");
	private static final RawAnimation ENRAGE_ANIM = RawAnimation.begin().thenLoop("enrage");
	private static final RawAnimation ENRAGED_IDLE_ANIM = RawAnimation.begin().thenLoop("enragedIdle");
	private static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
	private static final RawAnimation UPPERCUT_SLAM_ANIM = RawAnimation.begin().thenLoop("uppercutSlam");
	private static final RawAnimation NIBBLE_ANIM = RawAnimation.begin().thenLoop("nibble");
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	protected static final TrackedData<Boolean> RARE = DataTracker.registerData(GreaterFilthEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(GreaterFilthEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> DODGE_TICKS = DataTracker.registerData(GreaterFilthEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> FRUSTRATION_TICKS = DataTracker.registerData(GreaterFilthEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> ENRAGE_TICKS = DataTracker.registerData(GreaterFilthEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_HOOK = 1;
	private static final byte ANIMATION_COMBO = 2;
	private static final byte ANIMATION_ONEPUNCH = 3;
	private static final byte ANIMATION_DODGE = 4;
	private static final byte ANIMATION_ENRAGE = 5;
	private static final byte ANIMATION_UPPERCUT_SLAM = 6;
	
	public GreaterFilthEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		if(!world.isClient)
			dataTracker.set(RARE, random.nextInt(10000) == 0);
		((LivingEntityAccessor)this).setTakePunchKnockbackSupplier(() -> false);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(RARE, false);
		dataTracker.startTracking(ATTACK_COOLDOWN, 30);
		dataTracker.startTracking(DODGE_TICKS, 0);
		dataTracker.startTracking(FRUSTRATION_TICKS, 0);
		dataTracker.startTracking(ENRAGE_TICKS, 0);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(DODGE_TICKS))
		{
			if(dataTracker.get(DODGE_TICKS) == 0)
				setAnimation(ANIMATION_IDLE);
			if(dataTracker.get(DODGE_TICKS) == 12)
				setAnimation(ANIMATION_DODGE);
		}
		if(data.equals(ENRAGE_TICKS) && getAnimation() == ANIMATION_ENRAGE && dataTracker.get(ENRAGE_TICKS) <= 400)
			setAnimation(ANIMATION_IDLE);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new HookAttackGoal(this));
		goalSelector.add(0, new OnePunchAttackGoal(this));
		goalSelector.add(0, new ComboAttackGoal(this));
		goalSelector.add(1, new ApproachTargetGoal(this));
		
		targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		targetSelector.add(2, new RevengeGoal(this));
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 200d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d);
	}
	
	@Override
	protected boolean getBossDefault()
	{
		return true;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		AnimationController<GreaterFilthEntity> controller = new AnimationController<>(this, "controller",
				2, this::predicate);
		controllerRegistrar.add(controller);
	}
	
	private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event)
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
					controller.setAnimation(isEnraged() ? RUN_ANIM : WALK_ANIM);
				else
					controller.setAnimation(isEnraged() ? ENRAGED_IDLE_ANIM : IDLE_ANIM);
			}
			case ANIMATION_HOOK -> controller.setAnimation(HOOK_ANIM);
			case ANIMATION_ONEPUNCH -> controller.setAnimation(ONEPUNCH_ANIM);
			case ANIMATION_COMBO -> controller.setAnimation(COMBO_ANIM);
			case ANIMATION_DODGE -> controller.setAnimation(DODGE_ANIM);
			case ANIMATION_ENRAGE -> controller.setAnimation(ENRAGE_ANIM);
		}
		return PlayState.CONTINUE;
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
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - (isEnraged() ? 10 : 1));
		if(dataTracker.get(DODGE_TICKS) > 0)
			dataTracker.set(DODGE_TICKS, dataTracker.get(DODGE_TICKS) - 1);
		if(dataTracker.get(ENRAGE_TICKS) > 0)
			dataTracker.set(ENRAGE_TICKS, dataTracker.get(ENRAGE_TICKS) - 1);
		if(!isEnraged())
			dataTracker.set(FRUSTRATION_TICKS, dataTracker.get(FRUSTRATION_TICKS) + 1);
		if(dataTracker.get(FRUSTRATION_TICKS) > 400 && !isEnraged())
			enrage();
	}
	
	void enrage()
	{
		dataTracker.set(ENRAGE_TICKS, 438);
		setAnimation(ANIMATION_ENRAGE);
		playSound(SoundRegistry.GENERIC_ENRAGE, 1.5f, 0.9f);
	}
	
	@Override
	public void setAnimation(byte id)
	{
		dataTracker.set(ANIMATION, id);
	}
	
	@Override
	public int getAnimSpeedMult()
	{
		return 1;
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
	
	public boolean isDodging()
	{
		return dataTracker.get(DODGE_TICKS) < 0;
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
	public boolean damage(DamageSource source, float amount)
	{
		if(source.isIn(DamageTypeTags.MELEE) && !getWorld().isClient)
		{
			dataTracker.set(DODGE_TICKS, 12);
			Entity attacker = source.getAttacker();
			if(attacker != null)
			{
				lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, attacker.getEyePos());
				setVelocity(getPos().subtract(attacker.getPos()).normalize().multiply(0.75f));
			}
			return false;
		}
		amount /= 2f;
		return super.damage(source, amount);
	}
	
	@Override
	public void takeKnockback(double strength, double x, double z)
	{
	
	}
	
	@Override
	public boolean isEnraged()
	{
		return dataTracker.get(ENRAGE_TICKS) > 0;
	}
	
	@Override
	public Vec3d getEnrageFeatureSize()
	{
		return new Vec3d(1f, -1f, -1f);
	}
	
	@Override
	public Vec3d getEnragedFeatureOffset()
	{
		return new Vec3d(0f, -1.7f, 0f);
	}
	
	static class ApproachTargetGoal extends Goal
	{
		
		final protected GreaterFilthEntity mob;
		LivingEntity target;
		
		public ApproachTargetGoal(GreaterFilthEntity mob)
		{
			this.mob = mob;
			setControls(EnumSet.of(Control.LOOK, Control.MOVE));
		}
		
		@Override
		public boolean canStart()
		{
			target = mob.getTarget();
			return target != null && mob.isOnGround() && mob.getAnimation() == ANIMATION_IDLE;
		}
		
		@Override
		public void start()
		{
			mob.getNavigation().startMovingTo(target, mob.isEnraged() ? 2.5f : 1f);
		}
		
		@Override
		public boolean shouldContinue()
		{
			return false;
		}
	}
	
	static class HookAttackGoal extends TimedAttackGoal<GreaterFilthEntity>
	{
		Vec3d dir;
		final List<PlayerEntity> hits = new ArrayList<>();
		
		public HookAttackGoal(GreaterFilthEntity filth)
		{
			super(filth, ANIMATION_IDLE, ANIMATION_HOOK, 25);
			setControls(EnumSet.of(Control.LOOK));
			baseCooldown = 20;
			randomCooldownRange = 10;
		}
		
		@Override
		public boolean canStart()
		{
			if(mob.isDodging())
				return false;
			return super.canStart() && mob.distanceTo(mob.getTarget()) < 5f && mob.getRandom().nextInt(3) == 0;
		}
		
		@Override
		public void start()
		{
			super.start();
			mob.getNavigation().stop();
			hits.clear();
			mob.getDataTracker().set(FRUSTRATION_TICKS, mob.getDataTracker().get(FRUSTRATION_TICKS) - 100);
		}
		
		@Override
		protected void process()
		{
			super.process();
			if(timer == 7)
				dir = target.getPos().subtract(mob.getPos()).multiply(1, 0, 1).normalize();
			else if (timer == 8)
				mob.setVelocity(dir);
			else if(timer < 8)
				mob.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ(), 90, 90);
			
			if(timer >= 8 && timer <= 10)
			{
				mob.getWorld().getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), mob.getBoundingBox().expand(0.2), p -> true)
						.forEach(p -> {
							if(!hits.contains(p))
							{
								p.damage(DamageSources.get(mob.getWorld(), DamageSources.OBLITERATION, mob),
										(float)mob.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.5f);
								p.setVelocity(dir.multiply(3).add(0.0, 0.25, 0.0).rotateY((float)Math.toRadians(45f)));
								hits.add(p);
							}
							mob.dataTracker.set(FRUSTRATION_TICKS, 0);
						});
				Vec3d lookPos = mob.getPos().add(dir.multiply(10));
				mob.getLookControl().lookAt(lookPos.x, mob.getEyeY(), lookPos.z, 90, 90);
			}
		}
		
		@Override
		public boolean shouldContinue()
		{
			if(mob.isDodging())
				return false;
			return super.shouldContinue();
		}
		
		@Override
		public void stop()
		{
			super.stop();
			hits.clear();
		}
	}
	
	static class OnePunchAttackGoal extends TimedAttackGoal<GreaterFilthEntity>
	{
		Vec3d dir;
		final List<PlayerEntity> hits = new ArrayList<>();
		
		public OnePunchAttackGoal(GreaterFilthEntity filth)
		{
			super(filth, ANIMATION_IDLE, ANIMATION_ONEPUNCH, 50);
			setControls(EnumSet.of(Control.LOOK));
			baseCooldown = 30;
			randomCooldownRange = 15;
		}
		
		@Override
		public boolean canStart()
		{
			if(mob.isDodging())
				return false;
			return super.canStart() && mob.distanceTo(mob.getTarget()) < 7f && mob.getRandom().nextInt(8) == 0;
		}
		
		@Override
		public void start()
		{
			super.start();
			mob.getNavigation().stop();
			hits.clear();
			mob.getDataTracker().set(FRUSTRATION_TICKS, mob.getDataTracker().get(FRUSTRATION_TICKS) - 100);
		}
		
		@Override
		protected void process()
		{
			super.process();
			if(timer == 31)
				dir = target.getPos().subtract(mob.getPos()).multiply(1, 0, 1).normalize();
			else if (timer >= 32 && timer <= 38)
				mob.setVelocity(dir);
			if(timer < 34)
				mob.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ(), 90, 90);
			
			if(timer >= 34 && timer <= 43)
			{
				mob.getWorld().getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), mob.getBoundingBox().expand(0.2), p -> true)
						.forEach(p -> {
							if(!hits.contains(p))
							{
								p.damage(DamageSources.get(mob.getWorld(), DamageSources.OBLITERATION, mob),
										(float)mob.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 5f);
								p.setVelocity(dir.multiply(3).add(0.0, 0.5, 0.0));
								hits.add(p);
							}
							mob.dataTracker.set(FRUSTRATION_TICKS, 0);
						});
				Vec3d lookPos = mob.getPos().add(dir.multiply(10));
				mob.getLookControl().lookAt(lookPos.x, mob.getEyeY(), lookPos.z, 90, 90);
			}
		}
		
		@Override
		public boolean shouldContinue()
		{
			if(mob.isDodging())
				return false;
			return super.shouldContinue();
		}
		
		@Override
		public void stop()
		{
			super.stop();
			hits.clear();
		}
	}
	
	static class ComboAttackGoal extends TimedAttackGoal<GreaterFilthEntity>
	{
		Vec3d dir;
		final List<PlayerEntity> hits = new ArrayList<>();
		
		public ComboAttackGoal(GreaterFilthEntity filth)
		{
			super(filth, ANIMATION_IDLE, ANIMATION_COMBO, 50);
			setControls(EnumSet.of(Control.LOOK));
			baseCooldown = 30;
			randomCooldownRange = 25;
		}
		
		@Override
		public boolean canStart()
		{
			if(mob.isDodging())
				return false;
			return super.canStart() && mob.distanceTo(mob.getTarget()) < 5f && mob.getRandom().nextInt(3) == 0;
		}
		
		@Override
		public void start()
		{
			super.start();
			mob.getNavigation().stop();
			hits.clear();
			mob.getDataTracker().set(FRUSTRATION_TICKS, mob.getDataTracker().get(FRUSTRATION_TICKS) - 100);
		}
		
		@Override
		protected void process()
		{
			super.process();
			if(timer == 5 || timer == 11 || timer == 23)
				dir = target.getPos().subtract(mob.getPos()).multiply(1, 0, 1).normalize();
			else if (timer == 6 || timer == 12)
				mob.setVelocity(dir);
			else if (timer == 24)
				mob.setVelocity(dir.multiply(0.5f));
			if(timer < 6 || timer == 12 || timer == 24)
				mob.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ(), 90, 90);
			
			if((timer >= 6 && timer <= 10) || (timer >= 12 && timer <= 15) || (timer >= 25 && timer <= 29))
			{
				mob.getWorld().getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), mob.getBoundingBox().expand(0.2), p -> true)
						.forEach(p -> {
							if(!hits.contains(p))
							{
								p.damage(DamageSources.get(mob.getWorld(), DamageSources.OBLITERATION, mob),
										(float)mob.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.5f);
								p.setVelocity(dir.multiply(1.5).add(0.0, 0.25, 0.0));
								hits.add(p);
							}
							mob.dataTracker.set(FRUSTRATION_TICKS, 0);
						});
				Vec3d lookPos = mob.getPos().add(dir.multiply(10));
				mob.getLookControl().lookAt(lookPos.x, mob.getEyeY(), lookPos.z, 90, 90);
			}
			if(timer == 11 || timer == 24)
				hits.clear();
		}
		
		@Override
		public boolean shouldContinue()
		{
			if(mob.isDodging())
				return false;
			return super.shouldContinue();
		}
		
		@Override
		public void stop()
		{
			super.stop();
			hits.clear();
		}
	}
}
