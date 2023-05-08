package absolutelyaya.ultracraft.entity.other;

import absolutelyaya.ultracraft.accessor.Interruptable;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.UltraDamageSource;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class InterruptableCharge extends Entity
{
	Interruptable owner;
	protected static final TrackedData<Integer> LIFETIME = DataTracker.registerData(InterruptableCharge.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> OWNER = DataTracker.registerData(InterruptableCharge.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Float> START_SIZE = DataTracker.registerData(InterruptableCharge.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Float> GOAL_SIZE = DataTracker.registerData(InterruptableCharge.class, TrackedDataHandlerRegistry.FLOAT);
	
	public InterruptableCharge(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	public static InterruptableCharge spawn(World world, Interruptable owner, int lifetime, float startSize, float finalSize)
	{
		InterruptableCharge ic = new InterruptableCharge(EntityRegistry.INTERRUPTABLE_CHARGE, world);
		ic.setOwner(owner);
		LivingEntity livingOwner = (LivingEntity)owner;
		Vec3d offset = owner.getChargeOffset();
		ic.setPos(livingOwner.getX() + offset.x, livingOwner.getY() + offset.y, livingOwner.getZ() + offset.z);
		ic.setLifetime(lifetime, startSize, finalSize);
		world.spawnEntity(ic);
		return ic;
	}
	
	@Override
	protected void initDataTracker()
	{
		dataTracker.startTracking(OWNER, -1);
		dataTracker.startTracking(LIFETIME, 10);
		dataTracker.startTracking(START_SIZE, 0.5f);
		dataTracker.startTracking(GOAL_SIZE, 1f);
	}
	
	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {}
	
	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		boolean result = source instanceof UltraDamageSource us && us.isHitscan();
		if(result)
		{
			owner.onInterrupted((PlayerEntity)source.getSource());
			discard();
			return super.damage(source, amount);
		}
		return false;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(world.isClient)
			return;
		LivingEntity livingOwner = (LivingEntity) world.getEntityById(dataTracker.get(OWNER));
		if(livingOwner == null || !livingOwner.isAlive() || age >= dataTracker.get(LIFETIME))
		{
			discard();
			return;
		}
		Vec3d offset = owner.getChargeOffset();
		setPosition(livingOwner.getX() + offset.x, livingOwner.getY() + offset.y, livingOwner.getZ() + offset.z);
	}
	
	public void setOwner(Interruptable owner)
	{
		this.owner = owner;
		dataTracker.set(OWNER, ((LivingEntity)owner).getId());
	}
	
	public void setBounds(Box box)
	{
		setBoundingBox(box);
	}
	
	public void setLifetime(int lifetime, float startSize, float finalSize)
	{
		dataTracker.set(LIFETIME, lifetime);
		dataTracker.set(START_SIZE, startSize);
		dataTracker.set(GOAL_SIZE, finalSize);
	}
	
	public float getScale()
	{
		return MathHelper.lerp((float)age / (float)dataTracker.get(LIFETIME), dataTracker.get(START_SIZE), dataTracker.get(GOAL_SIZE));
	}
	
	@Override
	public boolean shouldSave()
	{
		return false;
	}
	
	@Override
	public boolean isPartOf(Entity entity)
	{
		return entity.equals(this) || entity.equals(owner);
	}
	
	@Override
	public boolean canHit()
	{
		return true;
	}
}
