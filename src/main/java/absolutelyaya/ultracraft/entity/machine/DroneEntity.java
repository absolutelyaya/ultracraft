package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.accessor.MeleeInterruptable;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraFlyingEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

public class DroneEntity extends AbstractUltraFlyingEntity implements GeoEntity, MeleeInterruptable
{
	private final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	public static final TrackedData<Integer> FALLING = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Vector3f> FALLROT = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	public static final TrackedData<Integer> MOVE_COOLDOWN = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Integer> INVULERABLE = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.INTEGER);
	
	public DroneEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		this.moveControl = new DroneMoveControl(this);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(FALLING, 0);
		dataTracker.startTracking(FALLROT, new Vector3f());
		dataTracker.startTracking(MOVE_COOLDOWN, 40);
		dataTracker.startTracking(INVULERABLE, 0);
		dataTracker.startTracking(ATTACK_COOLDOWN, 60);
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
		if(dataTracker.get(INVULERABLE) > 0)
			return false;
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
				dataTracker.set(INVULERABLE, 2);
			}
			return false;
		}
		return super.damage(source, amount);
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(isFalling())
		{
			if(getWorld().getOtherEntities(this, getBoundingBox(), e -> e instanceof LivingEntity && collidesWith(e)).size() > 0)
				damage(DamageSources.get(getWorld(), DamageTypes.FLY_INTO_WALL), 10);
			dataTracker.set(FALLING, dataTracker.get(FALLING) + 1);
			Vec3d particlePos = getPos().addRandom(random, 0.5f);
			Vec3d vel = getVelocity();
			getWorld().addParticle(ParticleTypes.LARGE_SMOKE, particlePos.x, particlePos.y, particlePos.z, -vel.x, -vel.y, -vel.z);
		}
		if(getTarget() != null && !isFalling())
		{
			lookAtEntity(getTarget(), 180, 180);
			double e = getTarget().getX() - getX();
			double f = getTarget().getZ() - getZ();
			setYaw(-((float) MathHelper.atan2(e, f)) * 57.295776f);
			setBodyYaw(getYaw());
		}
		if(dataTracker.get(MOVE_COOLDOWN) > 0)
			dataTracker.set(MOVE_COOLDOWN, dataTracker.get(MOVE_COOLDOWN) - (getTarget() != null && distanceTo(getTarget()) < 3f ? 20 : 1));
		if(dataTracker.get(INVULERABLE) > 0)
			dataTracker.set(INVULERABLE, dataTracker.get(INVULERABLE) - 1);
		if(dataTracker.get(ATTACK_COOLDOWN) > 0 && getTarget() != null)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
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
		dataTracker.set(FALLING, 1);
	}
	
	@Override
	public boolean isAttacking()
	{
		return super.isAttacking() || isFalling();
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
				Vec3d dir = drone.getRotationVector();
				//if(drone.getTarget() != null)
				//	dir = dir.lerp(drone.getTarget().getPos().subtract(drone.getPos()).normalize(), Math.max(1.4f - fallTicks / 20f, 0f));
				//else
					dir = dir.subtract(0f, 0.1f * Math.min(fallTicks / 10f, 5f), 0f); //TODO: disable if parried
				Vec3d movement = dir.multiply(Math.min(fallTicks / 30f, 2f));
				HitResult hit = drone.getWorld().raycast(new RaycastContext(drone.getPos(), drone.getPos().add(movement),
						RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, drone));
				if(hit != null && hit.getType().equals(HitResult.Type.BLOCK))
					drone.damage(DamageSources.get(drone.getWorld(), DamageTypes.FLY_INTO_WALL), 10);
				drone.move(MovementType.SELF, movement);
				return;
			}
			if(state.equals(State.MOVE_TO))
			{
				Vec3d dest = new Vec3d(targetX, targetY, targetZ);
				Vec3d dir = dest.subtract(drone.getPos()).normalize();
				float distance = (float)dest.distanceTo(drone.getPos());
				drone.move(MovementType.SELF,
						dir.multiply(drone.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * Math.min(distance / 1.5f, 1f)));
				if(drone.getTarget() == null)
					drone.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, dest);
				if(distance < 0.01f)
					state = State.WAIT;
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
			super.tick();
			if(drone.dataTracker.get(MOVE_COOLDOWN) <= 0)
			{
				for (int i = 0; i < 5; i++)
				{
					Vec3d dest;
					if(drone.getTarget() != null)
						dest = drone.getTarget().getPos().add(drone.getTarget().getRotationVector().multiply(6.5f))
									   .multiply(1f, 0, 1f).add(0, drone.getTarget().getY() + 4.5f, 0).addRandom(drone.random, 4f);
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
				drone.dataTracker.set(MOVE_COOLDOWN, drone.random.nextBetween(30, 60));
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
			return drone.dataTracker.get(ATTACK_COOLDOWN) <= 0 && drone.getTarget() != null;
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
			ticks++;
			if(ticks >= 30)
			{
				//shoot
				stop();
			}
		}
		
		@Override
		public void stop()
		{
			super.stop();
			drone.dataTracker.set(ATTACK_COOLDOWN, 40 + drone.random.nextInt(30));
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
