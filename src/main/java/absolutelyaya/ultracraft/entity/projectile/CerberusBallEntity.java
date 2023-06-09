package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class CerberusBallEntity extends HellBulletEntity implements ProjectileEntityAccessor
{
	public CerberusBallEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	protected CerberusBallEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.CERBERUS_BALL, owner, world);
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		super.onCollision(hitResult);
		ExplosionHandler.explosion(null, world, hitResult.getPos(), getDamageSources().explosion(this, getOwner()), 8f, 4f, 2f, true);
	}
	
	public static CerberusBallEntity spawn(LivingEntity owner, World world)
	{
		return new CerberusBallEntity(owner, world);
	}
	
	@Override
	protected Item getDefaultItem()
	{
		return ItemRegistry.CERBERUS_BALL;
	}
}
