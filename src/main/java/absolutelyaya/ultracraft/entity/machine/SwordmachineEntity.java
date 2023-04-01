package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.MeleeParriable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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
	private final ServerBossBar bossBar = new ServerBossBar(this.getDisplayName(), BossBar.Color.RED, BossBar.Style.PROGRESS);
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation RAND_IDLE_ANIM = RawAnimation.begin().thenLoop("look_around");
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("move");
	private static final RawAnimation BLAST_ANIM = RawAnimation.begin().thenLoop("shotgun");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BYTE);
	protected static final TrackedData<Boolean> ENRAGED = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> BLASTING = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_LOOK = 1;
	
	public SwordmachineEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		((LivingEntityAccessor)this).SetTakePunchKnockbackSupplier(() -> false); //disable knockback
	}
	
	//TODO: attack husks when named something specific
	//TODO: add attacks lol
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ATTACK_COOLDOWN, 0);
		dataTracker.startTracking(ANIMATION, (byte)0);
		dataTracker.startTracking(ENRAGED, false);
		dataTracker.startTracking(BLASTING, false);
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0d)
					   .add(EntityAttributes.GENERIC_ARMOR, 6.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d)
					   .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0d);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new ChaseGoal(this));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
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
			case ANIMATION_IDLE -> controller.setAnimation(event.isMoving() ? WALK_ANIM : IDLE_ANIM);
			case ANIMATION_LOOK -> controller.setAnimation(RAND_IDLE_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	private <E extends GeoEntity> PlayState shotgunPredicate(AnimationState<E> event)
	{
		AnimationController<?> controller = event.getController();
		if(dataTracker.get(BLASTING))
			controller.setAnimation(BLAST_ANIM);
		else
			return PlayState.STOP;
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(
			new AnimationController<>(this, "main", 2, this::predicate),
			new AnimationController<>(this, "shotgun", 2, this::shotgunPredicate)
		);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if (hasCustomName())
			bossBar.setName(Text.translatable("entity.ultracraft.swordmachine-named", getDisplayName()));
	}
	
	@Override
	public void setCustomName(@Nullable Text name)
	{
		super.setCustomName(name);
		bossBar.setName(Text.translatable("entity.ultracraft.swordmachine-named", getDisplayName()));
	}
	
	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player)
	{
		super.onStartedTrackingBy(player);
		bossBar.addPlayer(player);
	}
	
	@Override
	public void onStoppedTrackingBy(ServerPlayerEntity player)
	{
		super.onStoppedTrackingBy(player);
		bossBar.removePlayer(player);
	}
	
	@Override
	protected void mobTick()
	{
		super.mobTick();
		bossBar.setPercent(getHealth() / getMaxHealth());
		int i = age % 40;
		if(i == 0)
			dataTracker.set(BLASTING, !dataTracker.get(BLASTING));
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
	
	private boolean isIdle()
	{
		return dataTracker.get(ANIMATION) == ANIMATION_IDLE ||dataTracker.get(ANIMATION) == ANIMATION_LOOK;
	}
	
	static class ChaseGoal extends Goal
	{
		SwordmachineEntity sm;
		LivingEntity target;
		
		public ChaseGoal(SwordmachineEntity sm)
		{
			this.sm = sm;
		}
		
		@Override
		public boolean canStart()
		{
			target = sm.getTarget();
			return target != null && sm.isIdle();
		}
		
		@Override
		public void tick()
		{
			sm.navigation.startMovingTo(target, 1f);
		}
		
		@Override
		public boolean shouldContinue()
		{
			return target != null && target.isAlive() && sm.squaredDistanceTo(target) > 1;
		}
		
		@Override
		public void stop()
		{
			sm.navigation.stop();
		}
	}
}
