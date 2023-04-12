package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.MeleeInterruptable;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.entity.husk.AbstractHuskEntity;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import absolutelyaya.ultracraft.entity.projectile.ThrownMachineSwordEntity;
import absolutelyaya.ultracraft.registry.DamageSources;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.Entity;
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
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SwordmachineEntity extends AbstractUltraHostileEntity implements GeoEntity, MeleeInterruptable, Enrageable
{
	private final ServerBossBar bossBar = new ServerBossBar(this.getDisplayName(), BossBar.Color.RED, BossBar.Style.PROGRESS);
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation HAND_CLOSED_ANIM = RawAnimation.begin().thenLoop("hand_closed");
	private static final RawAnimation RAND_IDLE_ANIM = RawAnimation.begin().thenPlay("look_around");
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("move");
	private static final RawAnimation BLAST_ANIM = RawAnimation.begin().thenLoop("shotgun");
	private static final RawAnimation BREAKDOWN_ANIM = RawAnimation.begin().thenPlay("breakdown");
	private static final RawAnimation ENRAGE_ANIM = RawAnimation.begin().thenPlay("enrage");
	private static final RawAnimation THROW_ANIM = RawAnimation.begin().thenPlay("throw");
	private static final RawAnimation SLASH_ANIM = RawAnimation.begin().thenPlay("slash");
	private static final RawAnimation COMBO_ANIM = RawAnimation.begin().thenPlay("combo");
	private static final RawAnimation SPIN_ANIM = RawAnimation.begin().thenPlay("spin");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	protected static final TrackedData<ItemStack> SWORD_STACK = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	protected static final TrackedData<Boolean> HAS_SHOTGUN = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> HAS_SWORD = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> BLASTING = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> ENRAGED_TICKS = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> BREAKDOWN_TICKS = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> MAX_STAMINA = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> BLAST_COOLDOWN = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> THROW_COOLDOWN = DataTracker.registerData(SwordmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_LOOK = 1;
	private static final byte ANIMATION_BREAKDOWN = 2;
	private static final byte ANIMATION_ENRAGE = 3;
	private static final byte ANIMATION_THROW = 4;
	private static final byte ANIMATION_SLASH = 5;
	private static final byte ANIMATION_COMBO = 6;
	private static final byte ANIMATION_SPIN = 7;
	
	public SwordmachineEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		((LivingEntityAccessor)this).SetTakePunchKnockbackSupplier(() -> false); //disable knockback
		if(!world.isClient())
			dataTracker.set(SWORD_STACK, ItemRegistry.MACHINE_SWORD.getSwordInstance((ServerWorld)world));
	}
	
	//TODO: Speed + Damage up when enraged
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(SWORD_STACK, ItemRegistry.MACHINE_SWORD.getDefaultStack());
		dataTracker.startTracking(ATTACK_COOLDOWN, 20);
		dataTracker.startTracking(BLAST_COOLDOWN, 0);
		dataTracker.startTracking(THROW_COOLDOWN, 120);
		dataTracker.startTracking(ENRAGED_TICKS, 0);
		dataTracker.startTracking(BREAKDOWN_TICKS, 0);
		dataTracker.startTracking(HAS_SHOTGUN, true);
		dataTracker.startTracking(HAS_SWORD, true);
		dataTracker.startTracking(BLASTING, false);
		dataTracker.startTracking(MAX_STAMINA, 100);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(HAS_SWORD) && dataTracker.get(HAS_SWORD))
			dataTracker.set(THROW_COOLDOWN, 200);
		if(data.equals(BREAKDOWN_TICKS))
		{
			int bt = dataTracker.get(BREAKDOWN_TICKS);
			if(bt == 58)
				dataTracker.set(HAS_SHOTGUN, false); //second frame of breakdown anim / lose shotgun
			else if(bt == 0 && getAnimation() == ANIMATION_BREAKDOWN)
				dataTracker.set(ANIMATION, ANIMATION_IDLE);
		}
		if(data.equals(ENRAGED_TICKS))
		{
			if(dataTracker.get(ENRAGED_TICKS) <= 200 && getAnimation() == ANIMATION_ENRAGE)
				dataTracker.set(ANIMATION, ANIMATION_IDLE);
		}
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
		goalSelector.add(1, new ThrowSwordGoal(this));
		goalSelector.add(1, new SlashGoal(this));
		goalSelector.add(1, new ComboGoal(this));
		goalSelector.add(1, new SpinGoal(this));
		goalSelector.add(3, new ChaseGoal(this));
		goalSelector.add(4, new LookAroundGoal(this));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
		targetSelector.add(0, new TargetHuskGoal(this));
	}
	
	@Override
	public void onInterrupt(PlayerEntity parrier)
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
			case ANIMATION_BREAKDOWN -> controller.setAnimation(BREAKDOWN_ANIM);
			case ANIMATION_ENRAGE -> controller.setAnimation(ENRAGE_ANIM);
			case ANIMATION_THROW -> controller.setAnimation(THROW_ANIM);
			case ANIMATION_SLASH -> controller.setAnimation(SLASH_ANIM);
			case ANIMATION_COMBO -> controller.setAnimation(COMBO_ANIM);
			case ANIMATION_SPIN -> controller.setAnimation(SPIN_ANIM);
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
	
	private <E extends GeoEntity> PlayState handPredicate(AnimationState<E> event)
	{
		//it's small details like this- that no one will notice lmao
		AnimationController<?> controller = event.getController();
		if(dataTracker.get(HAS_SWORD))
			controller.setAnimation(HAND_CLOSED_ANIM);
		else
			return PlayState.STOP;
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(
			new AnimationController<>(this, "main", 2, this::predicate),
			new AnimationController<>(this, "shotgun", 10, this::shotgunPredicate),
			new AnimationController<>(this, "hand", 15, this::handPredicate)
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
		nbt.putBoolean("sword", dataTracker.get(HAS_SWORD));
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if (hasCustomName())
			bossBar.setName(Text.translatable("entity.ultracraft.swordmachine-named", getDisplayName()));
		if(nbt.contains("shotgun", NbtElement.BYTE_TYPE))
			dataTracker.set(HAS_SHOTGUN, nbt.getBoolean("shotgun"));
		if(nbt.contains("sword", NbtElement.BYTE_TYPE))
			dataTracker.set(HAS_SWORD, nbt.getBoolean("sword"));
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
		if(dataTracker.get(BREAKDOWN_TICKS) > 0)
			dataTracker.set(BREAKDOWN_TICKS, dataTracker.get(BREAKDOWN_TICKS) - 1);
		if(dataTracker.get(ATTACK_COOLDOWN) > 0)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
		if(dataTracker.get(BLAST_COOLDOWN) > 0)
			dataTracker.set(BLAST_COOLDOWN, dataTracker.get(BLAST_COOLDOWN) - 1);
		if(dataTracker.get(THROW_COOLDOWN) > 0)
			dataTracker.set(THROW_COOLDOWN, dataTracker.get(THROW_COOLDOWN) - 1);
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		boolean b = super.damage(source, amount);
		bossBar.setPercent(getHealth() / getMaxHealth());
		if(dataTracker.get(HAS_SHOTGUN) && getHealth() < getMaxHealth() / 2)
		{
			dataTracker.set(BREAKDOWN_TICKS, 60);
			dataTracker.set(ANIMATION, ANIMATION_BREAKDOWN);
			if(world.getServer() == null || !(source.getAttacker() instanceof PlayerEntity))
				return b;
			Advancement advancement = world.getServer().getAdvancementLoader().get(new Identifier(Ultracraft.MOD_ID, "shotgun_get"));
			if(source.getAttacker() instanceof ServerPlayerEntity p && !p.getAdvancementTracker().getProgress(advancement).isDone())
			{
				LootTable lootTable = world.getServer().getLootManager().getTable(new Identifier(Ultracraft.MOD_ID, "entities/swordmachine_breakdown"));
				LootContext.Builder builder = getLootContextBuilder(source.getAttacker() instanceof PlayerEntity, source);
				lootTable.generateLoot(builder.build(LootContextTypes.ENTITY), this::dropStack);
				for (String string : p.getAdvancementTracker().getProgress(advancement).getUnobtainedCriteria())
					p.getAdvancementTracker().grantCriterion(advancement, string);
			}
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
		dataTracker.set(ENRAGED_TICKS, 250);
		dataTracker.set(ANIMATION, ANIMATION_ENRAGE);
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
	
	private void throwSword(LivingEntity target)
	{
		Vec3d dir = target.getPos().subtract(getPos()).normalize();
		ThrownMachineSwordEntity sword = ThrownMachineSwordEntity.spawn(world, this, ItemStack.EMPTY, 30);
		sword.setVelocity(dir);
		sword.setYaw(-(float)Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90);
		dataTracker.set(HAS_SWORD, false);
	}
	
	@Override
	public boolean isEnraged()
	{
		return dataTracker.get(ENRAGED_TICKS) > 0;
	}
	
	@Override
	public Vec3d getEnrageFeatureSize()
	{
		return new Vec3d(1f, -1f, -1f);
	}
	
	@Override
	public Vec3d getEnragedFeatureOffset()
	{
		return new Vec3d(0f, -2.5f, 0f);
	}
	
	private boolean isIdle()
	{
		if(dataTracker.get(BREAKDOWN_TICKS) > 0 || dataTracker.get(ENRAGED_TICKS) > 200)
			return false;
		return dataTracker.get(ANIMATION) == ANIMATION_IDLE || dataTracker.get(ANIMATION) == ANIMATION_LOOK;
	}
	
	public boolean hasShotgun()
	{
		return dataTracker.get(HAS_SHOTGUN);
	}
	
	private boolean shouldHuntHusks()
	{
		if(!hasCustomName() || getCustomName() == null)
			return false;
		return getCustomName().getString().equalsIgnoreCase("dan");
	}
	
	public void setHasSword(boolean b)
	{
		dataTracker.set(HAS_SWORD, b);
	}
	
	public boolean isHasSword()
	{
		return dataTracker.get(HAS_SWORD);
	}
	
	@Override
	public boolean isPushable()
	{
		return dataTracker.get(BREAKDOWN_TICKS) == 0 && dataTracker.get(ENRAGED_TICKS) <= 200;
	}
	
	public ItemStack getSwordStack()
	{
		return dataTracker.get(SWORD_STACK);
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
			if(timer++ == 10)
			{
				sm.setAttacking(true);
				sm.addParryIndicatorParticle(new Vec3d(0f, sm.getStandingEyeHeight(), -1f), true, false);
			}
			if(timer == 20)
			{
				sm.setAttacking(false);
				sm.fireShotgun();
			}
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return ((target != null && target.isAlive()) || timer > 20) && sm.isIdle() && timer < 40;
		}
		
		@Override
		public void stop()
		{
			sm.dataTracker.set(BLAST_COOLDOWN, 20);
			sm.dataTracker.set(BLASTING, false);
			if(sm.isAttacking())
				sm.setAttacking(false);
		}
	}
	
	static class ThrowSwordGoal extends Goal
	{
		SwordmachineEntity sm;
		LivingEntity target;
		int timer;
		
		public ThrowSwordGoal(SwordmachineEntity sm)
		{
			this.sm = sm;
		}
		
		@Override
		public boolean canStart()
		{
			if(sm.random.nextBetween(0, 4) != 0)
				return false;
			target = sm.getTarget();
			return target != null && sm.isIdle() && !sm.dataTracker.get(HAS_SHOTGUN) && sm.dataTracker.get(HAS_SWORD) && sm.dataTracker.get(THROW_COOLDOWN) == 0;
		}
		
		@Override
		public void start()
		{
			timer = 0;
			sm.dataTracker.set(ANIMATION, ANIMATION_THROW);
		}
		
		@Override
		public void tick()
		{
			sm.lookAtEntity(target, 30, 30);
			if(timer++ == 12)
				sm.throwSword(target);
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return target != null && target.isAlive() && sm.getAnimation() == ANIMATION_THROW && timer < 22;
		}
		
		@Override
		public boolean canStop()
		{
			return !shouldContinue();
		}
		
		@Override
		public void stop()
		{
			sm.dataTracker.set(THROW_COOLDOWN, 200);
			if(sm.getAnimation() == ANIMATION_THROW)
				sm.dataTracker.set(ANIMATION, ANIMATION_IDLE);
		}
	}
	
	static class SlashGoal extends Goal
	{
		List<Entity> damaged = new ArrayList<>();
		SwordmachineEntity sm;
		LivingEntity target;
		int timer;
		Vec3d direction;
		UUID trailID;
		
		public SlashGoal(SwordmachineEntity sm)
		{
			this.sm = sm;
		}
		
		@Override
		public boolean canStart()
		{
			if(sm.random.nextBetween(0, 3) != 0)
				return false;
			target = sm.getTarget();
			return target != null && sm.isIdle() && sm.dataTracker.get(HAS_SWORD) && sm.dataTracker.get(ATTACK_COOLDOWN) == 0 && sm.distanceTo(target) < 8;
		}
		
		@Override
		public void start()
		{
			timer = 0;
			sm.dataTracker.set(ANIMATION, ANIMATION_SLASH);
			direction = target.getPos().subtract(sm.getPos()).multiply(1f, 0f, 1f).normalize();
			sm.lookAtEntity(target, 360, 360);
			sm.setBodyYaw(sm.headYaw);
			damaged.clear();
			trailID = UUID.randomUUID();
		}
		
		@Override
		public void tick()
		{
			sm.setBodyYaw(sm.getYaw());
			
			if(timer++ == 4)
			{
				sm.addParryIndicatorParticle(new Vec3d(0f, sm.getStandingEyeHeight(), -1f), true, false);
				sm.setAttacking(true);
			}
			if(timer == 6)
				sm.setAttacking(false);
			if(timer == 9)
				UltracraftClient.TRAIL_RENDERER.createTrail(trailID,
				() -> {
					Vector3f left = sm.getPos().toVector3f().add(new Vector3f(0f, 1f, 1.5f).rotateY((float)Math.toRadians(sm.getYaw() + timer * 30)));
					Vector3f right = sm.getPos().toVector3f().add(new Vector3f(0f, 1f, 0.5f).rotateY((float)Math.toRadians(sm.getYaw() + timer * 30)));
					return new Pair<>(left, right);
				});
			if(timer > 10 && timer < 25)
			{
				sm.setVelocity(direction.multiply(0.75f));
				List<Entity> hit = sm.world.getOtherEntities(sm, sm.getBoundingBox().expand(2f, 0f, 2f),
						e -> (e instanceof PlayerEntity || (sm.shouldHuntHusks() && e instanceof AbstractHuskEntity)) && !damaged.contains(e));
				hit.forEach(e -> {
					if(e.damage(DamageSources.getSwordmachine(sm), 8))
						damaged.add(e);
				});
			}
			if(timer == 26)
				UltracraftClient.TRAIL_RENDERER.removeTrail(trailID);
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return sm.getAnimation() == ANIMATION_SLASH && timer < 35;
		}
		
		@Override
		public boolean canStop()
		{
			return !shouldContinue();
		}
		
		@Override
		public void stop()
		{
			sm.dataTracker.set(ATTACK_COOLDOWN, 40);
			if(sm.getAnimation() == ANIMATION_SLASH)
				sm.dataTracker.set(ANIMATION, ANIMATION_IDLE);
			damaged.clear();
			UltracraftClient.TRAIL_RENDERER.removeTrail(trailID);
		}
	}
	
	static class ComboGoal extends Goal
	{
		List<Entity> damaged = new ArrayList<>();
		SwordmachineEntity sm;
		LivingEntity target;
		int timer;
		Vec3d direction;
		UUID trailID;
		
		public ComboGoal(SwordmachineEntity sm)
		{
			this.sm = sm;
		}
		
		@Override
		public boolean canStart()
		{
			if(sm.random.nextBetween(0, 4) != 0)
				return false;
			target = sm.getTarget();
			return target != null && sm.isIdle() && sm.dataTracker.get(HAS_SWORD) &&
						   sm.dataTracker.get(ATTACK_COOLDOWN) == 0 && sm.distanceTo(target) < 8;
		}
		
		@Override
		public void start()
		{
			timer = 0;
			sm.dataTracker.set(ANIMATION, ANIMATION_COMBO);
			direction = target.getPos().subtract(sm.getPos()).multiply(1f, 0f, 1f).normalize();
			sm.lookAtEntity(target, 360, 360);
			sm.setBodyYaw(sm.headYaw);
			damaged.clear();
		}
		
		@Override
		public void tick()
		{
			sm.setBodyYaw(sm.getYaw());
			if(timer++ == 18 || timer == 38 || timer == 56)
			{
				damaged.clear();
				sm.lookAtEntity(target, 360, 360);
				sm.setAttacking(true);
				sm.addParryIndicatorParticle(new Vec3d(0f, sm.getStandingEyeHeight(), -1f), true, false);
			}
			else if(timer == 22 || timer == 41 || timer == 60)
				sm.setAttacking(false);
			else if(timer == 27 || timer == 42 || timer == 62)
			{
				UltracraftClient.TRAIL_RENDERER.removeTrail(trailID);
				trailID = UUID.randomUUID();
				switch (timer)
				{
					case 27 -> UltracraftClient.TRAIL_RENDERER.createTrail(trailID, this::getPointFirst); //6 ticks
					case 42 -> UltracraftClient.TRAIL_RENDERER.createTrail(trailID, this::getPointSecond); //6 ticks
					case 62 -> UltracraftClient.TRAIL_RENDERER.createTrail(trailID, this::getPointDownSlash); //4 ticks
					default -> {}
				}
				direction = target.getPos().subtract(sm.getPos()).multiply(1f, 0f, 1f).normalize();
			}
			if (timer == 33 || timer == 48 || timer == 66)
				UltracraftClient.TRAIL_RENDERER.removeTrail(trailID);
			
			if((timer >= 27 && timer <= 29) || (timer >= 42 && timer <= 44) || (timer == 62))
			{
				sm.setVelocity(direction.multiply(timer == 62 ? 0.75f : 1f));
				List<Entity> hit = sm.world.getOtherEntities(sm, sm.getBoundingBox().expand(3f, 0f, 3f),
						e -> (e instanceof PlayerEntity || (sm.shouldHuntHusks() && e instanceof AbstractHuskEntity)) && !damaged.contains(e));
				hit.forEach(e -> {
					if(e.damage(DamageSources.getSwordmachine(sm), 8))
						damaged.add(e);
				});
			}
		}
		
		Pair<Vector3f, Vector3f> getPointFirst()
		{
			float time = (timer - 27) / 6f;
			Vector3f left = sm.getPos().toVector3f().add(
					new Vector3f(0f, 1f, 2f).rotateY(-(float)Math.toRadians(sm.getYaw() - 135 + time * 225)));
			Vector3f right = sm.getPos().toVector3f().add(
					new Vector3f(0f, 1f, 2.5f).rotateY(-(float)Math.toRadians(sm.getYaw() - 135 + time * 225)));
			return new Pair<>(left, right);
		}
		
		Pair<Vector3f, Vector3f> getPointSecond()
		{
			float time = (timer - 42) / 6f;
			Vector3f left = sm.getPos().toVector3f().add(
					new Vector3f(0f, 1.2f - (0.2f * time), 2f).rotateY(-(float)Math.toRadians(sm.getYaw() + 45 - time * 225)));
			Vector3f right = sm.getPos().toVector3f().add(
					new Vector3f(0f, 1.2f - (0.2f * time), 2.5f).rotateY(-(float)Math.toRadians(sm.getYaw() + 45 - time * 225)));
			return new Pair<>(left, right);
		}
		
		Pair<Vector3f, Vector3f> getPointDownSlash()
		{
			float time = (timer - 62) / 4f;
			float yaw = sm.getYaw();
			float angle = time * 135;
			Vector3f rot = new Vector3f((float)Math.toRadians(angle), -(float)Math.toRadians(yaw), 0f);
			Vector3f left =
					sm.getPos().toVector3f().add(new Vector3f(0f, 3f, 0f).rotateX(rot.x).rotateY(rot.y));
			Vector3f right =
					sm.getPos().toVector3f().add(new Vector3f(0f, 3.5f, 0f).rotateX(rot.x).rotateY(rot.y));
			return new Pair<>(left, right);
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return sm.getAnimation() == ANIMATION_COMBO && timer < 80;
		}
		
		@Override
		public boolean canStop()
		{
			return !shouldContinue();
		}
		
		@Override
		public void stop()
		{
			sm.dataTracker.set(ATTACK_COOLDOWN, 40);
			if(sm.getAnimation() == ANIMATION_COMBO)
				sm.dataTracker.set(ANIMATION, ANIMATION_IDLE);
			damaged.clear();
			UltracraftClient.TRAIL_RENDERER.removeTrail(trailID);
			sm.setAttacking(false);
		}
	}
	
	static class SpinGoal extends Goal
	{
		List<Entity> damaged = new ArrayList<>();
		SwordmachineEntity sm;
		LivingEntity target;
		int timer;
		Vec3d direction;
		UUID trailID;
		
		public SpinGoal(SwordmachineEntity sm)
		{
			this.sm = sm;
		}
		
		@Override
		public boolean canStart()
		{
			if(sm.random.nextBetween(0, 4) != 0)
				return false;
			target = sm.getTarget();
			return target != null && sm.isIdle() && sm.dataTracker.get(HAS_SWORD) && !sm.dataTracker.get(HAS_SHOTGUN) &&
						   sm.dataTracker.get(ATTACK_COOLDOWN) == 0 && sm.distanceTo(target) < 8;
		}
		
		@Override
		public void start()
		{
			timer = 0;
			sm.dataTracker.set(ANIMATION, ANIMATION_SPIN);
			direction = target.getPos().subtract(sm.getPos()).multiply(1f, 0f, 1f).normalize();
			sm.lookAtEntity(target, 360, 360);
			sm.setBodyYaw(sm.headYaw);
			damaged.clear();
			trailID = UUID.randomUUID();
		}
		
		@Override
		public void tick()
		{
			sm.setBodyYaw(sm.getYaw());
			
			if(timer++ == 4)
			{
				sm.addParryIndicatorParticle(new Vec3d(0f, sm.getStandingEyeHeight(), -1f), true, false);
				sm.setAttacking(true);
			}
			if(timer == 6)
				sm.setAttacking(false);
			if(timer == 12)
				UltracraftClient.TRAIL_RENDERER.createTrail(trailID,
				() -> {
					float time = (timer - 12) / 28f * 3f;
					float angle = (float)Math.toRadians(time * -360f);
					Vector3f left = sm.getPos().toVector3f().add(new Vector3f(0f, 1f + time / 30f, 2f + time).rotateY(angle));
					Vector3f right = sm.getPos().toVector3f().add(new Vector3f(0f, 1f + time / 30f, 1.5f + time).rotateY(angle));
					return new Pair<>(left, right);
				});
			if(timer == 20 || timer == 30)
				damaged.clear();
			if(timer > 10 && timer < 60)
			{
				float time = (timer - 12) / 48f * 3f;
				List<Entity> hit = sm.world.getOtherEntities(sm, sm.getBoundingBox().expand(1f + time, 0f, 1f + time),
						e -> (e instanceof PlayerEntity || (sm.shouldHuntHusks() && e instanceof AbstractHuskEntity)) && !damaged.contains(e));
				hit.forEach(e -> {
					if(e.damage(DamageSources.getSwordmachine(sm), 8))
						damaged.add(e);
				});
			}
			if(timer == 40)
				UltracraftClient.TRAIL_RENDERER.removeTrail(trailID);
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return sm.getAnimation() == ANIMATION_SPIN && timer < 60;
		}
		
		@Override
		public boolean canStop()
		{
			return !shouldContinue();
		}
		
		@Override
		public void stop()
		{
			sm.dataTracker.set(ATTACK_COOLDOWN, 40);
			if(sm.getAnimation() == ANIMATION_SPIN)
				sm.dataTracker.set(ANIMATION, ANIMATION_IDLE);
			damaged.clear();
			UltracraftClient.TRAIL_RENDERER.removeTrail(trailID);
		}
	}
}
