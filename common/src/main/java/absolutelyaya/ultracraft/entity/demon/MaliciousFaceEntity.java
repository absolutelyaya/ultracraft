package absolutelyaya.ultracraft.entity.demon;

import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;

public class MaliciousFaceEntity extends GhastEntity
{
	protected static final TrackedData<Integer> SALVAE_COOLDOWN = DataTracker.registerData(MaliciousFaceEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Boolean> CRACKED = DataTracker.registerData(MaliciousFaceEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> DEAD = DataTracker.registerData(MaliciousFaceEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> LANDED = DataTracker.registerData(MaliciousFaceEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public MaliciousFaceEntity(EntityType<? extends GhastEntity> entityType, World world)
	{
		super(entityType, world);
		this.moveControl = new MaliciousMoveControl(this);
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0d)
					   .add(EntityAttributes.GENERIC_ARMOR, 6.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64)
					   .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
	}
	
	@Override
	protected void initGoals()
	{
		goalSelector.add(0, new HoverIntoSightGoal(this));
		goalSelector.add(1, new MaliciousSalvaeGoal(this));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, 4, false, false, (a) -> true));
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(SALVAE_COOLDOWN, 200);
		dataTracker.startTracking(CRACKED, false);
		dataTracker.startTracking(DEAD, false);
		dataTracker.startTracking(LANDED, false);
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
		}
		else if(data.equals(LANDED) && dataTracker.get(LANDED))
		{
			for (int i = 0; i < 32; i++)
			{
				float x = (float)((random.nextDouble() * 16) - 8 + getX());
				float z = (float)((random.nextDouble() * 16) - 8 + getZ());
				world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, world.getBlockState(new BlockPos(x, getY() - 2, z))),
						x, getY(), z, 0f, 0f, 0f);
			}
		}
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(getTarget() != null)
		{
			lookAtEntity(getTarget(), 180, 180);
			setBodyYaw(headYaw);
			double e = getTarget().getX() - getX();
			double f = getTarget().getZ() - getZ();
			setYaw(-((float)MathHelper.atan2(e, f)) * 57.295776f);
			bodyYaw = getYaw();
			headYaw = getYaw();
		}
		if(dataTracker.get(SALVAE_COOLDOWN) > 0)
			dataTracker.set(SALVAE_COOLDOWN, dataTracker.get(SALVAE_COOLDOWN) - 1);
		if(dataTracker.get(DEAD))
		{
			Vec3d vel = getVelocity();
			double q = vel.y;
			if (hasStatusEffect(StatusEffects.LEVITATION))
			{
				q += (0.05 * (double)(getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - vel.y) * 0.2;
				onLanding();
			}
			else if (!hasNoGravity())
			{
				q -= 0.08;
			}
			
			this.setVelocity(0f, q * 0.98f, 0f);
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
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
	protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition)
	{
		if(dataTracker.get(DEAD) && onGround)
		{
			List<Entity> entities = world.getOtherEntities(this, getBoundingBox().expand(5f), Entity::isLiving);
			for (Entity e : entities)
				e.addVelocity(0f, e.isOnGround() ? 1f : 0f, 0f);
			entities = world.getOtherEntities(this, getBoundingBox(), Entity::isLiving);
			for (Entity e : entities)
				e.kill();
			dataTracker.set(LANDED, true);
			kill();
		}
	}
	
	@Override
	protected void updatePostDeath()
	{
		if(!world.isClient)
		{
			world.sendEntityStatus(this, (byte)60);
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
	
	static class MaliciousMoveControl extends MoveControl
	{
		private final GhastEntity face;
		Random rand;
		boolean strafeDir;
		int tickSinceStrafeDirSwitch;
		
		public MaliciousMoveControl(GhastEntity ghast)
		{
			super(ghast);
			this.face = ghast;
			rand = face.getRandom();
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
				if(face.squaredDistanceTo(targetX, targetY, targetZ) < 0.5f)
					stopMoving();
				Vec3d forward = new Vec3d(targetX - face.getX(), targetY - face.getY(), targetZ - face.getZ());
				forward = forward.normalize();
				if (willCollide(forward))
				{
					Vec3d right = forward.crossProduct(new Vec3d(0, 1, 0));
					face.setVelocity(face.getVelocity().add(right.multiply(0.1 * speed * (strafeDir ? 1 : -1))));
					if((tickSinceStrafeDirSwitch++ > 120 && rand.nextInt(32) == 0) || willCollide(right.multiply(strafeDir ? 1 : -1)))
					{
						strafeDir = rand.nextBoolean();
						tickSinceStrafeDirSwitch = 0;
					}
				}
				else
					face.setVelocity(face.getVelocity().add(forward.multiply(0.1 * speed)));
			}
		}
		
		private boolean willCollide(Vec3d direction)
		{
			Box box = face.getBoundingBox().offset(direction.multiply(0.5));
			return !face.world.isSpaceEmpty(face, box);
		}
	}
	
	static class HoverIntoSightGoal extends Goal
	{
		private final MaliciousFaceEntity face;
		LivingEntity target;
		
		public HoverIntoSightGoal(MaliciousFaceEntity ghast)
		{
			this.face = ghast;
			this.setControls(EnumSet.of(Control.MOVE));
		}
		
		public boolean canStart()
		{
			MoveControl control = face.moveControl;
			if(face.getTarget() == null || face.canSee(face.getTarget()))
			{
				((MaliciousMoveControl)control).stopMoving();
				return false;
			}
			target = face.getTarget();
			return true;
		}
		
		public boolean shouldContinue() {
			return false;
		}
		
		public void start()
		{
			Vec3d invDir = face.getPos().subtract(target.getPos()).rotateY((float) (face.random.nextDouble() * Math.toRadians(360)))
								   .multiply(1f, 0f, 1f).normalize().multiply(4f);
			
			double x = target.getX() + invDir.x;
			double z = target.getZ() + invDir.z;
			double y = face.world.getTopY(Heightmap.Type.MOTION_BLOCKING, target.getBlockX(), target.getBlockZ()) + 4;
			if(!face.getMoveControl().isMoving())
				face.getMoveControl().moveTo(x, y, z, 0.05);
		}
	}
	
	static class MaliciousSalvaeGoal extends Goal
	{
		MaliciousFaceEntity face;
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
			return target != null && face.canSee(target) && face.dataTracker.get(SALVAE_COOLDOWN) <= 0;
		}
		
		@Override
		public void start()
		{
			shots = 8;
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
				face.dataTracker.set(SALVAE_COOLDOWN, 100 + (int)(face.random.nextFloat() * 60));
			}
		}
		
		@Override
		public boolean shouldContinue()
		{
			return shots > 0 && target != null;
		}
	}
}