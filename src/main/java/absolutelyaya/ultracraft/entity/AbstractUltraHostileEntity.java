package absolutelyaya.ultracraft.entity;

import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
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
	public boolean damage(DamageSource source, float amount)
	{
		boolean b = super.damage(source, amount);
		timeUntilRegen = 9;
		return b;
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
}
