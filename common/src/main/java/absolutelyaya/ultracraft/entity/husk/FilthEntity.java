package absolutelyaya.ultracraft.entity.husk;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class FilthEntity extends HostileEntity implements IAnimatable
{
	private final AnimationFactory factory = new AnimationFactory(this);
	private static final AnimationBuilder IDLE_ANIM = new AnimationBuilder().addAnimation("idle", true);
	protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(FilthEntity.class, TrackedDataHandlerRegistry.BYTE);
	protected static final TrackedData<Integer> ANIM_TICKS = DataTracker.registerData(FilthEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final byte ANIMATION_IDLE = 0;
	
	public FilthEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0f);
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0d)
					   .add(EntityAttributes.GENERIC_ARMOR, 2.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.5d);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		this.dataTracker.startTracking(ANIMATION, ANIMATION_IDLE);
		this.dataTracker.startTracking(ANIM_TICKS, 0);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(ANIMATION))
			dataTracker.set(ANIM_TICKS, 0);
	}
	
	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
	{
		byte anim = dataTracker.get(ANIMATION);
		AnimationController<?> controller = event.getController();
		
		switch (anim)
		{
			case ANIMATION_IDLE ->
			{
				controller.setAnimationSpeed(getVelocity().horizontalLengthSquared() > 0.03 ? 2f : 1f);
				controller.setAnimation(event.isMoving() ? IDLE_ANIM : IDLE_ANIM);
			}
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimationData animationData)
	{
		AnimationController<FilthEntity> controller = new AnimationController<>(this, "controller",
				2, this::predicate);
		animationData.addAnimationController(controller);
	}
	
	@Override
	public AnimationFactory getFactory()
	{
		return factory;
	}
	
	public byte getAnimation()
	{
		return dataTracker.get(ANIMATION);
	}
	
	public int getAnimationTicks()
	{
		return dataTracker.get(ANIM_TICKS);
	}
}
