package absolutelyaya.ultracraft.entity;

import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import absolutelyaya.ultracraft.particle.TeleportParticleEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AbstractUltraHostileEntity extends HostileEntity
{
	protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(AbstractUltraHostileEntity.class, TrackedDataHandlerRegistry.BYTE);
	
	protected AbstractUltraHostileEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	public byte getAnimation()
	{
		return dataTracker.get(ANIMATION);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ANIMATION, (byte)0);
	}
	
	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet)
	{
		super.onSpawnPacket(packet);
		world.addParticle(new TeleportParticleEffect(getTeleportParticleSize()), packet.getX(), packet.getY(), packet.getZ(), 0f, 0f, 0f);
	}
	
	@Override
	public boolean teleport(double x, double y, double z, boolean particleEffects)
	{
		world.addParticle(new TeleportParticleEffect(getTeleportParticleSize()), x, y, z, 0f, 0f, 0f);
		return super.teleport(x, y, z, particleEffects);
	}
	
	protected double getTeleportParticleSize()
	{
		return 1.0;
	}
	
	public void addParryIndicatorParticle(Vec3d offset, boolean useYaw, boolean unparriable)
	{
		if(useYaw)
			offset = offset.rotateY(-(float)Math.toRadians(getYaw() + 180));
		if(!world.isClient)
		{
			((ServerWorld)world).spawnParticles(new ParryIndicatorParticleEffect(unparriable),
					getX() + offset.x, getY() + offset.y, getZ() + offset.z, 1, 0f, 0f, 0f, 0f);
		}
	}
	
	@Override
	protected boolean shouldSwimInFluids()
	{
		return !world.getFluidState(getBlockPos()).isIn(FluidTags.WATER);
	}
	
	@Override
	public int getAir()
	{
		return getMaxAir();
	}
}
