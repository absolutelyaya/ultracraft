package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class NailEntity extends ProjectileEntity implements ProjectileEntityAccessor
{
	public NailEntity(EntityType<? extends ProjectileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	protected void initDataTracker()
	{
	
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(isRemoved() || (getWorld().isClient && Ultracraft.isTimeFrozen()))
			return;
		HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
		
		if (hitResult.getType() != HitResult.Type.MISS)
			onCollision(hitResult);
		checkBlockCollision();
		Vec3d vel = getVelocity();
		double x = getX() + vel.x;
		double y = getY() + vel.y;
		double z = getZ() + vel.z;
		setVelocity(vel.x, vel.y - 0.03f, vel.z);
		updateRotation();
		setPosition(x, y, z);
		ProjectileUtil.setRotationFromVelocity(this, 1f);
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		Entity entity = entityHitResult.getEntity();
		if(isOwner(entity))
			return;
		super.onEntityHit(entityHitResult);
		float amount = 0.2f;
		//if(entity instanceof AbstractUltraHostileEntity)
		//	amount *= 0.6f;
		entity.damage(DamageSources.get(getWorld(), DamageSources.NAIL, getOwner(), this), amount);
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		if (!getWorld().isClient && !isRemoved())
		{
			if(hitResult instanceof BlockHitResult bHit && getVelocity().length() > 0.5f)
			{
				Vector3f dir = bHit.getSide().getUnitVector();
				Vec3d vel = getVelocity();
				setVelocity(vel.multiply(dir.x == 0 ? 0.35f : -0.35f, dir.y == 0 ? 0.4f : -0.4f, dir.z == 0 ? 0.35f : -0.35f));
				return;
			}
			if(hitResult instanceof EntityHitResult eHit && isOwner(eHit.getEntity()))
				return;
			getWorld().sendEntityStatus(this, (byte)3);
			discard();
		}
		super.onCollision(hitResult);
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
	
	@Override
	public void onParriedCollision(HitResult hitResult)
	{
	
	}
	
	@Override
	public boolean isHitscanHittable(byte type)
	{
		return false;
	}
	
	@Override
	public boolean isBoostable()
	{
		return false;
	}
	
	@Override
	public PlayerEntity getParrier()
	{
		return null;
	}
	
	@Override
	public void setParrier(PlayerEntity p)
	{
	
	}
	
	@Override
	public boolean shouldRender(double distance)
	{
		return true;
	}
}
