package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.entity.IAntiCheeseBoss;
import absolutelyaya.ultracraft.entity.goal.AntiCheeseProximityTargetGoal;
import absolutelyaya.ultracraft.entity.other.ProgressionItemEntity;
import absolutelyaya.ultracraft.entity.projectile.BeamProjectileEntity;
import absolutelyaya.ultracraft.entity.projectile.EjectedCoreEntity;
import absolutelyaya.ultracraft.entity.projectile.ShotgunPelletEntity;
import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.azure.azurelib.core.animation.AnimationState;

import java.util.EnumSet;

public class V2Entity extends AbstractUltraHostileEntity implements IAntiCheeseBoss, GeoEntity, Enrageable
{
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("idle");
	private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenPlay("walk");
	private static final RawAnimation FALL_ANIM = RawAnimation.begin().thenPlay("fall");
	private static final RawAnimation INTRO_ANIM = RawAnimation.begin().thenPlay("intro").thenPlay("idle");
	private static final RawAnimation SLIDE_ANIM = RawAnimation.begin().thenPlay("slide_start").thenPlay("slide_loop");
	private static final RawAnimation OUTRO_ANIM = RawAnimation.begin().thenPlay("outro");
	static protected final TrackedData<Integer> FRUSTRATION = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	static protected final TrackedData<Integer> IDLE_TIMER = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	static protected final TrackedData<Integer> INTRO_TICKS = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	static protected final TrackedData<Integer> OUTRO_TICKS = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.INTEGER);
	static protected final TrackedData<Byte> MOVEMENT_MODE = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.BYTE);
	static protected final TrackedData<Boolean> ENRAGED = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
	static protected final TrackedData<ItemStack> WEAPON = DataTracker.registerData(V2Entity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	protected static final byte ANIMATION_IDLE = 0;
	protected static final byte ANIMATION_INTRO = 1;
	protected static final byte ANIMATION_SLIDE = 2;
	protected static final byte ANIMATION_OUTRO = 3;
	
	int wallJumps = 3, ferocity, movementChangeCD = 0, attackCD = 60, activeAttack = -1, shots;
	SnowballEntity escapePearl;
	Vec3d nextShotDir;
	DamageSource killingBlow;
	
	public V2Entity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		((LivingEntityAccessor)this).setTakePunchKnockbackSupplier(() -> false); //disable knockback
		moveControl = new V2MoveControl(this);
	}
	
	public static DefaultAttributeContainer getDefaultAttributes()
	{
        return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d).build();
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(FRUSTRATION, 0);
		dataTracker.startTracking(IDLE_TIMER, 0);
		dataTracker.startTracking(INTRO_TICKS, 0);
		dataTracker.startTracking(OUTRO_TICKS, 0);
		dataTracker.startTracking(MOVEMENT_MODE, (byte)0);
		dataTracker.startTracking(ENRAGED, false);
		dataTracker.startTracking(WEAPON, ItemRegistry.CORE_SHOTGUN.getDefaultStack());
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(INTRO_TICKS) && getAnimation() == ANIMATION_INTRO && dataTracker.get(INTRO_TICKS) >= 100)
		{
			dataTracker.set(INTRO_TICKS, -1);
			dataTracker.set(ANIMATION, ANIMATION_IDLE);
		}
	}
	
	@Override
	protected void initGoals()
	{
		super.initGoals();
		goalSelector.add(0, new YellowMovementGoal(this));
		goalSelector.add(0, new BlueMovementGoal(this));
		goalSelector.add(0, new RedMovementGoal(this));
		goalSelector.add(0, new GreenMovementGoal(this));
		
		targetSelector.add(0, new AntiCheeseProximityTargetGoal<>(this, PlayerEntity.class, 20, 32));
		targetSelector.add(1, new RevengeGoal(this, V2Entity.class));
	}
	
	@Override
	public void tick()
	{
		super.tick();
		dataTracker.set(FRUSTRATION, dataTracker.get(FRUSTRATION) + 1);
		if(getTarget() == null)
		{
			ferocity = 60;
			if(getHealth() < getMaxHealth())
			{
				dataTracker.set(IDLE_TIMER, dataTracker.get(IDLE_TIMER) + 1);
				if(dataTracker.get(IDLE_TIMER) > 1200)
				{
					if(age % 20 == 0)
						heal(1f);
					if(isEnraged())
					{
						dataTracker.set(ENRAGED, false);
						ferocity = 0;
					}
				}
			}
		}
		else if(dataTracker.get(IDLE_TIMER) > 0)
			dataTracker.set(IDLE_TIMER, 0);
		if(!finishedIntro())
		{
			if(isOnGround() && getAnimation() != ANIMATION_INTRO)
				dataTracker.set(ANIMATION, ANIMATION_INTRO);
			if(getAnimation() == ANIMATION_INTRO)
				dataTracker.set(INTRO_TICKS, dataTracker.get(INTRO_TICKS) + 1);
		}
		int outro;
		if((outro = dataTracker.get(OUTRO_TICKS)) > 0)
		{
			dataTracker.set(OUTRO_TICKS, outro + 1);
			if(getWorld().isClient)
				return;
			if(outro >= 133)
				trueDeath();
			else if(outro == 77)
				escapePearl = throwEscapeEnderPearl();
			else if(outro == 100 && escapePearl != null && !escapePearl.isRemoved())
				escapePearl.discard();
		}
	}
	
	public SnowballEntity throwEscapeEnderPearl()
	{
		SnowballEntity pearl = new SnowballEntity(getWorld(), this);
		pearl.setItem(Items.ENDER_PEARL.getDefaultStack());
		pearl.setPosition(getEyePos());
		pearl.setVelocity(getRotationVector().multiply(0.75f, 0f, 0.75f).add(0, 1f, 0));
		pearl.noClip = true;
		getWorld().spawnEntity(pearl);
		return pearl;
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(getAnimation() != ANIMATION_SLIDE)
		{
			prevYaw = getYaw();
			setYaw(MathHelper.lerp(0.1f, getYaw(), getHeadYaw()));
		}
		else
		{
			Vec3d dir = getVelocity().multiply(1.0, 0.0, 1.0).normalize();
			Vec3d particleVel = new Vec3d(-dir.x, -dir.y, -dir.z).multiply(random.nextDouble() * 0.1 + 0.025);
			Vec3d pos = getPos().add(dir.multiply(1.5));
			getWorld().addParticle(ParticleRegistry.SLIDE, false, pos.x, pos.y + 0.1, pos.z, particleVel.x, particleVel.y, particleVel.z);
		}
		if(wallJumps < 3 && groundCollision)
			wallJumps = 3;
		if(getTarget() != null && !(Ultracraft.isTimeFrozen() || isPlayingIntro() || isPlayingOutro()))
		{
			updateMovementGoal();
			tickAttack();
		}
	}
	
	void updateMovementGoal()
	{
		LivingEntity target = getTarget();
		float targetDistance = distanceTo(target);
		boolean rage = isEnraged();
		if(targetDistance >= 8f && !rage)
			ferocity += 1;
		else if(targetDistance < 4f)
			ferocity -= 2;
		else if(targetDistance < 8f && !rage && ferocity > 0)
			ferocity--;
		if(ferocity >= 500 && !isEnraged())
		{
			enrage();
			return;
		}
		if(rage)
		{
			if(getMovementMode() != 2)
				setMovementMode(2);
			return;
		}
		if(movementChangeCD > 0)
		{
			movementChangeCD--;
			return;
		}
		if(rage && ferocity <= 0)
			dataTracker.set(ENRAGED, false);
		if(ferocity <= 0 && targetDistance < 4f && target.getHealth() > target.getMaxHealth() / 3f )
				setMovementMode(3);
		if(getMovementMode() == 3)
		{
			if(targetDistance > 10f)
				setMovementMode(random.nextBetween(0, 1));
			return;
		}
		float changeOdds = 0.05f;
		if(getMovementMode() == 2)
			changeOdds += 0.04f;
		if(ferocity < 150 && random.nextFloat() < changeOdds)
			setMovementMode(getMovementMode() == 0 ? 1 : 0);
		else if(ferocity > 400 && random.nextFloat() < 0.05f)
			setMovementMode(2);
		else if(ferocity > 250 && random.nextFloat() < 0.01f)
			setMovementMode(2);
		else if(ferocity > 150 && random.nextFloat() < changeOdds)
			setMovementMode(random.nextBetween(0, 2));
	}
	
	void tickAttack()
	{
		if(getTarget() == null)
		{
			attackCD = 60;
			return;
		}
		else if(attackCD > 0)
		{
			attackCD--;
			return;
		}
		switch(activeAttack)
		{
			default -> {
				float dist = distanceTo(getTarget());
				if(dist > 12f)
				{
					activeAttack = random.nextInt(2);
					shots = -1;
					if(!dataTracker.get(WEAPON).isOf(ItemRegistry.PIERCE_REVOLVER))
						dataTracker.set(WEAPON, ItemRegistry.PIERCE_REVOLVER.getDefaultStack());
				}
				else
				{
					if(random.nextFloat() < 0.25f)
					{
						shots = -1;
						activeAttack = 3; //core_eject (rarer)
					}
					else
						activeAttack = 2;
					if(!dataTracker.get(WEAPON).isOf(ItemRegistry.CORE_SHOTGUN))
						dataTracker.set(WEAPON, ItemRegistry.CORE_SHOTGUN.getDefaultStack());
				}
			}
			case 0 -> { //revolver three shots
				if(shots == 0)
				{
					activeAttack = -1;
					attackCD = 10 + random.nextInt(20);
					return;
				}
				if(shots == -1)
				{
					shots = 3;
					attackCD = 20;
					getWorld().sendEntityStatus(this, EntityStatuses.PLAY_ATTACK_SOUND); //flash
					nextShotDir = null;
				}
				else if(shots > 0)
				{
					if(nextShotDir != null)
					{
						fireRevolver();
						shots--;
						attackCD = 10;
						nextShotDir = null;
					}
					else
					{
						attackCD = 3;
						nextShotDir = aim(0.1f);
					}
				}
				else
					activeAttack = -1; //safeguard
			}
			case 1 -> { //piercer shot
				if(shots == -1)
				{
					shots = 1;
					attackCD = isEnraged() ? 20 : 40;
					playSound(SoundRegistry.V2_PIERCER_TELL, 1.5f, 1f);
				}
				else if(shots == 1)
				{
					if(nextShotDir == null)
					{
						nextShotDir = aim(0.1f);
						attackCD = 4;
						return;
					}
					firePiercer();
					shots = 0;
					activeAttack = -1;
					if(!isEnraged())
						attackCD = 20 + random.nextInt(20);
				}
			}
			case 2 -> { //shotgun shot
				fireShotgun();
				activeAttack = -1;
				attackCD = (isEnraged() ? 10 : 30) + random.nextInt(20);
			}
			case 3 -> { //core eject
				if(shots == -1)
				{
					shots = 1;
					attackCD = 30;
					playSound(SoundRegistry.V2_CORE_EJECT_TELL, 1.5f, 1f);
				}
				else if(shots == 1)
				{
					ejectCore();
					shots = 0;
					activeAttack = -1;
					attackCD = 20 + random.nextInt(30);
				}
			}
		}
	}
	
	void fireRevolver()
	{
		BeamProjectileEntity beam = BeamProjectileEntity.spawn(getWorld(), this, 5f, ServerHitscanHandler.NORMAL);
		beam.setVelocity(nextShotDir.multiply(7.5f));
		beam.setDamage(1.5f);
		playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.75f, 0.9f + (getRandom().nextFloat() - 0.5f) * 0.2f);
	}
	
	void firePiercer()
	{
		BeamProjectileEntity beam = BeamProjectileEntity.spawn(getWorld(), this, 5f, ServerHitscanHandler.REVOLVER_PIERCE);
		beam.setVelocity(nextShotDir.multiply(7.5f));
		beam.setDamage(2f);
		playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1f, 0.85f + (getRandom().nextFloat() - 0.5f) * 0.2f);
	}
	
	Vec3d aim(float predict)
	{
		return getTarget().getPos().add(getTarget().getVelocity().normalize().multiply(predict)).subtract(getPos()).normalize();
	}
	
	void fireShotgun()
	{
		Vec3d dir = getTarget().getEyePos().subtract(getEyePos()).normalize();
		for (int i = 0; i < 16; i++)
		{
			ShotgunPelletEntity bullet = ShotgunPelletEntity.spawn(this, getWorld(), false);
			bullet.setVelocity(dir.x, dir.y, dir.z, 1f, 20f);
			Vec3d vel = bullet.getVelocity();
			getWorld().addParticle(ParticleTypes.SMOKE, bullet.getX(), bullet.getY(), bullet.getZ(), vel.x, vel.y, vel.z);
			bullet.setNoGravity(true);
			bullet.setIgnored(getClass());
			getWorld().spawnEntity(bullet);
		}
		playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 0.2f / (getRandom().nextFloat() * 0.2f + 0.6f));
	}
	
	void ejectCore()
	{
		Vec3d dir = getTarget().getPos().subtract(getPos()).normalize();
		EjectedCoreEntity core = EjectedCoreEntity.spawn(this, getWorld());
		core.setVelocity(dir.multiply(0.33f, 0, 0.33f).add(0, 0.2, 0));
		getWorld().spawnEntity(core);
	}
	
	public ItemStack getWeapon()
	{
		return dataTracker.get(WEAPON);
	}
	
	void enrage()
	{
		dataTracker.set(ENRAGED, true);
		playSound(SoundRegistry.GENERIC_ENRAGE, 10f, 1f);
	}
	
	void setMovementMode(int i)
	{
		dataTracker.set(MOVEMENT_MODE, (byte)i);
		movementChangeCD = 100;
	}
	
	@Override
	protected void jump()
	{
		if(!groundCollision)
		{
			if(wallJumps-- <= 0)
				return;
		}
		super.jump();
	}
	
	@Override
	public int getFrustration()
	{
		return dataTracker.get(FRUSTRATION);
	}
	
	@Override
	public void resetFrustration()
	{
		dataTracker.set(FRUSTRATION, 0);
	}
	
	private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> ctx)
	{
		switch(getAnimation())
		{
			case ANIMATION_IDLE -> ctx.setAnimation(isOnGround() ? IDLE_ANIM : FALL_ANIM);
			case ANIMATION_INTRO -> ctx.setAnimation(INTRO_ANIM);
			case ANIMATION_SLIDE -> ctx.setAnimation(SLIDE_ANIM);
			case ANIMATION_OUTRO -> ctx.setAnimation(OUTRO_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	private <T extends GeoAnimatable> PlayState legPredicate(AnimationState<T> ctx)
	{
		if(getAnimation() == ANIMATION_IDLE && ctx.isMoving() && isOnGround())
		{
			AnimationController<?> controller = ctx.getController();
			controller.setAnimationSpeed(1.65f);
			ctx.setAnimation(WALK_ANIM);
			return PlayState.CONTINUE;
		}
		return PlayState.STOP;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(
				new AnimationController<>(this, "main", 2, this::predicate),
				new AnimationController<>(this, "legs", 2, this::legPredicate));
	}
	
	double getSpeedAttribute()
	{
		return getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	protected boolean getBossDefault()
	{
		return true;
	}
	
	@Override
	public boolean cannotDespawn()
	{
		return true;
	}
	
	@Override
	public boolean isEnraged()
	{
		return dataTracker.get(ENRAGED);
	}
	
	@Override
	public boolean isFireImmune()
	{
		return true;
	}
	
	boolean isPlayingIntro()
	{
		return dataTracker.get(INTRO_TICKS) >= 0;
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(isPlayingIntro() && !source.isOf(DamageSources.COIN_PUNCH))
			return false;
		if(source.isOf(DamageTypes.FALL))
			return false;
		if(getHealth() - amount <= 0 || isPlayingOutro())
		{
			if(!getWorld().isClient && !isPlayingOutro())
			{
				dataTracker.set(ANIMATION, ANIMATION_OUTRO);
				dataTracker.set(OUTRO_TICKS, 1);
				dataTracker.set(ENRAGED, false);
				dataTracker.set(WEAPON, ItemStack.EMPTY);
				setHealth(1f);
				bossBar.setPercent(0f);
				if(source.getAttacker() instanceof LivingEntity living)
					setAttacker(living);
				LivingEntity adversary = getPrimeAdversary();
				if(adversary != null)
					adversary.updateKilledAdvancementCriterion(this, 1, source);
				killingBlow = source;
				playSound(SoundRegistry.V2_DEATH, 1f, 1f);
				return false;
			}
			return false;
		}
		return super.damage(source, amount);
	}
	
	@Override
	public Vec3d getEnrageFeatureSize()
	{
		return new Vec3d(1f, -1f, -1f);
	}
	
	@Override
	public Vec3d getEnragedFeatureOffset()
	{
		return new Vec3d(0f, -2f, 0f);
	}
	
	@Override
	public Box getBoundingBox(EntityPose pose)
	{
		if(getAnimation() == ANIMATION_SLIDE)
			return Box.of(getPos(), 0.3f, 0.5f, 0.3f);
		return super.getBoundingBox(pose);
	}
	
	/**
	 * 0 : yellow -> random movement + jumping<br>
	 * 1 : blue -> circle target<br>
	 * 2 : red -> approach, slide and circle target (closer)<br>
	 * 3 : green -> flee<br>
	 */
	public int getMovementMode()
	{
		return dataTracker.get(MOVEMENT_MODE);
	}
	
	public boolean shouldHideWings()
	{
		return dataTracker.get(INTRO_TICKS) < 5 && !finishedIntro();
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean finishedIntro()
	{
		return dataTracker.get(INTRO_TICKS) == -1;
	}
	
	public boolean isPlayingOutro()
	{
		return dataTracker.get(OUTRO_TICKS) > 0;
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("playIntro", !finishedIntro());
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if(nbt.contains("playIntro", NbtElement.BYTE_TYPE) && !nbt.getBoolean("playIntro"))
			dataTracker.set(INTRO_TICKS, -1);
	}
	
	void trueDeath()
	{
		getWorld().sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
		if(isBoss() && getWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT))
		{
			ProgressionItemEntity item = ProgressionItemEntity.spawn(getWorld(), getPos(), "ultracraft:knuckleblaster",
					ItemRegistry.KNUCKLEBLASTER.getDefaultStack(), getRandom());
			item.setNoPickup();
			if(killingBlow != null)
				dropLoot(killingBlow, killingBlow.getAttacker() != null && killingBlow.getAttacker().isPlayer());
		}
		remove(Entity.RemovalReason.KILLED);
	}
	
	@Override
	public void takeKnockback(double strength, double x, double z)
	{
	
	}
	
	@Override
	public void handleStatus(byte status)
	{
		if(status == EntityStatuses.ADD_DEATH_PARTICLES)
		{
			for (int i = 0; i < 20; i++)
			{
				EntityDimensions dimensions = getDimensions(getPose());
				Vec3d pos = getBoundingBox().getCenter().add((random.nextFloat() - 0.5f) * dimensions.width,
						(random.nextFloat() - 0.5f) * dimensions.height,
						(random.nextFloat() - 0.5f) * dimensions.width);
				getWorld().addParticle(ParticleTypes.PORTAL, pos.x, pos.y, pos.z,
						(random.nextFloat() - 0.5f) * 0.25f, (random.nextFloat() - 0.5f) * 0.25f, (random.nextFloat() - 0.5f) * 0.25f);
			}
			getWorld().playSound(this, getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 4f, 1f);
			return;
		}
		if(status == EntityStatuses.PLAY_ATTACK_SOUND)
		{
			Vec3d pos = getEyePos().add(getRotationVector()).subtract(0, 0.25, 0);
			getWorld().addParticle(new ParryIndicatorParticleEffect(false), pos.x, pos.y, pos.z, 0f, 0f, 0f);
			playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 5f, 1.25f);
			return;
		}
		super.handleStatus(status);
	}
	
	static class V2MoveControl extends MoveControl
	{
		public V2MoveControl(MobEntity entity)
		{
			super(entity);
		}
		
		public void strafeTo(float forward, float sideways, float speed)
		{
			if(entity instanceof V2Entity v2 && (v2.isPlayingIntro() || v2.isPlayingOutro()))
			{
				forwardMovement = sidewaysMovement = 0f;
				return;
			}
			state = State.STRAFE;
			forwardMovement = forward;
			sidewaysMovement = sideways;
			this.speed = speed;
		}
	}
	
	static class V2MovementGoal extends Goal
	{
		protected final V2Entity mob;
		
		public V2MovementGoal(V2Entity mob)
		{
			this.mob = mob;
			setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
		}
		
		@Override
		public boolean canStart()
		{
			return !mob.isPlayingIntro() && !mob.isPlayingOutro();
		}
		
		@Override
		public boolean shouldContinue()
		{
			if(mob.isPlayingOutro())
				return false;
			return super.shouldContinue();
		}
	}
	
	static class YellowMovementGoal extends V2MovementGoal //Random Direction + Jumping
	{
		int cooldown;
		Vec3d dir;
		
		public YellowMovementGoal(V2Entity mob)
		{
			super(mob);
		}
		
		@Override
		public boolean canStart()
		{
			if(!super.canStart())
				return false;
			return mob.getTarget() != null && mob.dataTracker.get(MOVEMENT_MODE) == 0;
		}
		
		@Override
		public void start()
		{
			super.start();
			changeDirection();
		}
		
		void changeDirection()
		{
			Vec3d wallDir = Vec3d.ZERO;
			Direction[] dirs = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
			for (Direction dir : dirs)
			{
				BlockPos pos = mob.getBlockPos().offset(dir);
				Vec3i vec = dir.getVector();
				if (mob.getWorld().getBlockState(pos).isFullCube(mob.getWorld(), pos))
					wallDir = wallDir.add(vec.getX() * 0.35f, vec.getY() * 0.35f, vec.getZ() * 0.35f);
			}
			dir = new Vec3d(mob.random.nextFloat() - 0.5f, 0f, mob.random.nextFloat() - 0.5f).subtract(wallDir).normalize();
			cooldown = 300 + (int)(300 * mob.random.nextFloat());
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public void tick()
		{
			if(Ultracraft.isTimeFrozen())
				return;
			if(dir == null)
			{
				changeDirection();
				return;
			}
			super.tick();
			mob.getLookControl().lookAt(mob.getTarget());
			mob.setVelocity(dir.multiply(mob.getSpeedAttribute() * 1.5f).add(0, mob.getVelocity().y, 0));
			cooldown--;
			if(mob.getRandom().nextFloat() < 0.005f && mob.isOnGround())
				mob.jump();
			if(mob.horizontalCollision || cooldown <= 0)
			{
				changeDirection();
				if(mob.getRandom().nextFloat() < 0.1f)
					mob.jump();
			}
			if(mob.distanceTo(mob.getTarget()) > 24f)
				dir = mob.getTarget().getPos().subtract(mob.getPos()).normalize();
		}
		
		@Override
		public boolean shouldContinue()
		{
			return mob.dataTracker.get(MOVEMENT_MODE) == 0 && mob.getTarget() != null && super.shouldContinue();
		}
	}
	
	static class BlueMovementGoal extends V2MovementGoal //Circle Target
	{
		float dir;
		
		public BlueMovementGoal(V2Entity mob)
		{
			super(mob);
		}
		
		@Override
		public boolean canStart()
		{
			if(!super.canStart())
				return false;
			return mob.getTarget() != null && mob.dataTracker.get(MOVEMENT_MODE) == 1;
		}
		
		@Override
		public void start()
		{
			super.start();
			dir = mob.getRandom().nextBoolean() ? 1 : -1;
		}
		
		@Override
		public void tick()
		{
			if(Ultracraft.isTimeFrozen())
				return;
			mob.getLookControl().lookAt(mob.getTarget(), 30.0f, 30.0f);
			float forward = 0.5f;
			float dirChangeChance = 0.01f;
			if(mob.horizontalCollision)
			{
				forward += 0.2f;
				dirChangeChance = 0.1f;
				if(mob.getRandom().nextFloat() < 0.1f)
					mob.jump();
			}
			if(mob.getRandom().nextFloat() < dirChangeChance)
				dir = (dir == 1 ? -1 : 1);
			if(mob.getMoveControl() instanceof V2MoveControl moveControl)
				moveControl.strafeTo(mob.distanceTo(mob.getTarget()) < 4f ? 0f : forward, dir, 0.5f);
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return mob.dataTracker.get(MOVEMENT_MODE) == 1 && mob.getTarget() != null && super.shouldContinue();
		}
		
		@Override
		public void stop()
		{
			super.stop();
			mob.getMoveControl().strafeTo(0f, 0f);
		}
	}
	
	static class RedMovementGoal extends V2MovementGoal //Rapid, Aggressive approach
	{
		Vec3d slideDir;
		int slideTimer;
		boolean circleDir;
		
		public RedMovementGoal(V2Entity mob)
		{
			super(mob);
		}
		
		@Override
		public boolean canStart()
		{
			if(!super.canStart())
				return false;
			return mob.getTarget() != null && mob.dataTracker.get(MOVEMENT_MODE) == 2;
		}
		
		@Override
		public void start()
		{
			super.start();
			circleDir = mob.random.nextBoolean();
		}
		
		@Override
		public void tick()
		{
			if(Ultracraft.isTimeFrozen())
				return;
			super.tick();
			float targetDistance = mob.distanceTo(mob.getTarget());
			mob.getLookControl().lookAt(mob.getTarget(), 20.0f, 20.0f);
			if(mob.getAnimation() != ANIMATION_SLIDE) //Not Sliding
			{
				float jumpChance = 0.01f;
				if(mob.getTarget().getPos().y - 2 < mob.getPos().y)
					jumpChance = 0.1f;
				if(mob.horizontalCollision && mob.getRandom().nextFloat() < jumpChance)
					mob.jump();
				if (targetDistance > 10f) //Start sliding
				{
					if(mob.canSee(mob.getTarget()) && mob.getRandom().nextFloat() < 0.025f)
					{
						mob.getDataTracker().set(ANIMATION, ANIMATION_SLIDE);
						slideDir = mob.getTarget().getPos().add(mob.getTarget().getVelocity().multiply(1.5f)).subtract(mob.getPos())
										   .multiply(1, 0, 1).normalize();
						slideTimer = 200 + mob.getRandom().nextInt(100);
					}
				}
				if(targetDistance > 4f)
				{
					Vec3d dest = mob.getTarget().getPos();
					mob.moveControl.moveTo(dest.x, dest.y, dest.z, 1.4f);
				}
				else //Circle Target
				{
					if(mob.getMoveControl() instanceof V2MoveControl moveControl)
						moveControl.strafeTo(targetDistance < 3f ? 0f : 0.2f, 0.8f * (circleDir ? 1 : -1), 0.5f);
				}
			}
			else //Sliding
			{
				if(targetDistance < 5f || slideTimer-- <= 0 || mob.horizontalCollision)
					mob.dataTracker.set(ANIMATION, ANIMATION_IDLE);
				mob.setVelocity(slideDir.multiply(mob.getSpeedAttribute() * 1.9f).add(0, mob.getVelocity().y, 0));
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
			return mob.dataTracker.get(MOVEMENT_MODE) == 2 && mob.getTarget() != null && super.shouldContinue();
		}
		
		@Override
		public void stop()
		{
			super.stop();
			if(mob.getAnimation() == ANIMATION_SLIDE)
				mob.getDataTracker().set(ANIMATION, ANIMATION_IDLE);
		}
	}
	
	static class GreenMovementGoal extends V2MovementGoal //Flee
	{
		int timer;
		Path path;
		
		public GreenMovementGoal(V2Entity mob)
		{
			super(mob);
		}
		
		@Override
		public boolean canStart()
		{
			if(!super.canStart())
				return false;
			if(mob.getTarget() == null)
				return false;
			LivingEntity target = mob.getTarget();
			Vec3d vec3d = NoPenaltyTargeting.findFrom(this.mob, 16, 7, mob.getTarget().getPos());
			if (vec3d == null)
				return false;
			path = mob.getNavigation().findPathTo(vec3d.x, vec3d.y, vec3d.z, 0);
			return path != null && target.getHealth() > target.getMaxHealth() / 3f && mob.dataTracker.get(MOVEMENT_MODE) == 3;
		}
		
		@Override
		public void start()
		{
			super.start();
			mob.addVelocity(mob.getPos().subtract(mob.getTarget().getPos()).normalize().multiply(2f));
			mob.ferocity += 50;
			timer = 20;
			mob.getNavigation().startMovingAlong(path, mob.getSpeedAttribute() * 1.25f);
		}
		
		@Override
		public void tick()
		{
			if(Ultracraft.isTimeFrozen())
				return;
			mob.getLookControl().lookAt(mob.getTarget(), 30.0f, 30.0f);
			if(timer-- <= 0)
				mob.setMovementMode(mob.getRandom().nextInt(2)); //either yellow or blue
			super.tick();
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public boolean shouldContinue()
		{
			return mob.dataTracker.get(MOVEMENT_MODE) == 3 && mob.getTarget() != null && timer > 0 && super.shouldContinue();
		}
		
		@Override
		public void stop()
		{
			super.stop();
			mob.navigation.stop();
			path = null;
		}
	}
}
