package absolutelyaya.ultracraft.entity.demon;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.MeleeInterruptable;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.entity.AbstractUltraFlyingEntity;
import absolutelyaya.ultracraft.entity.other.ShockwaveEntity;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import absolutelyaya.ultracraft.particle.goop.GoopStringParticleEffect;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;

public class MaliciousFaceEntity extends AbstractUltraFlyingEntity implements MeleeInterruptable, Enrageable
{
	protected static final TrackedData<Integer> ATTACK_COOLDOWN = DataTracker.registerData(MaliciousFaceEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Boolean> CRACKED = DataTracker.registerData(MaliciousFaceEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> DEAD = DataTracker.registerData(MaliciousFaceEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> LANDED = DataTracker.registerData(MaliciousFaceEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> WAS_INTERRUPTED = DataTracker.registerData(MaliciousFaceEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> CHARGE = DataTracker.registerData(MaliciousFaceEntity.class, TrackedDataHandlerRegistry.INTEGER);
	static final float DESIRED_HEIGHT = 3;
	Vec2f deathRotation;
	int deathTicks;
	
	public MaliciousFaceEntity(EntityType<? extends AbstractUltraFlyingEntity> entityType, World world)
	{
		super(entityType, world);
		this.moveControl = new MaliciousMoveControl(this);
		((LivingEntityAccessor)this).SetTakePunchKnockbackSupplier(() -> false); //disable knockback
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64)
					   .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new MaliciousBeamGoal(this));
		goalSelector.add(1, new MaliciousSalvaeGoal(this));
		goalSelector.add(2, new SpreadOutGoal(this));
		goalSelector.add(3, new HoverIntoSightGoal(this));
		goalSelector.add(4, new GainHeightGoal(this));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, 4, false, false, (a) -> true));
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ATTACK_COOLDOWN, 100);
		dataTracker.startTracking(CRACKED, false);
		dataTracker.startTracking(DEAD, false);
		dataTracker.startTracking(LANDED, false);
		dataTracker.startTracking(WAS_INTERRUPTED, false);
		dataTracker.startTracking(CHARGE, 0);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if (data.equals(CRACKED) && dataTracker.get(CRACKED))
		{
			for (int i = 0; i < 16; i++)
			{
				EntityDimensions dimensions = getDimensions(getPose());
				double x = random.nextDouble() * dimensions.width - dimensions.width / 2 + getX();
				double y = random.nextDouble() * dimensions.height - dimensions.height / 2 + getY();
				double z = random.nextDouble() * dimensions.width - dimensions.width / 2 + getZ();
				world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.CHISELED_STONE_BRICKS.getDefaultState()),
						x, y, z, 0f, 0f, 0f);
			}
			if(world.getDifficulty().equals(Difficulty.HARD) || world.getGameRules().getBoolean(GameruleRegistry.EFFECTIVELY_VIOLENT))
				playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 1.5f, 0.9f);
		}
		else if(data.equals(LANDED) && dataTracker.get(LANDED))
		{
			for (int i = 0; i < 32; i++)
			{
				float x = (float)((random.nextDouble() * 16) - 8 + getX());
				float z = (float)((random.nextDouble() * 16) - 8 + getZ());
				world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK,
						world.getBlockState(new BlockPos((int)getX(), (int)getY() - 2, (int)getZ()))),
						x, getY(), z, 0f, 0f, 0f);
			}
		}
		else if (data.equals(DEAD) && dataTracker.get(DEAD))
		{
			deathRotation = new Vec2f(getPitch(), getYaw());
			((LivingEntityAccessor)this).SetCanBleedSupplier(() -> false); //disable bleeding
		}
	}
	
	@Override
	public boolean isCollidable()
	{
		return true;
	}
	
	@Override
	protected void pushOutOfBlocks(double x, double y, double z)
	{
		if(!dataTracker.get(DEAD))
			super.pushOutOfBlocks(x, y, z);
	}
	
	@Override
	protected double getTeleportParticleSize()
	{
		return 1.5;
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("cracked", dataTracker.get(CRACKED));
		nbt.putBoolean("dead", dataTracker.get(DEAD));
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		if(nbt.contains("cracked"))
			dataTracker.set(CRACKED, nbt.getBoolean("cracked"));
		if(nbt.contains("dead"))
			dataTracker.set(DEAD, nbt.getBoolean("dead"));
	}
	
	@Override
	public boolean isFireImmune()
	{
		return true;
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(getTarget() != null && !isAttacking())
		{
			if(dataTracker.get(DEAD))
			{
				setPitch(deathRotation.x);
				setYaw(deathRotation.y);
			}
			else
			{
				lookAtEntity(getTarget(), 180, 180);
				setBodyYaw(headYaw);
				double e = getTarget().getX() - getX();
				double f = getTarget().getZ() - getZ();
				setYaw(-((float)MathHelper.atan2(e, f)) * 57.295776f);
			}
			bodyYaw = getYaw();
			headYaw = getYaw();
		}
		if(dataTracker.get(ATTACK_COOLDOWN) > 0)
			dataTracker.set(ATTACK_COOLDOWN, dataTracker.get(ATTACK_COOLDOWN) - 1);
		if(dataTracker.get(DEAD) && !dataTracker.get(LANDED))
		{
			Vec3d vel = getVelocity();
			double q = vel.y;
			if (hasStatusEffect(StatusEffects.LEVITATION))
			{
				StatusEffectInstance levitation = getStatusEffect(StatusEffects.LEVITATION);
				if(levitation != null)
					q += (0.05 * (double)(levitation.getAmplifier() + 1) - vel.y) * 0.2;
			}
			else if (!hasNoGravity())
				q -= 0.08;
			this.setVelocity(0f, q * 0.98f, 0f);
		}
		if(dataTracker.get(CHARGE) > 0 && age % 4 == 0)
		{
			Vec3d particlePos = getPos().add(getRotationVector().multiply(1f));
			Vec3d offset = new Vec3d(random.nextDouble() * 1.6, random.nextDouble() * 1.6, random.nextDouble() * 1.6);
			offset = offset.multiply(random.nextBoolean() ? 1f : -1f);
			world.addParticle(ParticleRegistry.MALICIOUS_CHARGE, particlePos.x + offset.x, particlePos.y + offset.y, particlePos.z + offset.z,
					-offset.x * 0.04, -offset.y * 0.04, -offset.z * 0.04);
		}
		if(dataTracker.get(CRACKED))
		{
			java.util.Random rand = new java.util.Random();
			EntityDimensions dimensions = getDimensions(getPose());
			double x = random.nextDouble() * dimensions.width - dimensions.width / 2 + getX();
			double y = random.nextDouble() * dimensions.height + getY();
			double z = random.nextDouble() * dimensions.width - dimensions.width / 2 + getZ();
			if(rand.nextFloat() > 0.5f + getHealth() / getMaxHealth())
				world.addParticle(new GoopStringParticleEffect(new Vec3d(0.56, 0.09, 0.01),
								0.4f + rand.nextFloat() * 0.2f), x, y, z,
						0f, 0f, 0f);
		}
		//TODO: add tendrils
	}
	
	@Override
	public void move(MovementType movementType, Vec3d movement)
	{
		super.move(movementType, movement);
		if(movement.lengthSquared() > 0.02)
		{
			List<Entity> list = world.getOtherEntities(this, getBoundingBox().expand(0f, 0.1, 0f), e -> !(e instanceof MaliciousFaceEntity));
			list.forEach(e -> e.move(MovementType.SHULKER, movement));
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(dataTracker.get(DEAD))
		{
			deathTicks++;
			if(deathTicks > 200)
				kill();
		}
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(source.isIn(DamageTypeTags.IS_EXPLOSION))
			return false;
		if(source.isOf(DamageSources.POUND))
			amount *= 2;
		if(dataTracker.get(DEAD))
		{
			if(source.isOf(DamageTypes.STARVE)) //starve because there's no way this damage would occur accidentally
				setHealth(0);
			if(source.isOf(DamageSources.POUND))
			{
				setHealth(0);
				for (int i = 0; i < 32; i++)
				{
					double x = random.nextDouble() - 0.5 + getX();
					double y = random.nextDouble() - 0.5 + getY() + 1f;
					double z = random.nextDouble() - 0.5 + getZ();
					world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.CHISELED_STONE_BRICKS.getDefaultState()),
							x, y, z, 0f, 0f, 0f);
				}
			}
			return false;
		}
		if(source.isOf(DamageTypes.FALL))
			return false;
		if(getHealth() - amount <= 0f && !dataTracker.get(DEAD))
		{
			dataTracker.set(DEAD, true);
			setNoGravity(false);
			setHealth(1);
			setInvulnerable(true);
			addVelocity(0f, 0.1f, 0f);
			return false;
		}
		if(getHealth() - amount < getMaxHealth() / 2 && !dataTracker.get(CRACKED))
			dataTracker.set(CRACKED, true);
		return super.damage(source, amount);
	}
	
	@Override
	public boolean isImmuneToExplosion()
	{
		return true;
	}
	
	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition)
	{
		if(dataTracker.get(DEAD) && onGround)
		{
			if(!world.isClient)
			{
				ShockwaveEntity shockwave = EntityRegistry.SHOCKWAVE.spawn((ServerWorld)world, getBlockPos(), SpawnReason.EVENT);
				if(shockwave != null)
				{
					shockwave.setIgnored(getClass());
					shockwave.setDamage(0f);
				}
			}
			List<Entity> entities = world.getOtherEntities(this, getBoundingBox(), Entity::isLiving);
			for (Entity e : entities)
				e.damage(DamageSources.get(world, DamageSources.MAURICE), 999f);
			dataTracker.set(LANDED, true);
			setPosition(getPos().subtract(0f, 0.5f, 0f));
		}
	}
	
	@Override
	public void kill()
	{
		damage(DamageSources.get(world, DamageTypes.STARVE), Float.MAX_VALUE);
	}
	
	@Override
	protected void updatePostDeath()
	{
		if(!world.isClient && !isRemoved())
		{
			world.sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
			remove(RemovalReason.KILLED);
		}
	}
	
	@Override
	public float getEyeHeight(EntityPose pose)
	{
		return (float)((getBoundingBox().minY + getBoundingBox().maxX) / 2.0);
	}
	
	@Override
	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions)
	{
		return dimensions.height / 2;
	}
	
	@Override
	public boolean isPushable()
	{
		return false;
	}
	
	public boolean isEnraged()
	{
		if(world.isClient)
			return isCracked() && (world.getDifficulty().equals(Difficulty.HARD) || UltracraftClient.isViolentFeaturesEnabled(world));
		return isCracked() && (world.getDifficulty().equals(Difficulty.HARD) || world.getGameRules().getBoolean(GameruleRegistry.EFFECTIVELY_VIOLENT));
	}
	
	@Override
	public Vec3d getEnrageFeatureSize()
	{
		return new Vec3d(2.75f, -2.75f, -2.75f);
	}
	
	@Override
	public Vec3d getEnragedFeatureOffset()
	{
		return new Vec3d(0f, -0.5f, 0f);
	}
	
	public void shootBullet(LivingEntity target)
	{
		HellBulletEntity bullet = HellBulletEntity.spawn(this, this.world);
		double d = target.getEyeY() - 0f;
		double e = target.getX() - getX();
		double f = d - bullet.getY();
		double g = target.getZ() - getZ();
		bullet.setVelocity(e, f, g, 1f, 0.0f);
		bullet.setNoGravity(true);
		this.playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0f, 0.4f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
		this.world.spawnEntity(bullet);
	}
	
	@Override
	protected SoundEvent getAmbientSound()
	{
		return null;
	}
	
	@Override
	protected SoundEvent getDeathSound()
	{
		return null;
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource source)
	{
		return null;
	}
	
	public boolean isCracked()
	{
		return dataTracker.get(CRACKED);
	}
	
	public float getChargePercent()
	{
		return MathHelper.clamp(dataTracker.get(CHARGE) / 80f, 0f, 1f);
	}
	
	private float getHealthPercent()
	{
		return getHealth() / getMaxHealth();
	}
	
	@Override
	public void onInterrupt(PlayerEntity parrier)
	{
		if(!world.isClient && getServer() != null)
			getServer().execute(() -> ExplosionHandler.explosion(parrier, world, new Vec3d(getX(), getY(), getZ()),
					getDamageSources().explosion(parrier, parrier), 10f, 0f, 5.5f, true));
		damage(getDamageSources().mobAttack(parrier), 10);
		dataTracker.set(WAS_INTERRUPTED, true);
	}
	
	public float getDistanceToGround()
	{
		BlockHitResult hit = world.raycast(new RaycastContext(getPos(), getPos().add(0, -25, 0),
				RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
		return getBlockY() - (float)hit.getPos().y;
	}
	
	@Override
	public boolean isAlive()
	{
		return !dataTracker.get(DEAD);
	}
	
	static class MaliciousMoveControl extends MoveControl
	{
		private final MaliciousFaceEntity face;
		final Random rand;
		boolean strafeDir;
		int tickSinceStrafeDirSwitch;
		
		public MaliciousMoveControl(MaliciousFaceEntity face)
		{
			super(face);
			this.face = face;
			rand = this.face.getRandom();
			strafeDir = rand.nextBoolean();
		}
		
		public void stopMoving()
		{
			if(!face.getDataTracker().get(DEAD))
				face.setVelocity(0f, 0f, 0f);
			state = State.WAIT;
		}
		
		public void tick()
		{
			if(face.getDataTracker().get(DEAD))
				return;
			if (state == State.MOVE_TO)
			{
				if(face.squaredDistanceTo(targetX, targetY, targetZ) < 0.75f)
					stopMoving();
				Vec3d forward = new Vec3d(targetX - face.getX(), targetY - face.getY(), targetZ - face.getZ());
				forward = forward.normalize();
				if (willCollide(forward))
				{
					Vec3d right = forward.crossProduct(new Vec3d(0, 1, 0));
					face.setVelocity(face.getVelocity().add(right.multiply(0.15 * speed * (strafeDir ? 1 : -1))));
					if((tickSinceStrafeDirSwitch++ > 120 && rand.nextInt(32) == 0) || willCollide(right.multiply(strafeDir ? 1 : -1)))
					{
						strafeDir = rand.nextBoolean();
						tickSinceStrafeDirSwitch = 0;
					}
				}
				else
					face.setVelocity(face.getVelocity().add(forward.multiply(0.1 * speed)));
				
				float upward = face.getDistanceToGround() < DESIRED_HEIGHT ? (float)speed * 0.1f : 0f;
				if(face.getDistanceToGround() > DESIRED_HEIGHT + 0.25)
					upward = (float)speed * -0.1f;
				face.setVelocity(face.getVelocity().add(0f, upward, 0f));
			}
		}
		
		private boolean willCollide(Vec3d direction)
		{
			Box box = face.getBoundingBox().offset(direction.multiply(0.5));
			return !face.world.isSpaceEmpty(face, box);
		}
	}
	
	static class GainHeightGoal extends Goal
	{
		private final MaliciousFaceEntity face;
		
		public GainHeightGoal(MaliciousFaceEntity face)
		{
			this.face = face;
			this.setControls(EnumSet.of(Control.MOVE));
		}
		
		@Override
		public boolean canStart()
		{
			return face.getDistanceToGround() < DESIRED_HEIGHT;
		}
		
		public boolean shouldContinue() {
			return false;
		}
		
		public void start()
		{
			double y = face.world.getTopY(Heightmap.Type.MOTION_BLOCKING, face.getBlockX(), face.getBlockZ()) + DESIRED_HEIGHT + 1.5;
			if(!face.getMoveControl().isMoving())
				face.getMoveControl().moveTo(face.getX(), y, face.getZ(), 0.05);
		}
	}
	
	static class HoverIntoSightGoal extends Goal
	{
		private final MaliciousFaceEntity face;
		LivingEntity target;
		
		public HoverIntoSightGoal(MaliciousFaceEntity face)
		{
			this.face = face;
			this.setControls(EnumSet.of(Control.MOVE));
		}
		
		public boolean canStart()
		{
			if(face.getTarget() == null || face.canSee(face.getTarget()))
				return false;
			target = face.getTarget();
			return true;
		}
		
		public boolean shouldContinue() {
			return face.getTarget() != null && !face.canSee(face.getTarget());
		}
		
		public void start()
		{
			Vec3d invDir = face.getPos().subtract(target.getPos()).rotateY((float) (face.random.nextDouble() * Math.toRadians(360)))
								   .multiply(1f, 0f, 1f).normalize().multiply(4f);
			
			double x = target.getX() + invDir.x;
			double z = target.getZ() + invDir.z;
			double y = face.world.getTopY(Heightmap.Type.MOTION_BLOCKING, target.getBlockX(), target.getBlockZ()) + DESIRED_HEIGHT;
			if(!face.getMoveControl().isMoving())
				face.getMoveControl().moveTo(x, y, z, 0.05);
		}
		
		@Override
		public void stop()
		{
			((MaliciousMoveControl)face.moveControl).stopMoving();
		}
	}
	
	static class SpreadOutGoal extends Goal
	{
		private final MaliciousFaceEntity face;
		MaliciousFaceEntity closestOther;
		
		public SpreadOutGoal(MaliciousFaceEntity face)
		{
			this.face = face;
			this.setControls(EnumSet.of(Control.MOVE));
		}
		
		public boolean canStart()
		{
			if(face.moveControl.isMoving() || face.random.nextDouble() < 0.9)
				return false;
			List<MaliciousFaceEntity> nearby = face.world.getEntitiesByType(TypeFilter.instanceOf(MaliciousFaceEntity.class),
					face.getBoundingBox().expand(5f), LivingEntity::isAlive);
			float closestDistance = Float.MAX_VALUE;
			for (MaliciousFaceEntity e : nearby)
			{
				float dist = (float)e.getPos().squaredDistanceTo(face.getPos());
				if (dist < closestDistance)
				{
					closestDistance = dist;
					closestOther = e;
				}
			}
			return closestOther != null;
		}
		
		public boolean shouldContinue() {
			return closestOther != null && (float)closestOther.getPos().squaredDistanceTo(face.getPos()) < 5 * 5;
		}
		
		public void start()
		{
			Vec3d invDir = face.getPos().subtract(closestOther.getPos()).rotateY((float) (face.random.nextDouble() * Math.toRadians(360)))
								   .multiply(1f, 0f, 1f).normalize().multiply(6f);
			
			double x = closestOther.getX() + invDir.x;
			double z = closestOther.getZ() + invDir.z;
			double y = face.world.getTopY(Heightmap.Type.MOTION_BLOCKING, closestOther.getBlockX(), closestOther.getBlockZ()) + DESIRED_HEIGHT;
			if(!face.getMoveControl().isMoving())
				face.getMoveControl().moveTo(x, y, z, 0.05);
		}
		
		@Override
		public void stop()
		{
			((MaliciousMoveControl)face.moveControl).stopMoving();
		}
	}
	
	static class MaliciousSalvaeGoal extends Goal
	{
		final MaliciousFaceEntity face;
		LivingEntity target;
		int timer, shots;
		
		MaliciousSalvaeGoal(MaliciousFaceEntity face)
		{
			this.face = face;
		}
		
		@Override
		public boolean canStart()
		{
			target = face.getTarget();
			return target != null && face.canSee(target) && face.dataTracker.get(ATTACK_COOLDOWN) <= 0 && !face.dataTracker.get(DEAD)
						   && face.dataTracker.get(CHARGE) <= 0;
		}
		
		@Override
		public void start()
		{
			shots = 6;
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public void tick()
		{
			if(timer-- <= 0)
			{
				shots--;
				timer = 2;
				face.shootBullet(target);
				face.dataTracker.set(ATTACK_COOLDOWN, 40 + (int)(face.random.nextFloat() * 60));
			}
		}
		
		@Override
		public boolean shouldContinue()
		{
			return shots > 0 && target != null && !face.dataTracker.get(DEAD);
		}
	}
	
	static class MaliciousBeamGoal extends Goal
	{
		final MaliciousFaceEntity face;
		LivingEntity target;
		int timer;
		Vec3d lastTargetPos, targetPos;
		boolean repeat;
		
		MaliciousBeamGoal(MaliciousFaceEntity face)
		{
			this.face = face;
			setControls(EnumSet.of(Control.LOOK, Control.MOVE));
		}
		
		@Override
		public boolean canStart()
		{
			target = face.getTarget();
			if(target == null || !face.canSee(target) || face.dataTracker.get(ATTACK_COOLDOWN) > 0 || face.dataTracker.get(DEAD))
				return false;
			return face.random.nextFloat() > face.getHealthPercent() - 0.2f;
		}
		
		@Override
		public void start()
		{
			timer = 100;
			face.dataTracker.set(ATTACK_COOLDOWN, 100);
			face.dataTracker.set(WAS_INTERRUPTED, false);
			repeat = face.isEnraged();
		}
		
		@Override
		public boolean shouldRunEveryTick()
		{
			return true;
		}
		
		@Override
		public void tick()
		{
			if(face.isEnraged() && !repeat)
			 	timer--; //double as quick when enraged and first shot was fired
			if(--timer > 20)
			{
				face.dataTracker.set(CHARGE, 100 - timer);
				lastTargetPos = target.getPos();
			}
			if(timer == 20)
			{
				Vec3d dir = target.getPos().subtract(lastTargetPos);
				if(dir.lengthSquared() < 0.01)
					dir = Vec3d.ZERO;
				targetPos = target.getPos().add(dir.normalize().multiply(3)).add(0, target.getHeight() / 4, 0);
				face.setAttacking(true);
				face.addParryIndicatorParticle(face.getRotationVector().multiply(1.5f), false, false);
				face.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 2f, 1.75f);
			}
			if(timer < 20)
			{
				if(targetPos != null)
					face.lookControl.lookAt(targetPos);
				face.dataTracker.set(CHARGE, 100 - timer);
			}
			if(timer <= 0)
			{
				ServerHitscanHandler.performHitscan(face, (byte)5, 0, new ServerHitscanHandler.HitscanExplosionData(5.5f, 10f, 0f, true));
				if(repeat)
				{
					timer = 22;
					repeat = false;
					return;
				}
				face.dataTracker.set(ATTACK_COOLDOWN, 50 + (int)(face.random.nextFloat() * 60));
				face.dataTracker.set(CHARGE, 0);
			}
		}
		
		@Override
		public void stop()
		{
			super.stop();
			face.setAttacking(false);
			face.dataTracker.set(CHARGE, 0);
			if(face.dataTracker.get(WAS_INTERRUPTED))
			{
				face.dataTracker.set(WAS_INTERRUPTED, false);
				face.dataTracker.set(ATTACK_COOLDOWN, 50 + (int)(face.random.nextFloat() * 60));
			}
		}
		
		@Override
		public boolean shouldContinue()
		{
			return ((timer > 0 && target != null) || repeat) && !face.dataTracker.get(DEAD) && !face.dataTracker.get(WAS_INTERRUPTED);
		}
	}
}
