package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.other.StainedGlassWindow;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
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

public class NailEntity extends ProjectileEntity implements ProjectileEntityAccessor, IIgnoreSharpshooter
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
		float amount = 0.4f;
		entity.damage(DamageSources.get(getWorld(), DamageSources.NAIL, getOwner(), this),
				amount * getWorld().getGameRules().getInt(GameruleRegistry.NAILGUN_DAMAGE));
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		if (!isRemoved())
		{
			if(hitResult instanceof BlockHitResult bHit && getVelocity().length() > 0.33f)
			{
				Vector3f dir = bHit.getSide().getUnitVector();
				Vec3d vel = getVelocity();
				setVelocity(vel.multiply(dir.x == 0 ? 0.15f : -0.15f, dir.y == 0 ? 0.2f : -0.2f, dir.z == 0 ? 0.15f : -0.15f));
				return;
			}
			if(hitResult instanceof EntityHitResult eHit &&
					   (isOwner(eHit.getEntity()) || (eHit.getEntity() instanceof StainedGlassWindow window && window.isReinforced())))
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
