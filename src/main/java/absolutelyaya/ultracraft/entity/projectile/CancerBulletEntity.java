package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class CancerBulletEntity extends HellBulletEntity implements ProjectileEntityAccessor
{
	LivingEntity target;
	
	public CancerBulletEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	protected CancerBulletEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.CANCER_BULLET, owner, world);
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		super.onCollision(hitResult);
		ExplosionHandler.explosion(null, getWorld(), hitResult.getPos(), DamageSources.get(getWorld(), DamageSources.CANCER, this, getOwner()),
				8f, 4f, 4f, true);
	}
	
	public static CancerBulletEntity spawn(LivingEntity owner, World world, LivingEntity target)
	{
		CancerBulletEntity bullet = new CancerBulletEntity(owner, world);
		bullet.target = target;
		return bullet;
	}
	
	@Override
	protected Item getDefaultItem()
	{
		return ItemRegistry.CANCER_BULLET;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(target != null && target.isAlive())
			setVelocity(getVelocity().lerp(target.getEyePos().subtract(getPos()).normalize(), 0.05f));
		else if(age % 10 == 0)
		{
			getWorld().getOtherEntities(owner, getBoundingBox().expand(8), i -> i instanceof LivingEntity)
					.forEach(i -> {
						if(target != null)
							return;
						if(i instanceof LivingEntity living)
							target = living;
					});
		}
	}
}
