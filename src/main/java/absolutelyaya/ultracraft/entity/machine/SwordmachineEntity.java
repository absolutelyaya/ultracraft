package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.MeleeParriable;
import absolutelyaya.ultracraft.entity.husk.AbstractHuskEntity;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
	private final ServerBossBar bossBar = new ServerBossBar(this.getDisplayName(), BossBar.Color.RED, BossBar.Style.PROGRESS);
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation RAND_IDLE_ANIM = RawAnimation.begin().thenLoop("look_around");
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("move");
	private static final RawAnimation BLAST_ANIM = RawAnimation.begin().thenLoop("shotgun");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BYTE);
	protected static final TrackedData<Boolean> HAS_SHOTGUN = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> BLASTING = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> ENRAGED_TICKS = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> STAMINA = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> MAX_STAMINA = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> BLAST_COOLDOWN = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_LOOK = 1;
	
	public SwordmachineEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		((LivingEntityAccessor)this).SetTakePunchKnockbackSupplier(() -> false); //disable knockback
	}
	
	//TODO: attack husks when named something specific
	//TODO: add attacks lol
	//TODO: Slash attack
	//TODO: Piruette attack
	//TODO: Combo attack
	//TODO: Throw attack
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ATTACK_COOLDOWN, 20);
		dataTracker.startTracking(BLAST_COOLDOWN, 0);
		dataTracker.startTracking(ANIMATION, (byte)0);
		dataTracker.startTracking(ENRAGED_TICKS, 0);
		dataTracker.startTracking(HAS_SHOTGUN, true);
		dataTracker.startTracking(BLASTING, false);
		dataTracker.startTracking(STAMINA, 30);
		dataTracker.startTracking(MAX_STAMINA, 100);
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
		goalSelector.add(0, new ShotgunGoal(this));
		goalSelector.add(3, new ChaseGoal(this));
		goalSelector.add(4, new LookAroundGoal(this));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
		targetSelector.add(0, new TargetHuskGoal(this));
	}
	
	@Override
	public void onParried(PlayerEntity parrier)
	{
		enrage();
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
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("shotgun", dataTracker.get(HAS_SHOTGUN));
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if (hasCustomName())
			bossBar.setName(Text.translatable("entity.ultracraft.swordmachine-named", getDisplayName()));
		if(nbt.contains("shotgun"))
			dataTracker.set(HAS_SHOTGUN, nbt.getBoolean("shotgun"));
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
		if(isEnraged())
			dataTracker.set(ENRAGED_TICKS, dataTracker.get(ENRAGED_TICKS) - 1);
		if(dataTracker.get(ATTACK_COOLDOWN) > 0)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
		if(dataTracker.get(BLAST_COOLDOWN) > 0)
			dataTracker.set(BLAST_COOLDOWN, dataTracker.get(BLAST_COOLDOWN) - 1);
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		boolean b = super.damage(source, amount);
		if(dataTracker.get(HAS_SHOTGUN) && getHealth() < getMaxHealth() / 2)
		{
			dataTracker.set(HAS_SHOTGUN, false);
			//TODO: don't drop if killer carries a shotgun
			if(world.getServer() == null /*|| source.getAttacker() instanceof PlayerEntity p && p.getInventory().contains()*/)
				return b;
			LootTable lootTable = world.getServer().getLootManager().getTable(new Identifier(Ultracraft.MOD_ID, "entities/swordmachine_breakdown"));
			LootContext.Builder builder = getLootContextBuilder(source.getAttacker() instanceof PlayerEntity, source);
			lootTable.generateLoot(builder.build(LootContextTypes.ENTITY), this::dropStack);
		}
		return b;
	}
	
	@Override
	protected Identifier getLootTableId()
	{
		return new Identifier(Ultracraft.MOD_ID, "entities/swordmachine_death");
	}
	
	private void enrage()
	{
		dataTracker.set(ENRAGED_TICKS, 200);
	}
	
	//TODO: add shotgun Projectiles
	private void fireShotgun()
	{
		Vec3d dir = new Vec3d(0f, 0f, 1f);
		dir = dir.rotateY((float)Math.toRadians(-getBodyYaw()));
		for (int i = 0; i < 16; i++)
		{
			HellBulletEntity bullet = HellBulletEntity.spawn(this, world);
			bullet.setVelocity(dir.x, dir.y, dir.z, 1f, 20f);
			Vec3d vel = bullet.getVelocity();
			world.addParticle(ParticleTypes.SMOKE, bullet.getX(), bullet.getY(), bullet.getZ(), vel.x, vel.y, vel.z);
			bullet.setNoGravity(true);
			bullet.setIgnored(getClass());
			world.spawnEntity(bullet);
		}
		playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 0.2f / (getRandom().nextFloat() * 0.2f + 0.6f));
	}
	
	@Override
	public boolean isEnraged()
	{
		return dataTracker.get(ENRAGED_TICKS) > 0;
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
	
	public boolean hasShotgun()
	{
		return dataTracker.get(HAS_SHOTGUN);
	}
	
	public int getAnimation()
	{
		return dataTracker.get(ANIMATION);
	}
	
	private boolean shouldHuntHusks()
	{
		if(!hasCustomName() || getCustomName() == null)
			return false;
		return getCustomName().getString().equalsIgnoreCase("dan");
	}
	
	private boolean tryConsumeStamina(int consume)
	{
		int val;
		if((val = dataTracker.get(STAMINA)) > consume)
		{
			dataTracker.set(STAMINA, val - consume);
			return true;
		}
		return false;
	}
	
	static class TargetHuskGoal extends ActiveTargetGoal<LivingEntity>
	{
		public TargetHuskGoal(SwordmachineEntity sm)
		{
			super(sm, LivingEntity.class, 0, true, true, l -> l instanceof AbstractHuskEntity);
		}
		
		@Override
		public boolean canStart()
		{
			return ((SwordmachineEntity)mob).shouldHuntHusks() && super.canStart();
		}
		
		@Override
		public void start()
		{
			super.start();
			mob.setDespawnCounter(0);
		}
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
			return target != null && sm.navigation.isIdle() && sm.isIdle();
		}
		
		@Override
		public void tick()
		{
			sm.navigation.startMovingTo(target, 1f);
		}
		
		@Override
		public boolean shouldContinue()
		{
			return target != null && target.isAlive() && sm.squaredDistanceTo(target) > 1 && sm.isIdle();
		}
		
		@Override
		public void stop()
		{
			sm.navigation.stop();
		}
	}
	
	static class LookAroundGoal extends Goal
	{
		SwordmachineEntity sm;
		int timer;
		
		public LookAroundGoal(SwordmachineEntity sm)
		{
			this.sm = sm;
		}
		
		@Override
		public boolean canStart()
		{
			return sm.getTarget() == null && sm.getDataTracker().get(ANIMATION) == ANIMATION_IDLE && sm.getRandom().nextInt(400) == 0;
		}
		
		@Override
		public void start()
		{
			sm.getDataTracker().set(ANIMATION, ANIMATION_LOOK);
			timer = 0;
		}
		
		@Override
		public void tick()
		{
			timer++;
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return timer < 44 && sm.getTarget() == null || sm.getDataTracker().get(ANIMATION) != ANIMATION_LOOK;
		}
		
		@Override
		public void stop()
		{
			if(sm.getDataTracker().get(ANIMATION) == ANIMATION_LOOK)
				sm.getDataTracker().set(ANIMATION, ANIMATION_IDLE);
		}
	}
	
	static class ShotgunGoal extends Goal
	{
		SwordmachineEntity sm;
		LivingEntity target;
		int timer;
		
		public ShotgunGoal(SwordmachineEntity sm)
		{
			this.sm = sm;
		}
		
		@Override
		public boolean canStart()
		{
			target = sm.getTarget();
			return target != null && sm.isIdle() && sm.dataTracker.get(HAS_SHOTGUN) && sm.dataTracker.get(BLAST_COOLDOWN) == 0;
		}
		
		@Override
		public void start()
		{
			timer = 0;
			sm.dataTracker.set(BLASTING, true);
		}
		
		@Override
		public void tick()
		{
			sm.lookAtEntity(target, 30, 30);
			if(timer++ == 20)
				sm.fireShotgun();
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return target != null && target.isAlive() && timer < 40;
		}
		
		@Override
		public void stop()
		{
			sm.dataTracker.set(BLAST_COOLDOWN, 20);
			sm.dataTracker.set(BLASTING, false);
		}
	}
}
