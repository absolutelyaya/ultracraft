package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.entity.IAntiCheeseBoss;
import absolutelyaya.ultracraft.entity.goal.AntiCheeseProximityTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class V2Entity extends AbstractUltraHostileEntity implements IAntiCheeseBoss, GeoEntity, Enrageable
{
	static protected final TrackedData<Integer> FRUSTRATION = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	static protected final TrackedData<Integer> DISTANCE = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	static protected final TrackedData<Integer> IDLE_TIMER = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	
	public V2Entity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		((LivingEntityAccessor)this).setTakePunchKnockbackSupplier(() -> false); //disable knockback
	}
	
	public static DefaultAttributeContainer getDefaultAttributes()
	{
        return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d).build();
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(FRUSTRATION, 0);
		dataTracker.startTracking(DISTANCE, 0);
		dataTracker.startTracking(IDLE_TIMER, 0);
	}
	
	@Override
	protected void initGoals()
	{
		super.initGoals();
		goalSelector.add(0, new MeleeAttackGoal(this, 1f, true));
		
		targetSelector.add(0, new AntiCheeseProximityTargetGoal<>(this, PlayerEntity.class, 5, 32));
	}
	
	@Override
	public void tick()
	{
		super.tick();
		dataTracker.set(FRUSTRATION, dataTracker.get(FRUSTRATION) + 1);
		if(getTarget() == null && getHealth() < getMaxHealth())
		{
			dataTracker.set(IDLE_TIMER, dataTracker.get(IDLE_TIMER) + 1);
			if(dataTracker.get(IDLE_TIMER) > 1200)
			{
				if(age % 20 == 0)
					heal(1f);
			}
		}
		else if(dataTracker.get(IDLE_TIMER) > 0)
			dataTracker.set(IDLE_TIMER, 0);
	}
	
	@Override
	public int getFrustration()
	{
		return dataTracker.get(FRUSTRATION);
	}
	
	@Override
	public void resetFrustration()
	{
		dataTracker.set(FRUSTRATION, 0);
	}
	
	private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState)
	{
		return PlayState.STOP;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "main", 2, this::predicate));
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
	public boolean cannotDespawn()
	{
		return true;
	}
	
	@Override
	public boolean isEnraged()
	{
		return false;
	}
	
	@Override
	public boolean isFireImmune()
	{
		return true;
	}
	
	@Override
	public Vec3d getEnrageFeatureSize()
	{
		return new Vec3d(1f, -1f, -1f);
	}
	
	@Override
	public Vec3d getEnragedFeatureOffset()
	{
		return new Vec3d(0f, -2f, 0f);
	}
}
