package absolutelyaya.ultracraft.entity.demon;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.entity.projectile.CancerBulletEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.InstancedAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;

public class RodentEntity extends AbstractUltraHostileEntity implements GeoEntity
{
	protected static final TrackedData<Integer> SIZE = DataTracker.registerData(RodentEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(RodentEntity.class, TrackedDataHandlerRegistry.INTEGER);
	final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	
	public RodentEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		((LivingEntityAccessor)this).setTakePunchKnockbackSupplier(() -> false); //disable knockback
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.05d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new RodentAttackGoal(this));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(SIZE, 0);
		dataTracker.startTracking(ATTACK_COOLDOWN, 0);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if (data.equals(SIZE))
			calculateDimensions();
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(getTarget() != null)
		{
			double e = getTarget().getX() - getX();
			double f = getTarget().getZ() - getZ();
			float targetYaw = -((float) MathHelper.atan2(e, f)) * MathHelper.DEGREES_PER_RADIAN;
			setYaw(targetYaw);
			moveControl.strafeTo(0.33f, 0f);
		}
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
	
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
	public void tick()
	{
		super.tick();
		if(dataTracker.get(ATTACK_COOLDOWN) > 0)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
		
	}
	
	public int getSize()
	{
		return dataTracker.get(SIZE);
	}
	
	@Override
	public Text getName()
	{
		String prefix = Text.translatable("entity.ultracraft.rodent.prefix").getString().repeat(Math.max(0, getSize()));
		return Text.of(prefix + super.getName().getString());
	}
	
	void setSize(int size)
	{
		dataTracker.set(SIZE, size);
		refreshPosition();
		calculateDimensions();
		if(size > 0)
			getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10 * size * size * (Math.max(getWorld().getDifficulty().ordinal(), 1)));
		else
			getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(0.5);
		setHealth(getMaxHealth());
	}
	
	@Override
	public EntityDimensions getDimensions(EntityPose pose)
	{
		return super.getDimensions(pose).scaled(getSize() * 4);
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if(nbt.contains("Size", NbtElement.INT_TYPE))
			setSize(nbt.getInt("Size"));
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("Size", getSize());
	}
	
	private void fireProjectile()
	{
		CancerBulletEntity bullet = CancerBulletEntity.spawn(this, getWorld(), getTarget());
		bullet.setPosition(getPos().add(getRotationVector()));
		bullet.setVelocity(getRotationVector());
		bullet.setOwner(this);
		getWorld().spawnEntity(bullet);
	}
	
	@Override
	public void pushAwayFrom(Entity entity)
	{
		if(entity.isPlayer() && getSize() == 0)
			return;
		super.pushAwayFrom(entity);
	}
	
	@Override
	protected void pushAway(Entity entity)
	{
		if(entity.isPlayer() && getSize() == 0)
			return;
		super.pushAway(entity);
	}
	
	@Override
	public boolean isCollidable()
	{
		return getSize() > 0;
	}
	
	@Override
	public void takeKnockback(double strength, double x, double z)
	{
		if(getSize() == 0)
			super.takeKnockback(strength, x, z);
	}
	
	static class RodentAttackGoal extends Goal
	{
		final RodentEntity rodent;
		int shots;
		
		public RodentAttackGoal(RodentEntity rodent)
		{
			this.rodent = rodent;
		}
		
		@Override
		public boolean canStart()
		{
			return rodent.getSize() > 0 && rodent.getTarget() != null && rodent.dataTracker.get(ATTACK_COOLDOWN) <= 0f;
		}
		
		@Override
		public void start()
		{
			super.start();
			shots = 3;
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public void tick()
		{
			if(shots <= 0)
				return;
			rodent.fireProjectile();
			shots--;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return shots > 0;
		}
		
		@Override
		public boolean canStop()
		{
			return !shouldContinue();
		}
		
		@Override
		public void stop()
		{
			rodent.dataTracker.set(ATTACK_COOLDOWN, 60);
		}
	}
}
