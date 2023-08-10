package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class StreetCleanerEntity extends AbstractUltraHostileEntity implements GeoEntity
{
	static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
	static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
	static final RawAnimation DODGE_ANIM = RawAnimation.begin().thenLoop("dodge");
	static final RawAnimation COUNTER_ANIM = RawAnimation.begin().thenLoop("counter");
	AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	static final byte ANIMATION_IDLE = 0;
	static final byte ANIMATION_DODGE = 1;
	static final byte ANIMATION_COUNTER = 2;
	
	public StreetCleanerEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 9.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d);
	}
	
	private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event)
	{
		byte anim = dataTracker.get(ANIMATION);
		
		switch(anim)
		{
			case ANIMATION_IDLE -> {
				if(event.isMoving())
					event.setAnimation((getTarget() != null && distanceTo(getTarget()) < 6f ? WALK_ANIM : RUN_ANIM));
				else
					event.setAnimation(IDLE_ANIM);
			}
			case ANIMATION_DODGE -> event.setAnimation(DODGE_ANIM);
			case ANIMATION_COUNTER -> event.setAnimation(COUNTER_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "streetcleaner", 2, this::predicate));
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
