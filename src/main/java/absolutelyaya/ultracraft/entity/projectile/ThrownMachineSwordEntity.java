package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.entity.machine.SwordsmachineEntity;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class 	ThrownMachineSwordEntity extends PersistentProjectileEntity implements ProjectileEntityAccessor
{
	protected static final TrackedData<ItemStack> SWORD = DataTracker.registerData(ThrownMachineSwordEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	protected static final TrackedData<Float> DISTANCE = DataTracker.registerData(ThrownMachineSwordEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Boolean> REACHED_DEST = DataTracker.registerData(ThrownMachineSwordEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> RETURNING = DataTracker.registerData(ThrownMachineSwordEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> STASIS_TICKS = DataTracker.registerData(ThrownMachineSwordEntity.class, TrackedDataHandlerRegistry.INTEGER);
	Vec3d spawnPos = getPos();
	public float lastRot;
	float hitNoisePitch = 0.5f;
	PlayerEntity parrier;
	
	public ThrownMachineSwordEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	private ThrownMachineSwordEntity(World world, LivingEntity owner)
	{
		super(EntityRegistry.THROWN_MACHINE_SWORD, owner, world);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(SWORD, ItemRegistry.MACHINE_SWORD.getDefaultStack());
		dataTracker.startTracking(DISTANCE, 0f);
		dataTracker.startTracking(REACHED_DEST, false);
		dataTracker.startTracking(RETURNING, false);
		dataTracker.startTracking(STASIS_TICKS, 0);
	}
	
	public static ThrownMachineSwordEntity spawn(World world, LivingEntity owner, ItemStack swordStack, float distance)
	{
		ThrownMachineSwordEntity sword = new ThrownMachineSwordEntity(world, owner);
		sword.setSword(swordStack);
		sword.setNoGravity(true);
		sword.dataTracker.set(DISTANCE, distance);
		sword.spawnPos = owner.getPos();
		sword.setRotation(owner.getYaw(), 0f);
		world.spawnEntity(sword);
		return sword;
	}
	
	void setSword(ItemStack stack)
	{
		dataTracker.set(SWORD, stack);
	}
	
	@Override
	public void tick()
	{
		if(world.isClient && age == 1)
			UltracraftClient.TRAIL_RENDERER.createTrail(uuid,
					() -> {
						float deg = (float)Math.toRadians(getYaw() + 90);
						Vector3f left =	getTrailPos(deg, 1.5f);
						Vector3f right = getTrailPos(deg, 1f);
						return new Pair<>(left, right);
					}, new Vector4f(1f, 0.5f, 0f, 0.6f), 30);
		if(world.isClient && Ultracraft.isTimeFrozen())
			return;
		move(MovementType.SELF, getVelocity());
		
		HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
		if (hitResult.getType() != HitResult.Type.MISS)
			onCollision(hitResult);
		checkBlockCollision();
		
		if(isInStasis() && dataTracker.get(STASIS_TICKS) % 5 == 0)
		{
			world.getOtherEntities(this, getBoundingBox().expand(0.5)).forEach(e -> {
				if(!e.equals(getOwner()))
					e.damage(DamageSources.get(world, DamageSources.SWORDSMACHINE, getOwner()), 2);
			});
		}
	}
	
	Vector3f getTrailPos(float deg, float distance)
	{
		//return getPos().add(0f, (distance - 1f) * 2f, 0f).toVector3f();
		float randOffset = (random.nextFloat() - 0.5f) * 0.001f;
		return getPos().toVector3f().add(new Vector3f(randOffset, distance + randOffset, randOffset).rotate(
				new Quaternionf(new AxisAngle4f((float)Math.toRadians(age * 0.936f * 60),
						(float)Math.sin(deg), 0f, (float)Math.cos(deg)))));
	}
	
	@Override
	protected void onBlockHit(BlockHitResult blockHitResult)
	{
		if(!dataTracker.get(RETURNING))
			dataTracker.set(DISTANCE, 0f);
	}
	
	@Override
	public void move(MovementType movementType, Vec3d movement)
	{
		if(getDistance() > 0f && !isParried())
		{
			setPosition(getPos().add(movement));
			dataTracker.set(DISTANCE, getDistance() - (float)movement.length());
		}
		else if(!dataTracker.get(REACHED_DEST))
			 dataTracker.set(REACHED_DEST, true);
		dataTracker.set(STASIS_TICKS, dataTracker.get(STASIS_TICKS) + 1);
		if(dataTracker.get(REACHED_DEST) && dataTracker.get(STASIS_TICKS) >= 100 || isParried())
		{
			if(!dataTracker.get(RETURNING))
				dataTracker.set(RETURNING, true);
			Vec3d dest;
			if(isOwnerAlive())
				dest = getOwner().getPos().add(0f, getOwner().getHeight() / 2f, 0f);
			else
				dest = spawnPos;
			Vec3d vel = dest.subtract(getPos()).normalize();
			setYaw(MathHelper.lerpAngleDegrees(0.5f, getYaw(), -(float)Math.toDegrees(Math.atan2(vel.z, vel.x)) - 90));
			setPosition(getPos().add(vel));
			if(getPos().distanceTo(dest) > 1f)
				return;
			if(isOwnerAlive())
			{
				if(getOwner() instanceof SwordsmachineEntity sm)
				{
					sm.setHasSword(true);
					if(isParried())
					{
						sm.onInterrupt(parrier);
						sm.damage(DamageSources.get(world, DamageSources.PARRY, parrier), 30);
					}
				}
				if(getOwner() instanceof PlayerEntity p && tryPickup(p))
				{
					if(!p.giveItemStack(dataTracker.get(SWORD)))
						dropStack(asItemStack());
					if(isParried())
						p.damage(DamageSources.get(world, DamageSources.PARRY, parrier), 12);
				}
				discard();
			}
			else
			{
				dropStack(asItemStack());
				discard();
			}
		}
	}
	
	@Override
	public void slowMovement(BlockState state, Vec3d multiplier)
	{
	
	}
	
	float getDistance()
	{
		return dataTracker.get(DISTANCE);
	}
	
	boolean isInStasis()
	{
		return dataTracker.get(REACHED_DEST) && dataTracker.get(STASIS_TICKS) < 100 && !isParried() && !dataTracker.get(RETURNING);
	}
	
	@Override
	protected boolean tryPickup(PlayerEntity player)
	{
		return dataTracker.get(RETURNING) && isOwner(player) && !player.isCreative();
	}
	
	boolean isOwnerAlive()
	{
		return getOwner() != null && getOwner().isAlive();
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		Entity hit = entityHitResult.getEntity();
		if(hit instanceof PlayerEntity p)
			onPlayerCollision(p);
		if(!isOwner(hit) && !isInStasis())
		{
			if(hit.damage(DamageSources.get(world, DamageSources.SWORDSMACHINE, getOwner()), 6) && getOwner() instanceof PlayerEntity playerOwner)
				playerOwner.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.5f, hitNoisePitch += 0.05);
		}
	}
	
	@Override
	protected ItemStack asItemStack()
	{
		return dataTracker.get(SWORD).copy();
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putDouble("spawnX", spawnPos.x);
		nbt.putDouble("spawnY", spawnPos.y);
		nbt.putDouble("spawnZ", spawnPos.z);
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if(nbt.contains("spawnX", NbtElement.DOUBLE_TYPE) && nbt.contains("spawnY", NbtElement.DOUBLE_TYPE) && nbt.contains("spawnZ", NbtElement.DOUBLE_TYPE))
			spawnPos = new Vec3d(nbt.getDouble("spawnX"), nbt.getDouble("spawnY"), nbt.getDouble("spawnZ"));
	}
	
	@Override
	protected SoundEvent getHitSound()
	{
		return SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP;
	}
	
	public ItemStack getStack()
	{
		return dataTracker.get(SWORD);
	}
	
	@Override
	public void setParried(boolean val, PlayerEntity parrier)
	{
		if(parrier != getOwner())
		{
			dataTracker.set(RETURNING, true);
			if(age > 4)
				this.parrier = parrier;
		}
	}
	
	@Override
	public boolean isParried()
	{
		return parrier != null;
	}
	
	@Override
	public boolean isParriable()
	{
		return !dataTracker.get(REACHED_DEST);
	}
	
	@Override
	public boolean isBoostable()
	{
		return switch(world.getGameRules().get(GameruleRegistry.PROJ_BOOST).get())
		{
			case ALLOW_ALL -> true;
			case ENTITY_TAG -> getType().isIn(EntityRegistry.PROJBOOSTABLE);
			case LIMITED -> (Object) this instanceof ShotgunPelletEntity;
			case DISALLOW -> false;
		} && age < 4;
	}
	
	@Override
	public void onParriedCollision(HitResult hitResult)
	{
		//since this entity is discarded upon reaching it's owner, the actual parry collision behavior is up in the move method
	}
	
	@Override
	public boolean isHitscanHittable()
	{
		return false;
	}
	
	@Override
	public void onRemoved()
	{
		super.onRemoved();
		if(world.isClient)
			UltracraftClient.TRAIL_RENDERER.removeTrail(uuid);
	}
	
	@Override
	public PlayerEntity getParrier()
	{
		return parrier;
	}
	
	@Override
	public void setParrier(PlayerEntity parrier)
	{
		this.parrier = parrier;
	}
}
