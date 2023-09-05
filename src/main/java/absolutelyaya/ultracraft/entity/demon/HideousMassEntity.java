package absolutelyaya.ultracraft.entity.demon;

import absolutelyaya.ultracraft.accessor.IAnimatedEnemy;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.entity.goal.TimedAttackGoal;
import absolutelyaya.ultracraft.entity.other.ShockwaveEntity;
import absolutelyaya.ultracraft.entity.other.VerticalShockwaveEntity;
import absolutelyaya.ultracraft.entity.projectile.HideousMortarEntity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
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
	protected static final TrackedData<Integer> MORTAR_COUNTER = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> SLAM_COUNTER = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Boolean> ENRAGED = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> LAYING = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	static final RawAnimation POSE_ANIM = RawAnimation.begin().thenPlay("pose");
	static final RawAnimation LAY_POSE_ANIM = RawAnimation.begin().thenPlay("lay_pose");
	static final RawAnimation MORTAR_ANIM = RawAnimation.begin().thenPlay("mortar");
	static final RawAnimation SLAM_STANDING_ANIM = RawAnimation.begin().thenPlay("stand_slam_start").thenPlay("slam");
	static final RawAnimation SLAM_LAYING_ANIM = RawAnimation.begin().thenPlay("lay_slam_start").thenPlay("slam");
	static final RawAnimation STAND_UP_ANIM = RawAnimation.begin().thenPlay("stand_up");
	static final RawAnimation CLAP_ANIM = RawAnimation.begin().thenPlay("clap");
	static final RawAnimation HARPOON_ANIM = RawAnimation.begin().thenPlay("harpoon");
	final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_MORTAR = 1;
	private static final byte ANIMATION_SLAM_STANDING = 2;
	private static final byte ANIMATION_SLAM_LAYING = 3;
	private static final byte ANIMATION_STAND_UP = 4;
	private static final byte ANIMATION_CLAP = 5;
	private static final byte ANIMATION_HARPOON = 6;
	
	public HideousMassEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		((LivingEntityAccessor)this).setTakePunchKnockbackSupplier(() -> false); //disable knockback
		lookControl = new HideousLookControl(this);
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
		dataTracker.startTracking(LAYING, false);
		dataTracker.startTracking(MORTAR_COUNTER, 0);
		dataTracker.startTracking(SLAM_COUNTER, 0);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new MortarAttackGoal(this));
		goalSelector.add(1, new StandupGoal(this));
		goalSelector.add(2, new ClapAttackGoal(this));
		goalSelector.add(4, new SlamAttackGoal(this, true));
		goalSelector.add(4, new SlamAttackGoal(this, false));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "controller", this::predicate));
	}
	
	private PlayState predicate(AnimationState<GeoAnimatable> event)
	{
		switch(getAnimation())
		{
			case ANIMATION_IDLE -> event.setAnimation(dataTracker.get(LAYING) ? LAY_POSE_ANIM : POSE_ANIM);
			case ANIMATION_MORTAR -> event.setAnimation(MORTAR_ANIM);
			case ANIMATION_SLAM_STANDING -> event.setAnimation(SLAM_STANDING_ANIM);
			case ANIMATION_SLAM_LAYING -> event.setAnimation(SLAM_LAYING_ANIM);
			case ANIMATION_STAND_UP -> event.setAnimation(STAND_UP_ANIM);
			case ANIMATION_CLAP -> event.setAnimation(CLAP_ANIM);
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
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		setBodyYaw(headYaw);
	}
	
	@Override
	public boolean isPushable()
	{
		return false;
	}
	
	@Override
	protected void pushAway(Entity entity) {
	
	}
	
	@Override
	public void takeKnockback(double strength, double x, double z) {
	}
	
	public void fireMortar(Vec3d offset)
	{
		HideousMortarEntity.spawn(getWorld(), new Vec3d(getX(), getBoundingBox().getMax(Direction.Axis.Y), getZ()).add(offset.rotateY(getYaw())),
				this, getTarget());
	}
	
	private void shockwave()
	{
		ShockwaveEntity shockwave = new ShockwaveEntity(EntityRegistry.SHOCKWAVE, getWorld());
		shockwave.setDamage(3f);
		shockwave.setGrowRate(0.5f);
		shockwave.setAffectOnly(PlayerEntity.class);
		shockwave.setPosition(getPos().add(0f, 0.5f, 0f));
		getWorld().spawnEntity(shockwave);
	}
	
	private void clap()
	{
		VerticalShockwaveEntity shockwave = new VerticalShockwaveEntity(EntityRegistry.VERICAL_SHOCKWAVE, getWorld());
		shockwave.setDamage(2f);
		shockwave.setYaw(getYaw());
		shockwave.setGrowRate(0.5f);
		shockwave.setAffectOnly(PlayerEntity.class);
		shockwave.setPosition(getPos().add(0f, 0.5f, 0f));
		getWorld().spawnEntity(shockwave);
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
	
	
	static class HideousLookControl extends LookControl
	{
		public HideousLookControl(MobEntity entity)
		{
			super(entity);
		}
		
		@Override
		public void tick()
		{
			if(entity.getTarget() != null && entity.getDataTracker().get(ANIMATION).equals(ANIMATION_IDLE))
			{
				Vec3d diff = entity.getTarget().getPos().subtract(entity.getPos());
				float targetYaw = -((float)MathHelper.atan2(diff.x, diff.z)) * MathHelper.DEGREES_PER_RADIAN;
				entity.headYaw = changeAngle(entity.headYaw, targetYaw, 3.5f);
				entity.setYaw(entity.headYaw);
			}
		}
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
			return super.canStart() && !mob.dataTracker.get(LAYING) && mob.random.nextFloat() > 0.5;
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
		
		@Override
		public void stop()
		{
			super.stop();
			mob.dataTracker.set(MORTAR_COUNTER, mob.dataTracker.get(MORTAR_COUNTER) + 1);
			mob.dataTracker.set(SLAM_COUNTER, 0);
		}
	}
	
	static class SlamAttackGoal extends TimedAttackGoal<HideousMassEntity>
	{
		boolean standing;
		
		public SlamAttackGoal(HideousMassEntity mass, boolean standing)
		{
			super(mass, ANIMATION_IDLE, standing ? ANIMATION_SLAM_STANDING : ANIMATION_SLAM_LAYING, 35);
			this.standing = standing;
		}
		
		@Override
		public boolean canStart()
		{
			return super.canStart() && ((standing && !mob.dataTracker.get(LAYING) && mob.dataTracker.get(MORTAR_COUNTER) > 0 && mob.random.nextFloat() > 0.5f) ||
												(!standing && mob.dataTracker.get(LAYING)));
		}
		
		@Override
		protected void process()
		{
			super.process();
			if(timer == 22)
				mob.shockwave();
		}
		
		@Override
		public void stop()
		{
			super.stop();
			mob.dataTracker.set(LAYING, true);
			mob.dataTracker.set(SLAM_COUNTER, mob.dataTracker.get(SLAM_COUNTER) + 1);
			mob.dataTracker.set(MORTAR_COUNTER, 0);
		}
	}
	
	static class ClapAttackGoal extends TimedAttackGoal<HideousMassEntity>
	{
		public ClapAttackGoal(HideousMassEntity mass)
		{
			super(mass, ANIMATION_IDLE, ANIMATION_CLAP, 40);
		}
		
		@Override
		public boolean canStart()
		{
			return super.canStart() && mob.dataTracker.get(LAYING) && mob.dataTracker.get(SLAM_COUNTER) > 0 && mob.random.nextFloat() < 0.9f;
		}
		
		@Override
		protected void process()
		{
			super.process();
			if(timer == 30)
				mob.clap();
		}
	}
	
	static class StandupGoal extends TimedAttackGoal<HideousMassEntity>
	{
		
		public StandupGoal(HideousMassEntity mass)
		{
			super(mass, ANIMATION_IDLE, ANIMATION_STAND_UP, 15);
		}
		
		@Override
		public boolean canStart()
		{
			return super.canStart() && mob.dataTracker.get(LAYING) && mob.dataTracker.get(SLAM_COUNTER) > 2;
		}
		
		@Override
		public void stop()
		{
			super.stop();
			mob.dataTracker.set(LAYING, false);
		}
	}
}
