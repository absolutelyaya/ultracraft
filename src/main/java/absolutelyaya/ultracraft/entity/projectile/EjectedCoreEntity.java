package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EjectedCoreEntity extends ThrownItemEntity implements ProjectileEntityAccessor
{
	public EjectedCoreEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	private EjectedCoreEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.EJECTED_CORE, owner, world);
	}
	
	public static EjectedCoreEntity spawn(LivingEntity owner, World world)
	{
		return new EjectedCoreEntity(owner, world);
	}
	
	@Override
	protected Item getDefaultItem()
	{
		return ItemRegistry.EJECTED_CORE;
	}
	
	@Override
	public boolean hasNoGravity()
	{
		return false;
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		if(hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof MaliciousFaceEntity)
		{
			Vec3d newVel = getVelocity().multiply(-0.5f, 1f, -0.5f);
			setVelocity(newVel);
			world.playSound(null, new BlockPos(entityHit.getPos()), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 1f, 1.75f);
			return;
		}
		super.onCollision(hitResult);
		
		if (!world.isClient)
		{
			world.createExplosion(null,
					DamageSource.explosion(this, getOwner()), null, hitResult.getPos(), 1.5f, false, World.ExplosionSourceType.NONE);
			world.sendEntityStatus(this, (byte)3);
		}
		this.kill();
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(age % 5 == 0)
		{
			Vec3d vel = getVelocity();
			world.addParticle(ParticleRegistry.EJECTED_CORE_FLASH, getX(), getY(), getZ(), vel.x, vel.y, vel.z);
		}
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
}
