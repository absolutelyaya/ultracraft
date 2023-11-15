package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.accessor.EntityAccessor;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import absolutelyaya.ultracraft.entity.machine.StreetCleanerEntity;
import absolutelyaya.ultracraft.entity.machine.V2Entity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
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
		((EntityAccessor)this).setTargetpriorityFunction(e -> 5);
	}
	
	private EjectedCoreEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.EJECTED_CORE, owner, world);
		((EntityAccessor)this).setTargetpriorityFunction(e -> 5);
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
		if(hitResult instanceof EntityHitResult entityHit)
		{
			if(entityHit.getEntity() instanceof StreetCleanerEntity cleaner && cleaner.isCanCounter())
				return;
			else if(entityHit.getEntity() instanceof MaliciousFaceEntity)
			{
				Vec3d newVel = getVelocity().multiply(-0.5f, 1f, -0.5f);
				setVelocity(newVel);
				Vec3d pos = entityHit.getPos();
				getWorld().playSound(null, new BlockPos((int)pos.x, (int)pos.y, (int)pos.z),
						SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 1f, 1.75f);
				return;
			}
		}
		super.onCollision(hitResult);
		
		if (!getWorld().isClient)
			explode(getOwner());
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(source.isIn(DamageTypeTags.HITSCAN))
			explode(source.getAttacker());
		return super.damage(source, amount);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(getWorld().getFluidState(getBlockPos()).isIn(FluidTags.LAVA))
			explode(getOwner());
		if(getWorld().isClient && UltracraftClient.getConfig().safeVFX)
			return;
		if(age % 5 == 0)
		{
			Vec3d vel = getVelocity();
			getWorld().addParticle(ParticleRegistry.EJECTED_CORE_FLASH, getX(), getY(), getZ(), vel.x, vel.y, vel.z);
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
	public boolean isBoostable()
	{
		return switch(getWorld().getGameRules().get(GameruleRegistry.PROJ_BOOST).get())
		{
			case ALLOW_ALL -> true;
			case ENTITY_TAG -> getType().isIn(EntityRegistry.PROJBOOSTABLE);
			case LIMITED -> (Object) this instanceof ShotgunPelletEntity;
			case DISALLOW -> false;
		} && age < 4;
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
	public void onParriedCollision(HitResult hitResult)
	{
	
	}
	
	@Override
	public boolean isHitscanHittable(byte type)
	{
		return true;
	}
	
	@Override
	public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed)
	{
		return false;
	}
	
	void explode(Entity exploder)
	{
		ExplosionHandler.explosion(null, getWorld(), getPos(), DamageSources.get(getWorld(), DamageSources.CORE_EJECT, this, exploder),
				10f, 4f, 3f, true);
		getWorld().sendEntityStatus(this, (byte)3);
		kill();
	}
}
