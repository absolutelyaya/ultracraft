package absolutelyaya.ultracraft.entity.demon;

import absolutelyaya.goop.api.WaterHandling;
import absolutelyaya.goop.particles.GoopDropParticleEffect;
import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.accessor.IAnimatedEnemy;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.entity.goal.TimedAttackGoal;
import absolutelyaya.ultracraft.entity.other.ShockwaveEntity;
import absolutelyaya.ultracraft.entity.other.VerticalShockwaveEntity;
import absolutelyaya.ultracraft.entity.projectile.HarpoonEntity;
import absolutelyaya.ultracraft.entity.projectile.HideousMortarEntity;
import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
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

import java.util.List;

public class HideousMassEntity extends AbstractUltraHostileEntity implements GeoEntity, IAnimatedEnemy, Enrageable
{
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> MORTAR_COUNTER = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> SLAM_COUNTER = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Boolean> ENRAGED = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> LAYING = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> HAS_HARPOON = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Float> HARPOON_HEALTH = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Integer> DEATH = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Boolean> HIDDEN = DataTracker.registerData(HideousMassEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	static final RawAnimation POSE_ANIM = RawAnimation.begin().thenPlay("pose");
	static final RawAnimation LAY_POSE_ANIM = RawAnimation.begin().thenPlay("lay_pose");
	static final RawAnimation MORTAR_ANIM = RawAnimation.begin().thenPlay("mortar");
	static final RawAnimation SLAM_STANDING_ANIM = RawAnimation.begin().thenPlay("stand_slam_start").thenPlay("slam");
	static final RawAnimation SLAM_LAYING_ANIM = RawAnimation.begin().thenPlay("lay_slam_start").thenPlay("slam");
	static final RawAnimation STAND_UP_ANIM = RawAnimation.begin().thenPlay("stand_up");
	static final RawAnimation CLAP_ANIM = RawAnimation.begin().thenPlay("clap");
	static final RawAnimation HARPOON_ANIM = RawAnimation.begin().thenPlay("harpoon");
	static final RawAnimation ENRAGED_ANIM = RawAnimation.begin().thenPlay("enrage_start").thenPlay("enrage_loop");
	static final RawAnimation TURN_ANIM = RawAnimation.begin().thenPlay("turn");
	static final RawAnimation TURN_LAYING_ANIM = RawAnimation.begin().thenPlay("lay_turn");
	static final RawAnimation ENRAGED_TURN_ANIM = RawAnimation.begin().thenPlay("enrage_turn");
	static final RawAnimation HIDE_POSE_ANIM = RawAnimation.begin().thenPlay("hide_pose");
	static final RawAnimation UNHIDE_ANIM = RawAnimation.begin().thenPlay("unhide");
	final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	private static final byte ANIMATION_IDLE = 0;
	private static final byte ANIMATION_MORTAR = 1;
	private static final byte ANIMATION_SLAM_STANDING = 2;
	private static final byte ANIMATION_SLAM_LAYING = 3;
	private static final byte ANIMATION_STAND_UP = 4;
	private static final byte ANIMATION_CLAP = 5;
	private static final byte ANIMATION_HARPOON = 6;
	private static final byte ANIMATION_ENRAGED = 7;
	private static final byte ANIMATION_UNHIDE = 8;
	private static final Vec3d[] partPosDefault, partPosMortar, partPosLaying, partPosEnraged;
	private final HideousPart[] parts;
	private final HideousPart body1;
	private final HideousPart body2;
	private final HideousPart body3;
	private final HideousPart cap;
	private final HideousPart mask;
	private final HideousPart entrails;
	private final HideousPart tail;
	private final HideousPart right_arm;
	private final HideousPart left_arm;
	private final HideousPart cap_laying;
	private final Vec3d[] partPositions;
	
	HarpoonEntity harpoon;
	DamageSource killingBlow;
	boolean mortarSide;
	
	public HideousMassEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		((LivingEntityAccessor)this).setTakePunchKnockbackSupplier(() -> false); //disable knockback
		((LivingEntityAccessor)this).setCanBleedSupplier(() -> false); //disable auto-bleed
		lookControl = new HideousLookControl(this);
		
		body1 = new HideousPart(this, "body", new Vec2f(3f, 1.75f), false);
		body2 = new HideousPart(this, "body", new Vec2f(2f, 2f), false);
		body3 = new HideousPart(this, "body", new Vec2f(2f, 1.5f), false);
		cap = new HideousPart(this, "cap", new Vec2f(2f, 2f), true);
		mask = new HideousPart(this, "mask", new Vec2f(1.5f, 2.1f), true);
		entrails = new HideousPart(this, "entrails", new Vec2f(1.5f, 1.5f), false);
		tail = new HideousPart(this, "tail", new Vec2f(1.5f, 1.5f), false);
		right_arm = new HideousPart(this, "right_arm", new Vec2f(1.25f, 2.5f), true);
		left_arm = new HideousPart(this, "left_arm", new Vec2f(1.25f, 2.5f), true);
		cap_laying = new HideousPart(this, "cap", new Vec2f(3.5f, 2f), true);
		cap_laying.enabled = false;
		parts = new HideousPart[] {body1, body2, body3, cap, mask, entrails, tail, right_arm, left_arm, cap_laying};
		
		partPositions = partPosDefault.clone();
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 175.0d)
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
		dataTracker.startTracking(HAS_HARPOON, true);
		dataTracker.startTracking(HARPOON_HEALTH, 0f);
		dataTracker.startTracking(DEATH, 0);
		dataTracker.startTracking(HIDDEN, false);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new UnhideGoal(this));
		goalSelector.add(0, new EnrageGoal(this));
		goalSelector.add(1, new StandupGoal(this));
		goalSelector.add(2, new MortarAttackGoal(this));
		goalSelector.add(3, new ClapAttackGoal(this));
		goalSelector.add(4, new HarpoonAttackGoal(this));
		goalSelector.add(5, new SlamAttackGoal(this, true));
		goalSelector.add(5, new SlamAttackGoal(this, false));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	@Override
	protected ServerBossBar initBossBar()
	{
		ServerBossBar bb = super.initBossBar();
		bb.setDarkenSky(true);
		return bb;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "main", this::predicate),
								new AnimationController<>(this, "turn", this::turnPredicate));
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(DEATH) && dataTracker.get(DEATH) >= 100)
		{
			if(getWorld().isClient)
			{
				for (int i = 0; i < 64; i++)
				{
					Vec3d pos = getBoundingBox().getCenter().addRandom(random, 4);
					Vec3d vel = Vec3d.ZERO.addRandom(random, 1.3f);
					getWorld().addParticle(new GoopDropParticleEffect(new Vec3d(0.56, 0.09, 0.01), 0.75f, true, WaterHandling.REPLACE_WITH_CLOUD_PARTICLE),
							pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
				}
			}
			onDeath(killingBlow == null ? DamageSources.get(getWorld(), DamageTypes.GENERIC_KILL) : killingBlow);
			setHealth(0f);
		}
		if(data.equals(BOSS))
		{
			float health = isBoss() ? 175f : 75f;
			if(getHealth() > health)
				setHealth(health);
		}
		if(data.equals(LAYING))
		{
			if(dataTracker.get(LAYING))
			{
				mask.enabled = entrails.enabled = left_arm.enabled = right_arm.enabled = cap.enabled = false;
				cap_laying.enabled = true;
			}
			else
			{
				mask.enabled = entrails.enabled = left_arm.enabled = right_arm.enabled = cap.enabled = true;
				cap_laying.enabled = false;
			}
		}
		if(data.equals(ENRAGED))
		{
			left_arm.enabled = right_arm.enabled = mask.enabled = body3.enabled = false;
		}
		if(data.equals(HIDDEN) && !dataTracker.get(HIDDEN))
			setAllMainPartsEnabled(true);
	}
	
	private PlayState predicate(AnimationState<GeoAnimatable> event)
	{
		event.getController().setAnimationSpeed((isDying() || isDead()) ? 0f : 1f);
		switch(getAnimation())
		{
			case ANIMATION_IDLE -> {
				if(dataTracker.get(HIDDEN))
					event.setAnimation(HIDE_POSE_ANIM);
				else
					event.setAnimation(dataTracker.get(LAYING) ? LAY_POSE_ANIM : POSE_ANIM);
			}
			case ANIMATION_MORTAR -> event.setAnimation(MORTAR_ANIM);
			case ANIMATION_SLAM_STANDING -> event.setAnimation(SLAM_STANDING_ANIM);
			case ANIMATION_SLAM_LAYING -> event.setAnimation(SLAM_LAYING_ANIM);
			case ANIMATION_STAND_UP -> event.setAnimation(STAND_UP_ANIM);
			case ANIMATION_CLAP -> event.setAnimation(CLAP_ANIM);
			case ANIMATION_HARPOON -> event.setAnimation(HARPOON_ANIM);
			case ANIMATION_ENRAGED -> event.setAnimation(ENRAGED_ANIM);
			case ANIMATION_UNHIDE -> event.setAnimation(UNHIDE_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	private PlayState turnPredicate(AnimationState<GeoAnimatable> event)
	{
		event.getController().setAnimationSpeed((isDying() || isDead()) ? 0f : 1f);
		if(prevYaw != getYaw())
		{
			if(dataTracker.get(LAYING))
				event.setAnimation(TURN_LAYING_ANIM);
			else
				event.setAnimation(isEnraged() ? ENRAGED_TURN_ANIM : TURN_ANIM);
		}
		else
			return PlayState.STOP;
		return PlayState.CONTINUE;
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
		if(nbt.contains("hidden", NbtElement.BYTE_TYPE))
		{
			boolean b = nbt.getBoolean("hidden");
			dataTracker.set(HIDDEN, b);
			if(b)
				setAllMainPartsEnabled(false);
		}
		if(nbt.contains("laying", NbtElement.BYTE_TYPE))
			dataTracker.set(LAYING, nbt.getBoolean("laying"));
		if(nbt.contains("enraged", NbtElement.BYTE_TYPE))
			dataTracker.set(ENRAGED, nbt.getBoolean("enraged"));
		if(!nbt.contains("boss", NbtElement.BYTE_TYPE))
			dataTracker.set(BOSS, true);
	}
	
	void setAllMainPartsEnabled(boolean b)
	{
		body1.enabled = body2.enabled = body3.enabled = cap.enabled = mask.enabled = b;
		entrails.enabled = tail.enabled = right_arm.enabled = left_arm.enabled = b;
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("hidden", dataTracker.get(HIDDEN));
		nbt.putBoolean("laying", dataTracker.get(LAYING));
		nbt.putBoolean("enraged", dataTracker.get(ENRAGED));
		nbt.putBoolean("boss", dataTracker.get(BOSS));
	}
	
	@Override
	protected boolean getBossDefault()
	{
		return true;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(dataTracker.get(ATTACK_COOLDOWN) > 0)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
		
		if(!(isDying() || isDead()) && isEnraged() && getTarget() != null && getCooldown() <= 0) //1 in 5 per second
		{
			if(mortarSide)
				fireMortar(new Vec3d(2f, 0f, 0f));
			else
				fireMortar(new Vec3d(-2f, 0f, 0f));
			mortarSide = !mortarSide;
			setCooldown(15 + random.nextInt(15));
		}
		
		if(isDying())
		{
			dataTracker.set(DEATH, dataTracker.get(DEATH) + 1);
			if(age % 10 == 0)
			{
				Vec3d pos = getBoundingBox().getCenter().addRandom(random, 4);
				for (int i = 0; i < 8; i++)
				{
					Vec3d vel = Vec3d.ZERO.addRandom(random, 1.3f);
					getWorld().addParticle(new GoopDropParticleEffect(new Vec3d(0.56, 0.09, 0.01), 1f, true, WaterHandling.REPLACE_WITH_CLOUD_PARTICLE),
							pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
				}
			}
		}
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		for (HideousPart part : parts)
		{
			part.prevX = part.getX();
			part.prevY = part.getY();
			part.prevZ = part.getZ();
			part.lastRenderX = part.getX();
			part.lastRenderY = part.getY();
			part.lastRenderZ = part.getZ();
		}
		Vec3d[] dest;
		if(getAnimation() == ANIMATION_MORTAR)
			dest = partPosMortar;
		else if(dataTracker.get(LAYING))
			dest = partPosLaying;
		else if(dataTracker.get(ENRAGED))
			dest = partPosEnraged;
		else
			dest = partPosDefault;
		for (int i = 0; i < partPositions.length; i++)
		{
			partPositions[i] = partPositions[i].lerp(dest[i], 1f / 5f);
			positionPart(parts[i], partPositions[i]);
		}
		
		setBodyYaw(headYaw);
	}
	
	void positionPart(HideousPart part, Vec3d relative)
	{
		Vec3d pos = getPos().add(relative.rotateY(MathHelper.RADIANS_PER_DEGREE * -bodyYaw));
		part.setPosition(pos.x, pos.y, pos.z);
		part.tick();
	}
	
	@Override
	public boolean canHit()
	{
		return false;
	}
	
	@Override
	public boolean canBeHitByProjectile()
	{
		return false;
	}
	
	public boolean damagePart(HideousPart part, DamageSource source, float amount)
	{
		if(part.name.equals("entrails") || part.name.equals("tail"))
			amount *= 3f;
		if(!getWorld().isClient)
			((LivingEntityAccessor)this).bleed(part.getPos(), part.getHeight() / 2, source, amount);
		return damage(source, amount);
	}
	
	public HideousPart[] getParts()
	{
		return parts;
	}
	
	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet)
	{
		super.onSpawnPacket(packet);
		for (int i = 0; i < parts.length; i++)
			parts[i].setId(packet.getId() + i + 1);
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
	public boolean isFireImmune()
	{
		return true;
	}
	
	@Override
	public void takeKnockback(double strength, double x, double z) {
	}
	
	@Override
	public Vec3d getLeashPos(float delta)
	{
		return tail.getPos();
	}
	
	public void fireMortar(Vec3d offset)
	{
		if(getTarget() == null)
			return;
		HideousMortarEntity.spawn(getWorld(), new Vec3d(getX(), getBoundingBox().getMax(Direction.Axis.Y), getZ()).add(offset.rotateY(getYaw())),
				this, getTarget());
	}
	
	private void shockwave()
	{
		ShockwaveEntity shockwave = new ShockwaveEntity(EntityRegistry.SHOCKWAVE, getWorld());
		shockwave.setDamage(3f);
		shockwave.setGrowRate(0.6f);
		shockwave.setAffectOnly(PlayerEntity.class);
		shockwave.setPosition(getPos().add(0f, 0.5f, 0f));
		getWorld().spawnEntity(shockwave);
		playSound(SoundRegistry.HIDEOUS_MASS_IMPACT, 1f, 1f);
	}
	
	private void clap()
	{
		VerticalShockwaveEntity shockwave = new VerticalShockwaveEntity(EntityRegistry.VERICAL_SHOCKWAVE, getWorld());
		shockwave.setDamage(2f);
		shockwave.setYaw(getYaw());
		shockwave.setGrowRate(0.6f);
		shockwave.setAffectOnly(PlayerEntity.class);
		shockwave.setPosition(getPos().add(0f, 0.5f, 0f));
		getWorld().spawnEntity(shockwave);
		playSound(SoundRegistry.HIDEOUS_MASS_IMPACT, 1f, 1f);
	}
	
	private void shootHarpoon()
	{
		if(getTarget() == null)
			return;
		HarpoonEntity harpoon = HarpoonEntity.spawn(this, getLeashPos(0f), new Vec3d(0f, 0f, 0f));
		Entity target = getTarget();
		double targetY = target.getEyeY();
		double x = target.getX() - getX();
		double y = targetY - harpoon.getY();
		double z = target.getZ() - getZ();
		harpoon.setVelocity(x, y, z, 4f, 0.0f);
		harpoon.setNoGravity(true);
		harpoon.setYaw(-getYaw());
		playSound(SoundRegistry.REPULSIVE_SKEWER_SHOOT, 2.0f, 0.4f / (getRandom().nextFloat() * 0.4f + 0.8f));
		getWorld().spawnEntity(harpoon);
		this.harpoon = harpoon;
		setHasHarpoon(false);
		dataTracker.set(HARPOON_HEALTH, 20f);
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
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(isDying() || isDead())
			return false;
		if(source.getAttacker() instanceof HideousMassEntity || source.getSource() instanceof HideousMassEntity)
			return false;
		if(getHealth() - amount <= 0f)
		{
			dataTracker.set(DEATH, 1);
			killingBlow = source;
			if(harpoon != null)
			{
				harpoon.setReturning(true);
				harpoon = null;
			}
			setHealth(1f);
			playSound(SoundRegistry.HIDEOUS_MASS_DEATH, 1f, 1f);
			return true;
		}
		boolean b = super.damage(source, amount);
		if(b && harpoon != null)
		{
			dataTracker.set(HARPOON_HEALTH, dataTracker.get(HARPOON_HEALTH) - amount);
			if(dataTracker.get(HARPOON_HEALTH) <= 0)
			{
				harpoon.setReturning(true);
				harpoon = null;
			}
		}
		return b;
	}
	
	public boolean isHasHarpoon()
	{
		return dataTracker.get(HAS_HARPOON);
	}
	
	public void setHasHarpoon(boolean b)
	{
		dataTracker.set(HAS_HARPOON, b);
		dataTracker.set(ATTACK_COOLDOWN, 10 + random.nextInt(5));
	}
	
	@Override
	public boolean isEnraged()
	{
		return dataTracker.get(ENRAGED);
	}
	
	public boolean isDying()
	{
		return dataTracker.get(DEATH) > 0 && dataTracker.get(DEATH) < 101;
	}
	
	public boolean isHidden()
	{
		return dataTracker.get(HIDDEN) && getAnimation() != ANIMATION_UNHIDE;
	}
	
	@Override
	public Vec3d getEnrageFeatureSize()
	{
		return new Vec3d(7f, -7f, -7f);
	}
	
	@Override
	public Vec3d getEnragedFeatureOffset()
	{
		return new Vec3d(0, -0.5f, 0);
	}
	
	@Override
	protected double getTeleportParticleSize()
	{
		return 5f;
	}
	
	@Override
	public boolean isBossBarVisible()
	{
		return !dataTracker.get(HIDDEN);
	}
	
	@Override
	public boolean cannotDespawn()
	{
		return isBoss();
	}
	
	@Override
	public void handleStatus(byte status)
	{
		super.handleStatus(status);
		if(status == EntityStatuses.PLAY_ATTACK_SOUND)
		{
			Vec3d pos = tail.getPos().add(getRotationVector().multiply(2f));
			getWorld().addParticle(new ParryIndicatorParticleEffect(false), pos.x, pos.y, pos.z, 0, 0, 0);
		}
	}
	
	@Override
	public void remove(RemovalReason reason)
	{
		super.remove(reason);
		for (HideousPart part : parts)
			part.remove(reason);
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
			byte anim = entity.getDataTracker().get(ANIMATION);
			if(entity.getTarget() != null && (anim == ANIMATION_IDLE || anim == ANIMATION_ENRAGED) &&
					   !(entity.isDead() || ((HideousMassEntity)entity).isDying()))
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
			super(mass, ANIMATION_IDLE, ANIMATION_MORTAR, 55);
		}
		
		@Override
		public boolean canStart()
		{
			if(mob.isDying() || mob.isDead())
				return false;
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
			super(mass, ANIMATION_IDLE, standing ? ANIMATION_SLAM_STANDING : ANIMATION_SLAM_LAYING, 34);
			this.standing = standing;
		}
		
		@Override
		public boolean canStart()
		{
			if(mob.isDying() || mob.isDead())
				return false;
			return super.canStart() && ((standing && !mob.dataTracker.get(LAYING) && mob.dataTracker.get(MORTAR_COUNTER) > 0 && mob.random.nextFloat() > 0.5f) ||
												(!standing && mob.dataTracker.get(LAYING)));
		}
		
		@Override
		public void start()
		{
			super.start();
			mob.playSound(SoundRegistry.HIDEOUS_MASS_SLAM_TELL, 1f, 1f);
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
			super(mass, ANIMATION_IDLE, ANIMATION_CLAP, 39);
		}
		
		@Override
		public boolean canStart()
		{
			if(mob.isDying() || mob.isDead())
				return false;
			return super.canStart() && mob.dataTracker.get(LAYING) && mob.dataTracker.get(SLAM_COUNTER) > 0 && mob.random.nextFloat() < 0.3f;
		}
		
		@Override
		public void start()
		{
			super.start();
			mob.playSound(SoundRegistry.HIDEOUS_MASS_CLAP_TELL, 1f, 1f);
		}
		
		@Override
		protected void process()
		{
			super.process();
			if(timer == 30)
				mob.clap();
		}
	}
	
	static class HarpoonAttackGoal extends TimedAttackGoal<HideousMassEntity>
	{
		public HarpoonAttackGoal(HideousMassEntity mass)
		{
			super(mass, ANIMATION_IDLE, ANIMATION_HARPOON, 20);
		}
		
		@Override
		public boolean canStart()
		{
			if(mob.isDying() || mob.isDead())
				return false;
			return super.canStart() && mob.isHasHarpoon() && mob.dataTracker.get(LAYING) && mob.dataTracker.get(SLAM_COUNTER) > 0 && mob.random.nextFloat() < 0.5f;
		}
		
		@Override
		public void start()
		{
			super.start();
			mob.playSound(SoundRegistry.HIDEOUS_MASS_HARPOON_TELL, 1f, 1f);
		}
		
		@Override
		protected void process()
		{
			super.process();
			if(timer == 7)
				mob.getWorld().sendEntityStatus(mob, EntityStatuses.PLAY_ATTACK_SOUND);
			if(timer == 14)
				mob.shootHarpoon();
		}
	}
	
	static class StandupGoal extends TimedAttackGoal<HideousMassEntity>
	{
		public StandupGoal(HideousMassEntity mass)
		{
			super(mass, ANIMATION_IDLE, ANIMATION_STAND_UP, 13);
		}
		
		@Override
		public boolean canStart()
		{
			if(mob.isDying() || mob.isDead())
				return false;
			return super.canStart() && mob.getHealth() < 50f || (mob.isHasHarpoon() && mob.dataTracker.get(LAYING) && mob.dataTracker.get(SLAM_COUNTER) > 2);
		}
		
		@Override
		public void stop()
		{
			super.stop();
			mob.dataTracker.set(LAYING, false);
		}
	}
	
	static class EnrageGoal extends Goal
	{
		HideousMassEntity mob;
		
		public EnrageGoal(HideousMassEntity mob)
		{
			super();
			this.mob = mob;
		}
		
		@Override
		public boolean canStart()
		{
			if(mob.isDying() || mob.isDead())
				return false;
			if(mob.getCooldown() > 0 || mob.getAnimation() != ANIMATION_IDLE)
				return false;
			return !mob.dataTracker.get(LAYING) && !mob.isEnraged() && mob.getHealth() < 50f;
		}
		
		@Override
		public void start()
		{
			super.start();
			mob.dataTracker.set(ENRAGED, true);
			mob.setAnimation(ANIMATION_ENRAGED);
			mob.playSound(SoundRegistry.GENERIC_ENRAGE, 1.5f, 1f);
		}
	}
	
	static class UnhideGoal extends TimedAttackGoal<HideousMassEntity>
	{
		HideousMassEntity mob;
		
		public UnhideGoal(HideousMassEntity mob)
		{
			super(mob, ANIMATION_IDLE, ANIMATION_UNHIDE, 45);
			this.mob = mob;
		}
		
		@Override
		public boolean canStart()
		{
			if(mob.isDying() || mob.isDead())
				return false;
			if(mob.getAnimation() != ANIMATION_IDLE)
				return false;
			List<PlayerEntity> nearby = mob.getWorld().getPlayers(TargetPredicate.DEFAULT.setPredicate(e -> e.distanceTo(mob) < 24f), mob,
					mob.getBoundingBox().expand(64));
			return mob.getTarget() != null && mob.isHidden() && nearby.size() > 0;
		}
		
		@Override
		public void start()
		{
			super.start();
			mob.playSound(SoundRegistry.HIDEOUS_MASS_UNHIDE, 1f, 1f);
		}
		
		@Override
		public void stop()
		{
			super.stop();
			mob.dataTracker.set(HIDDEN, false);
		}
	}
	
	static {
		partPosDefault = new Vec3d[] {
				new Vec3d(0, 3.5, 1.7), new Vec3d(0, 2, 1.3), new Vec3d(0, 0.75, -0.5),
				new Vec3d(0, 4.5, 2), new Vec3d(0, 3.25, 2.9), new Vec3d(0, 2, 2.2),
				new Vec3d(0, 0.5, -2.5),  new Vec3d(-2, 2.3, 2.35), new Vec3d(2, 2.3, 2.35),
				new Vec3d(0, 4.5, 2)
		};
		partPosMortar = new Vec3d[] {
				new Vec3d(0, 2.25, 1.7), new Vec3d(0, 1.5, 1.3), new Vec3d(0, 0.75, -0.5),
				new Vec3d(0, 3.5, 2), new Vec3d(0, 2.25, 2.9), new Vec3d(0, 1, 2.2),
				new Vec3d(0, 0.5, -2.5),  new Vec3d(-2, 1.5, 2.35), new Vec3d(2, 1.5, 2.35),
				new Vec3d(0, 3, 2)
		};
		partPosLaying = new Vec3d[] {
				new Vec3d(0, 0.5, 1.7), new Vec3d(0, 2.5, -1), new Vec3d(0, 1.75, 0.5),
				new Vec3d(0, 0.5, 4), new Vec3d(0, 3.25, 2.9), new Vec3d(0, 2, 2.2),
				new Vec3d(0, 4.75, -1),  new Vec3d(-2, 2.3, 2.35), new Vec3d(2, 2.3, 2.35),
				new Vec3d(0, 0.5, 4)
		};
		partPosEnraged = new Vec3d[] {
				new Vec3d(0, 0.4, 1.5), new Vec3d(0, 0.1, -1), new Vec3d(0, 1.75, 0.5),
				new Vec3d(0, 0.5, 2.5), new Vec3d(0, 3.25, 2.9), new Vec3d(0, 0.9, 1.3),
				new Vec3d(0, 2.75, -2.75),  new Vec3d(-2, 2.3, 2.35), new Vec3d(2, 2.3, 2.35),
				new Vec3d(0, 0.5, 4)
		};
	}
}
