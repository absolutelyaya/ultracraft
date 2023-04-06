package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.entity.machine.SwordmachineEntity;
import absolutelyaya.ultracraft.registry.DamageSources;
import absolutelyaya.ultracraft.registry.EntityRegistry;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ThrownMachineSwordEntity extends PersistentProjectileEntity implements ProjectileEntityAccessor
{
	private ItemStack swordStack = new ItemStack(ItemRegistry.MACHINE_SWORD);
	protected static final TrackedData<Float> DISTANCE = DataTracker.registerData(ThrownMachineSwordEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Boolean> REACHED_DEST = DataTracker.registerData(ThrownMachineSwordEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> RETURNING = DataTracker.registerData(ThrownMachineSwordEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> STASIS_TICKS = DataTracker.registerData(ThrownMachineSwordEntity.class, TrackedDataHandlerRegistry.INTEGER);
	Vec3d spawnPos = getPos();
	public float lastRot;
	float hitNoisePitch = 0.5f;
	
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
		dataTracker.startTracking(DISTANCE, 0f);
		dataTracker.startTracking(REACHED_DEST, false);
		dataTracker.startTracking(RETURNING, false);
		dataTracker.startTracking(STASIS_TICKS, 0);
	}
	
	public static ThrownMachineSwordEntity spawn(World world, LivingEntity owner, ItemStack swordStack, float distance)
	{
		ThrownMachineSwordEntity sword = new ThrownMachineSwordEntity(world, owner);
		sword.swordStack = swordStack;
		sword.setNoGravity(true);
		sword.dataTracker.set(DISTANCE, distance);
		sword.spawnPos = owner.getPos();
		sword.setRotation(owner.getYaw(), 0f);
		world.spawnEntity(sword);
		return sword;
	}
	
	@Override
	public void tick()
	{
		if(world.isClient && Ultracraft.isTimeFrozen())
			return;
		move(MovementType.SELF, getVelocity());
		
		HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
		if (hitResult.getType() != HitResult.Type.MISS)
			onCollision(hitResult);
		checkBlockCollision();
	}
	
	@Override
	protected void onBlockCollision(BlockState state)
	{
		if(!state.isAir() && !dataTracker.get(RETURNING))
			dataTracker.set(DISTANCE, 0f);
	}
	
	@Override
	protected void onBlockHit(BlockHitResult blockHitResult)
	{
	
	}
	
	@Override
	public void move(MovementType movementType, Vec3d movement)
	{
		if(getDistance() > 0f)
		{
			setPosition(getPos().add(movement));
			dataTracker.set(DISTANCE, getDistance() - (float)movement.length());
		}
		else if(!dataTracker.get(REACHED_DEST))
			 dataTracker.set(REACHED_DEST, true);
		dataTracker.set(STASIS_TICKS, dataTracker.get(STASIS_TICKS) + 1);
		if(dataTracker.get(REACHED_DEST) && dataTracker.get(STASIS_TICKS) >= 100)
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
				if(getOwner() instanceof SwordmachineEntity sm)
					sm.setHasSword(true);
				if(getOwner() instanceof PlayerEntity p && tryPickup(p))
					p.giveItemStack(swordStack);
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
		if(!isOwner(hit))
		{
			if(hit.damage(DamageSources.getSwordmachine(getOwner()), 6) && getOwner() instanceof PlayerEntity playerOwner)
				playerOwner.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.5f, hitNoisePitch += 0.05);
		}
	}
	
	@Override
	protected ItemStack asItemStack()
	{
		return swordStack.copy();
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
		return swordStack;
	}
	
	@Override
	public void setParried(boolean val, PlayerEntity parrier)
	{
	
	}
	
	@Override
	public boolean isParried()
	{
		return false;
	}
	
	@Override
	public boolean isParriable()
	{
		return false;
	}
}
