package absolutelyaya.ultracraft.entity.other;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.EntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BackTank extends Entity
{
	LivingEntity owner;
	protected static final TrackedData<Integer> OWNER = DataTracker.registerData(BackTank.class, TrackedDataHandlerRegistry.INTEGER);
	
	public BackTank(EntityType<?> type, World world)
	{
		super(type, world);
		((EntityAccessor)this).setTargetpriorityFunction(e -> 4);
		((EntityAccessor)this).setTargettableSupplier(() -> true);
	}
	
	@Override
	protected void initDataTracker()
	{
		dataTracker.startTracking(OWNER, -1);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(OWNER))
		{
			Entity e = getWorld().getEntityById(dataTracker.get(OWNER));
			if(e instanceof LivingEntity living)
			{
				owner = living;
				if(living instanceof WingedPlayerEntity winged)
					winged.setBackTank(this);
			}
		}
	}
	
	public static BackTank spawn(World world, LivingEntity owner)
	{
		BackTank tank = new BackTank(EntityRegistry.BACK_TANK, world);
		tank.setOwner(owner);
		tank.positionSelf(owner);
		world.spawnEntity(tank);
		return tank;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(owner != null && owner.isAlive())
			positionSelf(owner);
		else if(!isRemoved())
			kill();
		if(owner instanceof PlayerEntity player && !player.getMainHandStack().isOf(ItemRegistry.FLAMETHROWER))
		{
			kill();
			((WingedPlayerEntity)player).setBackTank(null);
		}
	}
	
	public void positionSelf(LivingEntity owner)
	{
		prevX = getX();
		prevY = getY();
		prevZ = getZ();
		lastRenderX = getX();
		lastRenderY = getY();
		lastRenderZ = getZ();
		Vec3d pos = owner.getBoundingBox().getCenter().subtract(Vec3d.fromPolar(0f, owner.getBodyYaw()).multiply(0.3f));
		setPos(pos.x, pos.y + 0.3, pos.z);
		setBoundingBox(calculateBoundingBox());
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(source.isIn(DamageTypeTags.HITSCAN))
		{
			owner.damage(DamageSources.get(getWorld(), DamageSources.BACK_TANK), 999);
			ExplosionHandler.explosion(owner, getWorld(), getPos(), DamageSources.get(getWorld(), DamageTypes.EXPLOSION, this, source.getAttacker()),
					8, 5, 3f, true);
			if(source.getAttacker() instanceof ServerPlayerEntity player)
				Ultracraft.freeze(player, 6);
			kill();
			return true;
		}
		return false;
	}
	
	public void setOwner(LivingEntity owner)
	{
		this.owner = owner;
		dataTracker.set(OWNER, owner.getId());
	}
	
	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
	
	}
	
	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
	
	}
	
	@Override
	public boolean isPartOf(Entity entity)
	{
		return entity.equals(this) || entity.equals(owner);
	}
	
	@Override
	public boolean canHit()
	{
		return false;
	}
}
