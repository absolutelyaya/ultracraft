package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.accessor.MeleeParriable;
import net.minecraft.entity.EntityType;
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
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SwordmachineEntity extends HostileEntity implements GeoEntity, MeleeParriable, Enrageable
{
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	//private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BYTE);
	protected static final TrackedData<Boolean> ENRAGED = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final byte ANIMATION_IDLE = 0;
	
	public SwordmachineEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ATTACK_COOLDOWN, 0);
		dataTracker.startTracking(ANIMATION, (byte)0);
		dataTracker.startTracking(ENRAGED, false);
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0d)
					   .add(EntityAttributes.GENERIC_ARMOR, 6.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d);
	}
	
	@Override
	public void onParried(PlayerEntity parrier)
	{
	
	}
	
	private <E extends GeoEntity> PlayState predicate(AnimationState<E> event)
	{
		byte anim = dataTracker.get(ANIMATION);
		AnimationController<?> controller = event.getController();
		
		switch (anim)
		{
			case ANIMATION_IDLE -> controller.setAnimation(/*event.isMoving() ? WALK_ANIM : */IDLE_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		AnimationController<SwordmachineEntity> controller = new AnimationController<>(this, "controller",
				2, this::predicate);
		controllerRegistrar.add(controller);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public boolean isEnraged()
	{
		return false;
	}
	
	@Override
	public Vec3d getEnrageFeatureSize()
	{
		return new Vec3d(1f, 1f, 1f);
	}
	
	@Override
	public Vec3d getEnragedFeatureOffset()
	{
		return new Vec3d(0f, 2.5f, 0f);
	}
}
