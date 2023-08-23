package absolutelyaya.ultracraft.entity.demon;

import absolutelyaya.ultracraft.accessor.IAnimatedEnemy;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.entity.goal.TimedAttackGoal;
import absolutelyaya.ultracraft.entity.projectile.HideousMortarEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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

public class HideousMassEntity extends AbstractUltraHostileEntity implements GeoEntity, IAnimatedEnemy
{
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Boolean> ENRAGED = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	static final RawAnimation POSE_ANIM = RawAnimation.begin().thenPlay("pose");
	static final RawAnimation MORTAR_ANIM = RawAnimation.begin().thenPlay("mortar");
	final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_MORTAR = 1;
	
	public HideousMassEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		((LivingEntityAccessor)this).setTakePunchKnockbackSupplier(() -> false); //disable knockback
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 9.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ATTACK_COOLDOWN, 10);
		dataTracker.startTracking(ENRAGED, false);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new MortarAttackGoal(this));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, this::predicate));
	}
	
	private PlayState predicate(AnimationState<GeoAnimatable> event)
	{
		switch(getAnimation())
		{
			case ANIMATION_IDLE -> event.setAnimation(POSE_ANIM);
			case ANIMATION_MORTAR -> event.setAnimation(MORTAR_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(dataTracker.get(ATTACK_COOLDOWN) > 0)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
	}
	
	public void fireMortar(Vec3d offset)
	{
		HideousMortarEntity.spawn(getWorld(), new Vec3d(getX(), getBoundingBox().getMax(Direction.Axis.Y), getZ()).add(offset.rotateY(getYaw())),
				this, getTarget());
	}
	
	@Override
	public void setAnimation(byte id)
	{
		dataTracker.set(ANIMATION, id);
	}
	
	@Override
	public int getAnimSpeedMult()
	{
		return 1;
	}
	
	@Override
	public void setCooldown(int cooldown)
	{
		dataTracker.set(ATTACK_COOLDOWN, cooldown);
	}
	
	@Override
	public int getCooldown()
	{
		return dataTracker.get(ATTACK_COOLDOWN);
	}
	
	@Override
	public boolean isHeadFixed()
	{
		return true;
	}
	
	static class MortarAttackGoal extends TimedAttackGoal<HideousMassEntity>
	{
		public MortarAttackGoal(HideousMassEntity mass)
		{
			super(mass, ANIMATION_IDLE, ANIMATION_MORTAR, 56);
		}
		
		@Override
		public boolean canStart()
		{
			return super.canStart();
		}
		
		@Override
		protected void process()
		{
			super.process();
			if(timer == 14)
				mob.fireMortar(new Vec3d(2f, 0f, 0f));
			else if(timer == 21)
				mob.fireMortar(new Vec3d(-2f, 0f, 0f));
		}
	}
}
