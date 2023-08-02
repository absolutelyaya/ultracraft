package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraFlyingEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

public class DroneEntity extends AbstractUltraFlyingEntity implements GeoEntity
{
	private final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	public static final TrackedData<Boolean> FALLING = DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public DroneEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		this.moveControl = new DroneMoveControl(this);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(FALLING, false);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new DroneRandomMovementGoal(this));
	}
	
	public static DefaultAttributeContainer getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 5.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d).build();
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(source.isOf(DamageTypes.FALL))
		{
			explode(source);
			return false;
		}
		if(getHealth() - amount <= 0)
		{
			explode(source);
			return true;
		}
		return super.damage(source, amount);
	}
	
	void explode(DamageSource source)
	{
		dead = true;
		boolean player = (source.getSource() != null && source.getSource().isPlayer() || (source.getAttacker() != null && source.getAttacker().isPlayer()));
		ExplosionHandler.explosion(this, getWorld(), getPos(),
				DamageSources.get(getWorld(), DamageTypes.EXPLOSION, this, source.getAttacker()), 6, 2, 2f, true);
		dropLoot(source, player);
		dropXp();
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
			if(state.equals(State.MOVE_TO))
			{
				Vec3d dest = new Vec3d(targetX, targetY, targetZ);
				Vec3d dir = dest.subtract(drone.getPos()).normalize();
				drone.setPosition(drone.getPos().add(
						dir.multiply(drone.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * Math.min(dest.distanceTo(drone.getPos()) / 1.5f, 1f))));
				drone.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, dest);
			}
		}
	}
	
	static class DroneRandomMovementGoal extends Goal
	{
		DroneEntity drone;
		int ticks;
		
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
			if(ticks++ % 200 == 0)
			{
				Vec3d dest = drone.getPos().addRandom(drone.random, 15f);
				HitResult hit = drone.getWorld().raycast(new RaycastContext(dest, drone.getPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, drone));
				if(hit == null || hit.getType().equals(HitResult.Type.MISS))
					drone.moveControl.moveTo(dest.x, dest.y, dest.z, drone.speed);
			}
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
	}
}
