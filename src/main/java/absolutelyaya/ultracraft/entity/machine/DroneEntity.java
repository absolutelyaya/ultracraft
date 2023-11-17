package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.accessor.MeleeInterruptable;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraFlyingEntity;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import mod.azure.azurelib.core.animation.AnimationState;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.*;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3f;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.InstancedAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.object.PlayState;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class DroneEntity extends AbstractUltraFlyingEntity implements GeoEntity, MeleeInterruptable
{
	private final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	protected static final TrackedData<Vector3f> FALLROT = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	protected static final TrackedData<Vector3f> FALLDIR = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	protected static final TrackedData<Integer> FALLING = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> MOVE_COOLDOWN = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Boolean> KEEP_HEIGHT = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Optional<UUID>> PARRIER = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	protected static final TrackedData<Boolean> LOVEABLE = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<OptionalInt> LOVE = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	boolean wasCharging;
	
	public DroneEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		this.moveControl = new DroneMoveControl(this);
		if(!world.isClient)
			dataTracker.set(LOVEABLE, random.nextInt(100) == 0);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(FALLROT, new Vector3f());
		dataTracker.startTracking(FALLDIR, new Vector3f());
		dataTracker.startTracking(FALLING, 0);
		dataTracker.startTracking(MOVE_COOLDOWN, 40);
		dataTracker.startTracking(ATTACK_COOLDOWN, 60);
		dataTracker.startTracking(KEEP_HEIGHT, false);
		dataTracker.startTracking(PARRIER, Optional.empty());
		dataTracker.startTracking(LOVEABLE, false);
		dataTracker.startTracking(LOVE, OptionalInt.empty());
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(1, new DroneAttackGoal(this));
		goalSelector.add(0, new DroneRandomMovementGoal(this));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	public static DefaultAttributeContainer getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 2.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d).build();
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(getWorld().isClient)
			return true;
		if(isFalling() && !source.isOf(DamageSources.INTERRUPT))
		{
			explode(source);
			return false;
		}
		if(getHealth() - amount <= 0)
		{
			if(!isFalling())
			{
				dataTracker.set(FALLING, 1);
				dataTracker.set(FALLROT, new Vec3d(0f, 0f, 0f).addRandom(random, 0.5f).toVector3f());
				if(getTarget() != null)
				{
					dataTracker.set(KEEP_HEIGHT, true);
					LivingEntity t = getTarget();
					dataTracker.set(FALLDIR, t.getPos().add(0f, t.getHeight() / 2f, 0f).subtract(getPos()).normalize().toVector3f());
				}
				else
					dataTracker.set(FALLDIR, getRotationVector().toVector3f());
				setHealth(0.5f);
				playSound(SoundRegistry.DRONE_DEATH, 1f, 1f);
				return super.damage(source, 0.001f);
			}
			return false;
		}
		return super.damage(source, amount);
	}
	
	public void fire()
	{
		if(lovesTarget())
			return;
		float zRot = random.nextFloat() * 180 * MathHelper.RADIANS_PER_DEGREE;
		for (int i = 0; i < 3; i++)
		{
			HellBulletEntity bullet = HellBulletEntity.spawn(this, getWorld());
			Vec3d dir = new Vec3d(0, 0, 0.5)
								.add(new Vec3d((i - 1), 0f, 0f).rotateZ(zRot))
								.rotateX(-getPitch() * MathHelper.RADIANS_PER_DEGREE).rotateY(-getHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
			Vec3d pos = getPos().add(dir);
			bullet.setPos(pos.x, pos.y, pos.z);
			bullet.setVelocity(new Vec3d(0f, 0f, 0.7).rotateX(-getPitch() * MathHelper.RADIANS_PER_DEGREE)
									   .rotateY(-getHeadYaw() * MathHelper.RADIANS_PER_DEGREE));
			bullet.setIgnored(getClass());
			bullet.setOwner(this);
			getWorld().spawnEntity(bullet);
		}
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(isFalling())
		{
			if(getWorld().getOtherEntities(this, getBoundingBox().shrink(0.3f, 0.3f, 0.3).offset(getVelocity().multiply(-1)),
					e -> e instanceof LivingEntity && (dataTracker.get(PARRIER).isEmpty() || !dataTracker.get(PARRIER).get().equals(e.getUuid()))).size() > 0)
				damage(DamageSources.get(getWorld(), DamageTypes.PLAYER_ATTACK), 10);
			dataTracker.set(FALLING, dataTracker.get(FALLING) + 1);
			Vec3d particlePos = getPos().addRandom(random, 0.5f);
			getWorld().addParticle(ParticleTypes.LARGE_SMOKE, particlePos.x, particlePos.y, particlePos.z, 0f, 0f, 0f);
		}
		if(super.isAttacking() && age % 3 == 0)
		{
			Vec3d particlePos = getBoundingBox().getCenter().add(getRotationVector().multiply(0.5f));
			Vec3d offset = new Vec3d(0f, 0f, 0f).addRandom(random, 1f);
			getWorld().addParticle(ParticleRegistry.DRONE_CHARGE,
					particlePos.x + offset.x, particlePos.y + offset.y, particlePos.z + offset.z,
					-offset.x * 0.04, -offset.y * 0.04, -offset.z * 0.04);
		}
		else if(!super.isAttacking() && wasCharging && lovesTarget())
		{
			for (int i = 0; i < 4; i++)
			{
				Vec3d pos = getPos().addRandom(random, 1f);
				getWorld().addParticle(ParticleTypes.HEART, pos.x, pos.y, pos.z, 0f, 0f, 0f);
			}
		}
		wasCharging = super.isAttacking();
		if(getTarget() != null && !isFalling())
		{
			lookAtEntity(getTarget(), 180, 180);
			double e = getTarget().getX() - getX();
			double f = getTarget().getZ() - getZ();
			setYaw(-((float) MathHelper.atan2(e, f)) * 57.295776f);
			setBodyYaw(getYaw());
		}
		if(dataTracker.get(MOVE_COOLDOWN) > 0)
			dataTracker.set(MOVE_COOLDOWN, dataTracker.get(MOVE_COOLDOWN) - (getTarget() != null && distanceTo(getTarget()) < (lovesTarget() ? 1f : 3f) ? 20 : 1));
		if(dataTracker.get(ATTACK_COOLDOWN) > 0 && getTarget() != null)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
		if(lovesTarget() && age % 14 == 0 && random.nextInt(3) == 0)
		{
			if(!getWorld().isClient)
				dataTracker.set(LOVE, OptionalInt.of(getTarget().getId()));
			getWorld().addParticle(ParticleTypes.HEART, getX(), getY() + 1, getZ(), 0f, 0f, 0f);
		}
		if(dataTracker.get(LOVE).isPresent())
		{
			Entity e = getWorld().getEntityById(dataTracker.get(LOVE).getAsInt());
			if(e instanceof LivingEntity livingLove && !livingLove.getEquippedStack(EquipmentSlot.HEAD).isOf(ItemRegistry.DRONE_MASK))
			{
				getWorld().addParticle(ParticleRegistry.SHOCK, getX(), getY() + 1, getZ(),
						(random.nextFloat() - 0.5f) * 0.2f, 0.25f, (random.nextFloat() - 0.5f) * 0.2f);
				dataTracker.set(LOVE, OptionalInt.empty());
			}
		}
	}
	
	void explode(DamageSource source)
	{
		if(dead)
			return;
		dead = true;
		ExplosionHandler.explosion(this, getWorld(), getPos(),
				DamageSources.get(getWorld(), DamageTypes.EXPLOSION, this, source != null ? source.getAttacker() : null),
				6, 2, 2f, true);
		if(!getWorld().isClient)
			drop(source);
		discard();
	}
	
	private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState)
	{
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "drone", 2, this::predicate));
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	public boolean isFalling()
	{
		return dataTracker.get(FALLING) > 0;
	}
	
	public int getFallingTicks()
	{
		return dataTracker.get(FALLING);
	}
	
	public Vec3d getFallRot()
	{
		return new Vec3d(dataTracker.get(FALLROT));
	}
	
	@Override
	public void takeKnockback(double strength, double x, double z)
	{
		super.takeKnockback(strength / 3f, x, z);
	}
	
	@Override
	public void onInterrupt(PlayerEntity interrupter)
	{
		setRotation(interrupter.getYaw(), interrupter.getPitch());
		dataTracker.set(FALLDIR, interrupter.getRotationVector().toVector3f());
		dataTracker.set(FALLING, isFalling() ? 70 : 1);
		dataTracker.set(KEEP_HEIGHT, true);
		dataTracker.set(PARRIER, Optional.of(interrupter.getUuid()));
	}
	
	@Override
	public boolean isAttacking()
	{
		return super.isAttacking() || isFalling();
	}
	
	@Override
	public boolean isFireImmune()
	{
		return true;
	}
	
	public boolean lovesTarget()
	{
		if(getWorld().isClient)
			return dataTracker.get(LOVEABLE) && dataTracker.get(LOVE).isPresent();
		return dataTracker.get(LOVEABLE) && getTarget() != null && getTarget().getEquippedStack(EquipmentSlot.HEAD).isOf(ItemRegistry.DRONE_MASK);
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		if(dataTracker.get(LOVEABLE))
			nbt.putBoolean("oddValue", true);
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if(nbt.contains("oddValue"))
			dataTracker.set(LOVEABLE, nbt.getBoolean("oddValue"));
	}
	
	@Override
	public void setAttacking(boolean attacking)
	{
		super.setAttacking(attacking);
		if(attacking)
			playSound(SoundRegistry.DRONE_CHARGE, 1f, 1f);
	}
	
	static class DroneMoveControl extends MoveControl
	{
		DroneEntity drone;
		
		public DroneMoveControl(DroneEntity drone)
		{
			super(drone);
			this.drone = drone;
		}
		
		@Override
		public void tick()
		{
			if(drone.isFalling())
			{
				int fallTicks = drone.dataTracker.get(FALLING);
				Vec3d dir = new Vec3d(drone.dataTracker.get(FALLDIR));
				if(drone.getTarget() == null && !drone.dataTracker.get(KEEP_HEIGHT))
					dir = dir.subtract(0f, 0.1f * Math.min(fallTicks / 10f, 5f), 0f);
				Vec3d movement = dir.multiply(Math.min(fallTicks / 30f, 2f));
				HitResult hit = drone.getWorld().raycast(new RaycastContext(drone.getPos(), drone.getPos().add(movement),
						RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, drone));
				if(hit != null && hit.getType().equals(HitResult.Type.BLOCK))
					drone.damage(DamageSources.get(drone.getWorld(), DamageTypes.FLY_INTO_WALL), 10);
				drone.setVelocity(movement);
			}
			else if(state.equals(State.MOVE_TO))
			{
				Vec3d dest = new Vec3d(targetX, targetY, targetZ);
				Vec3d dir = dest.subtract(drone.getPos()).normalize();
				float distance = (float)dest.distanceTo(drone.getPos());
				drone.move(MovementType.SELF,
						dir.multiply(drone.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * Math.min(distance / 1.5f, 1f)));
				if(drone.getTarget() == null)
					drone.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, dest);
				if(distance < 0.1f)
					state = State.WAIT;
			}
			else if(drone.getTarget() != null && drone.distanceTo(drone.getTarget()) > (drone.lovesTarget() ? 3f : 6f))
			{
				Vec3d dir = drone.getTarget().getEyePos().subtract(drone.getPos()).normalize()
							  .multiply(drone.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * (drone.lovesTarget() ? 0.75f : 0.1f));
				drone.move(MovementType.SELF, dir);
			}
		}
	}
	
	static class DroneRandomMovementGoal extends Goal
	{
		DroneEntity drone;
		
		public DroneRandomMovementGoal(DroneEntity drone)
		{
			this.drone = drone;
		}
		
		@Override
		public boolean canStart()
		{
			return true;
		}
		
		@Override
		public void tick()
		{
			LivingEntity target = drone.getTarget();
			super.tick();
			if(drone.getDistanceToGround() < 1f)
			{
				Vec3d pos = drone.getPos();
				drone.moveControl.moveTo(pos.x, pos.y + 1, pos.z, drone.speed);
			}
			if(drone.dataTracker.get(MOVE_COOLDOWN) <= 0)
			{
				for (int i = 0; i < 5; i++)
				{
					Vec3d dest;
					if(target != null)
					{
						if(drone.distanceTo(target) < (drone.lovesTarget() ? 1f : 3f))
							dest = drone.getPos().add(drone.getPos().subtract(target.getEyePos()).normalize().multiply(3f));
						else
							dest = drone.getPos().add(Vec3d.fromPolar(0f, drone.getYaw() + 90f).multiply((drone.random.nextFloat() - 0.5) * 6));
					}
					else
						dest = drone.getPos().addRandom(drone.random, 15f);
					HitResult hit = drone.getWorld().raycast(new RaycastContext(dest, drone.getPos(), RaycastContext.ShapeType.COLLIDER,
							RaycastContext.FluidHandling.ANY, drone));
					if(hit == null || hit.getType().equals(HitResult.Type.MISS))
					{
						drone.moveControl.moveTo(dest.x, dest.y, dest.z, drone.speed);
						break;
					}
				}
				drone.dataTracker.set(MOVE_COOLDOWN, (int)(drone.random.nextBetween(50, 100) * (target == null ? 1f : 0.5f)));
			}
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
	}
	
	static class DroneAttackGoal extends Goal
	{
		DroneEntity drone;
		int ticks;
		
		public DroneAttackGoal(DroneEntity drone)
		{
			this.drone = drone;
		}
		
		@Override
		public boolean canStart()
		{
			return drone.dataTracker.get(ATTACK_COOLDOWN) <= 0 && drone.getTarget() != null && !drone.isFalling();
		}
		
		@Override
		public void start()
		{
			super.start();
			drone.setAttacking(true);
			ticks = 0;
		}
		
		@Override
		public void tick()
		{
			super.tick();
			if(drone.isFalling())
			{
				stop();
				return;
			}
			ticks++;
			if(ticks >= 30)
			{
				drone.fire();
				stop();
			}
		}
		
		@Override
		public void stop()
		{
			super.stop();
			drone.dataTracker.set(ATTACK_COOLDOWN, 80 + drone.random.nextInt(60));
			drone.setAttacking(false);
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean canStop()
		{
			return ticks >= 30;
		}
	}
}
