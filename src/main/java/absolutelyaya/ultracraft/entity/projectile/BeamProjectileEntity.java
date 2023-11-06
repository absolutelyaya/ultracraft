package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.client.ClientHitscanHandler;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BeamProjectileEntity extends ProjectileEntity
{
	public static final TrackedData<Byte> HITSCAN_TYPE = DataTracker.registerData(BeamProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
	Vec3d startPos = Vec3d.ZERO;
	float damage;
	
	public BeamProjectileEntity(EntityType<? extends ProjectileEntity> entityType, World world)
	{
		super(entityType, world);
		((ProjectileEntityAccessor)this).setOnParried(i -> world.sendEntityStatus(this, (byte)4));
	}
	
	@Override
	protected void initDataTracker()
	{
		dataTracker.startTracking(HITSCAN_TYPE, (byte)0);
	}
	
	public static BeamProjectileEntity spawn(World world, LivingEntity owner, float vel, byte hitscanType)
	{
		BeamProjectileEntity beam = new BeamProjectileEntity(EntityRegistry.BEAM, world);
		beam.setOwner(owner);
		beam.setNoGravity(true);
		beam.setPosition(owner.getEyePos());
		beam.setVelocity(owner.getRotationVector().multiply(vel));
		beam.setVelocity(owner, owner.getPitch(), owner.getYaw(), 0, 8f, 0);
		beam.startPos = beam.getPos();
		beam.setHitscanType(hitscanType);
		world.spawnEntity(beam);
		return beam;
	}
	
	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet)
	{
		super.onSpawnPacket(packet);
		startPos = getPos();
		ClientHitscanHandler.Hitscan.HitscanType type = ClientHitscanHandler.Hitscan.HitscanType.values()[dataTracker.get(HITSCAN_TYPE)];
		UltracraftClient.HITSCAN_HANDLER.addConnector(f -> getStartPos().equals(Vec3d.ZERO) ? getLerpedPos(f) : getStartPos(), this::getLerpedPos, getUuid(),
				new Vec2f(type.startGirth, 0f), 0f, type.color, 3);
	}
	
	@Override
	public void tick()
	{
		if(startPos.distanceTo(Vec3d.ZERO) < 0.1f)
			startPos = getPos();
		super.tick();
		HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
		checkBlockCollision();
		updateRotation();
		if (hitResult.getType() != HitResult.Type.MISS)
		{
			setPosition(hitResult.getPos());
			onCollision(hitResult);
		}
		else
			setPosition(getPos().add(getVelocity()));
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		if (!getWorld().isClient && !isRemoved())
		{
			ServerHitscanHandler.sendPacket((ServerWorld)getWorld(), hitResult.getPos(), getStartPos(), dataTracker.get(HITSCAN_TYPE));
			discard();
		}
		super.onCollision(hitResult);
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		super.onEntityHit(entityHitResult);
		entityHitResult.getEntity().damage(DamageSources.get(getWorld(), DamageSources.GUN, getOwner()), damage);
	}
	
	public Vec3d getStartPos()
	{
		return startPos;
	}
	
	@Override
	public void handleStatus(byte status)
	{
		if(status == 4)
		{
			startPos = getPos();
			return;
		}
		super.handleStatus(status);
	}
	
	void setHitscanType(byte b)
	{
		dataTracker.set(HITSCAN_TYPE, b);
	}
	
	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if(nbt.contains("type", NbtElement.BYTE_TYPE))
			setHitscanType(nbt.getByte("type"));
	}
	
	@Override
	public void onRemoved()
	{
		UltracraftClient.HITSCAN_HANDLER.removeMoving(getUuid());
		super.onRemoved();
	}
	
	public void setDamage(float damage)
	{
		this.damage = damage;
	}
}
