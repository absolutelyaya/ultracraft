package absolutelyaya.ultracraft.entity.other;

import absolutelyaya.ultracraft.damage.DamageSources;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.World;

public class ShockwaveEntity extends Entity
{
	private static final TrackedData<Float> RADIUS = DataTracker.registerData(ShockwaveEntity.class, TrackedDataHandlerRegistry.FLOAT);
	int duration = 100;
	float damage = 2f, velocity = 1f;
	Class<?> ignored;
	Class<?> affectOnly;
	Entity owner;
	
	public ShockwaveEntity(EntityType<?> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	protected void initDataTracker()
	{
		dataTracker.startTracking(RADIUS, 0.5f);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		if (RADIUS.equals(data))
			this.calculateDimensions();
		super.onTrackedDataSet(data);
	}
	
	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		if(nbt.contains("duration"))
			setDuration(nbt.getInt("duration"));
	}
	
	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		nbt.putInt("duration", getDuration());
	}
	
	@Override
	public void tick()
	{
		if (age > duration)
			remove(RemovalReason.DISCARDED);
		setRadius(getRadius() + 0.25f);
		
		if(affectOnly != null)
		{
			world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), getBoundingBox(), this::shouldDamage).forEach(e -> {
				e.damage(DamageSources.getShockwave(owner), damage);
				e.setVelocity(0f, velocity, 0f);
			});
		}
		else
		{
			world.getOtherEntities(this, getBoundingBox(), this::shouldDamage).forEach(e -> {
				e.damage(DamageSources.getShockwave(owner), damage);
				e.setVelocity(0f, velocity, 0f);
			});
		}
	}
	
	boolean shouldDamage(Entity entity)
	{
		return entity.isAlive() && distanceTo(entity) > getRadius() - 1f && !entity.getClass().equals(ignored);
	}
	
	@Override
	public void calculateDimensions() {
		double d = this.getX();
		double e = this.getY();
		double f = this.getZ();
		super.calculateDimensions();
		this.setPosition(d, e, f);
	}
	
	public int getDuration() {
		return this.duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public void setRadius(float radius)
	{
		dataTracker.set(RADIUS, radius);
	}
	
	public float getRadius()
	{
		return dataTracker.get(RADIUS);
	}
	
	public void setDamage(float damage)
	{
		this.damage = damage;
	}
	
	public void setThrowVelocity(float velocity)
	{
		this.velocity = velocity;
	}
	
	public void setOwner(Entity entity)
	{
		owner = entity;
	}
	
	@Override
	public EntityDimensions getDimensions(EntityPose pose)
	{
		return EntityDimensions.changing(getRadius() * 2.0f, 1f);
	}
	
	public float getOpagueness()
	{
		if (age > getDuration() - 20)
			return 1f - (age - (getDuration() - 20)) / 20f;
		else
			return 1f;
	}
	
	public void setIgnored(Class<?> ignored)
	{
		this.ignored = ignored;
	}
	
	public void setAffectOnly(Class<?> affect)
	{
		this.affectOnly = affect;
	}
}
