package absolutelyaya.ultracraft.entity.other;

import absolutelyaya.ultracraft.damage.DamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ShockwaveEntity extends Entity
{
	private static final TrackedData<Float> RADIUS = DataTracker.registerData(ShockwaveEntity.class, TrackedDataHandlerRegistry.FLOAT);
	int duration = 100;
	float damage = 2f, velocity = 1f, growRate = 0.25f;
	Class<?> ignored;
	Class<?> affectOnly;
	Entity owner;
	final List<Entity> hits = new ArrayList<>();
	
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
		if(nbt.contains("growRate"))
			setGrowRate(nbt.getFloat("growRate"));
		if(nbt.contains("damage"))
			setDamage(nbt.getFloat("damage"));
		if(nbt.contains("velocity"))
			setThrowVelocity(nbt.getFloat("velocity"));
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
			discard();
		setRadius(getRadius() + growRate);
		
		if(affectOnly != null)
		{
			getWorld().getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), getBoundingBox(), this::shouldDamage).forEach(e -> {
				e.damage(DamageSources.get(getWorld(), DamageSources.SHOCKWAVE, owner), damage);
				e.setVelocity(0f, velocity, 0f);
				hits.add(e);
			});
		}
		else
		{
			getWorld().getOtherEntities(this, getBoundingBox(), this::shouldDamage).forEach(e -> {
				e.damage(DamageSources.get(getWorld(), DamageSources.SHOCKWAVE, owner), damage);
				e.setVelocity(0f, velocity, 0f);
				hits.add(e);
			});
		}
	}
	
	boolean shouldDamage(Entity entity)
	{
		float dist = distanceTo(entity);
		return entity.isAlive() && dist < getRadius() + 1f && dist > getRadius() - 3f && !entity.getClass().equals(ignored) && !hits.contains(entity);
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
	
	/**
	 * default: 100
	 */
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
	
	/**
	 * default: 2
	 */
	public void setDamage(float damage)
	{
		this.damage = damage;
	}
	
	/**
	 * default: 1
	 */
	public void setThrowVelocity(float velocity)
	{
		this.velocity = velocity;
	}
	
	public void setOwner(Entity entity)
	{
		owner = entity;
	}
	
	/**
	 * default: 0.25
	 */
	public void setGrowRate(float growRate)
	{
		this.growRate = growRate;
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
