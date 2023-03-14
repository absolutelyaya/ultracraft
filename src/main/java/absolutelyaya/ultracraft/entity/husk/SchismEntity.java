package absolutelyaya.ultracraft.entity.husk;

import absolutelyaya.ultracraft.accessor.Interruptable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SchismEntity extends AbstractHuskEntity implements GeoEntity, Interruptable
{
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
	private static final RawAnimation ATTACK_VERTICAL_ANIM = RawAnimation.begin().thenLoop("attackVert");
	private static final RawAnimation ATTACK_HORIZONTAL_ANIM = RawAnimation.begin().thenLoop("attackHor");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_ATTACK_VERTICAL = 1;
	private static final byte ANIMATION_ATTACK_HORIZONTAL = 2;
	
	public SchismEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	//TODO: random wander
	//TODO: Vertical Attack
	//TODO: Horizontal Attack
	//TODO: Interuptable Attack Charge Time
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0d)
					   .add(EntityAttributes.GENERIC_ARMOR, 2.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0d);
	}
	
	private <E extends GeoEntity> PlayState predicate(AnimationState<E> event)
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
					controller.setAnimation(WALK_ANIM);
				else
					return PlayState.STOP;
			}
			case ANIMATION_ATTACK_VERTICAL -> controller.setAnimation(ATTACK_VERTICAL_ANIM);
			case ANIMATION_ATTACK_HORIZONTAL -> controller.setAnimation(ATTACK_HORIZONTAL_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		AnimationController<SchismEntity> controller = new AnimationController<>(this, "controller",
				2, this::predicate);
		controllerRegistrar.add(controller);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public void onInterrupted(PlayerEntity interruptor)
	{
	
	}
}
