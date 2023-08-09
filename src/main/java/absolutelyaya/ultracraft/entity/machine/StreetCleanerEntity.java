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
	static final RawAnimation PoseAnimation = RawAnimation.begin().thenLoop("pose");
	AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	
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
		event.setAnimation(PoseAnimation);
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
