package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class HarpoonEntity extends PersistentProjectileEntity
{
	protected static final TrackedData<Vector3f> START_POSITION = DataTracker.registerData(HarpoonEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	protected static final TrackedData<Integer> GROUND_TICKS = DataTracker.registerData(HarpoonEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Boolean> RETURNING = DataTracker.registerData(HarpoonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public HarpoonEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(START_POSITION, new Vector3f());
		dataTracker.startTracking(GROUND_TICKS, 0);
		dataTracker.startTracking(RETURNING, false);
	}
	
	public static HarpoonEntity spawn(LivingEntity owner, Vec3d pos, Vec3d vel)
	{
		HarpoonEntity harpoon = new HarpoonEntity(EntityRegistry.HARPOON, owner.getWorld());
		harpoon.setOwner(owner);
		harpoon.setPosition(pos);
		harpoon.dataTracker.set(START_POSITION, pos.toVector3f());
		harpoon.setVelocity(vel);
		owner.getWorld().spawnEntity(harpoon);
		return harpoon;
	}
	
	@Override
	protected ItemStack asItemStack()
	{
		return ItemRegistry.HARPOON.getDefaultStack();
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(getOwner() != null && !getOwner().isPlayer() && age > 600)
			despawn();
		if(inGround)
			dataTracker.set(GROUND_TICKS, dataTracker.get(GROUND_TICKS) + 1);
		if(dataTracker.get(GROUND_TICKS) > 20 && getOwner() != null && !getOwner().isPlayer())
			setReturning(true);
		if(dataTracker.get(RETURNING))
		{
			Vec3d start = new Vec3d(getStartPosition());
			Vec3d dir = getPos().subtract(start).normalize();
			setPosition(getPos().add(dir.multiply(-Math.min(1.6f, getPos().distanceTo(start)))));
			if(start.distanceTo(getPos()) < 0.1f)
			{
				if(getOwner() != null && getOwner().isPlayer())
					((PlayerEntity)getOwner()).giveItemStack(ItemRegistry.HARPOON.getDefaultStack());
				else
					despawn();
			}
		}
	}
	
	void despawn()
	{
		getWorld().addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, asItemStack()),
				getX(), getY(), getZ(), 0f, 0f, 0f);
		if(!getWorld().isClient())
			discard();
	}
	
	@Override
	protected SoundEvent getHitSound()
	{
		return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
	}
	
	public Vector3f getStartPosition()
	{
		return dataTracker.get(START_POSITION);
	}
	
	@Override
	public boolean canHit()
	{
		return super.canHit() && !dataTracker.get(RETURNING);
	}
	
	public void setReturning(boolean b)
	{
		dataTracker.set(RETURNING, b);
	}
}
