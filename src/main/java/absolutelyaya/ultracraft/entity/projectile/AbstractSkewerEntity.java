package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AbstractSkewerEntity extends PersistentProjectileEntity
{
	protected static final TrackedData<Float> GROUND_TIME = DataTracker.registerData(AbstractSkewerEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Float> IMPACT_YAW = DataTracker.registerData(AbstractSkewerEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Float> IMPACT_PITCH = DataTracker.registerData(AbstractSkewerEntity.class, TrackedDataHandlerRegistry.FLOAT);
	
	protected LivingEntity victim;
	protected int unmovingTicks;
	
	protected AbstractSkewerEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(GROUND_TIME, 0f);
		dataTracker.startTracking(IMPACT_YAW, 0f);
		dataTracker.startTracking(IMPACT_PITCH, 0f);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(dataTracker.get(GROUND_TIME) > 240)
			despawn();
		if(getVelocity().equals(Vec3d.ZERO) && !inGround && victim == null)
			unmovingTicks++;
		else if(unmovingTicks > 0)
			unmovingTicks = 0;
		if(unmovingTicks > 20)
			despawn();
		if(victim != null)
		{
			if(!victim.isAlive())
			{
				victim = null;
				return;
			}
			setVelocity(Vec3d.ZERO);
			setPosition(victim.getPos().add(0f, victim.getHeight() / 2, 0f));
			prevPitch = getPitch();
			prevYaw = getYaw();
			setYaw(dataTracker.get(IMPACT_YAW));
			setPitch(dataTracker.get(IMPACT_PITCH));
		}
		if(isRemoved() || !isInGround())
			return;
		dataTracker.set(GROUND_TIME, dataTracker.get(GROUND_TIME) + 1f);
	}
	
	protected void despawn()
	{
		getWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
		if(!getWorld().isClient())
			discard();
	}
	
	public boolean isInGround()
	{
		return inGround || victim != null;
	}
	
	public LivingEntity getVictim()
	{
		return victim;
	}
	
	@Override
	public void handleStatus(byte status) {
		if (status != EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES)
			return;
		for (int i = 0; i < 16; i++)
		{
			Vec3d pos = getPos().addRandom(random, 0.1f);
			Vec3d vel = Vec3d.ZERO.addRandom(random, 0.25f);
			getWorld().addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, asItemStack()), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
		}
		//TODO: break sound
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		if(victim != null)
			return;
		if(entityHitResult.getEntity() instanceof LivingEntity living)
		{
			victim = living;
			dataTracker.set(IMPACT_YAW, getYaw());
			dataTracker.set(IMPACT_PITCH, getPitch());
			living.damage(DamageSources.get(getWorld(), DamageSources.MAGNET, this, getOwner()), 3.5f);
			if(this instanceof ProjectileEntityAccessor proj && proj.isParried())
				proj.onParriedCollision(entityHitResult);
		}
	}
}
