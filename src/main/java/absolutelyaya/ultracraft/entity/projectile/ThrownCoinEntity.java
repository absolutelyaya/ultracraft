package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ThrownCoinEntity extends ThrownItemEntity implements ProjectileEntityAccessor
{
	byte state;
	int damage;
	
	public ThrownCoinEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	private ThrownCoinEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.THROWN_COIN, world);
		setOwner(owner);
	}
	
	@Override
	protected ItemStack getItem()
	{
		return ItemStack.EMPTY;
	}
	
	@Override
	protected Item getDefaultItem()
	{
		return Items.AIR;
	}
	
	public static ThrownCoinEntity spawn(LivingEntity owner, World world)
	{
		return new ThrownCoinEntity(owner, world);
	}
	
	@Override
	protected void initDataTracker()
	{
	
	}
	
	@Override
	public boolean hasNoGravity()
	{
		return false;
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		super.onCollision(hitResult);
		if (!world.isClient)
		{
			world.sendEntityStatus(this, (byte)3);
			discard();
		}
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if((source.isIn(DamageTypeTags.HITSCAN) || source.isOf(DamageSources.RICOCHET)) && source.getAttacker() instanceof LivingEntity attacker)
		{
			boolean isDamageRicochet = source.isOf(DamageSources.RICOCHET);
			if(world.isClient)
			{
				world.playSound(getX(), getY(), getZ(), SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.PLAYERS, 0.25f, 1.2f + (isDamageRicochet ? 0.5f * amount : 0f), false);
				world.sendEntityStatus(this, (byte)3);
				kill();
				return true;
			}
			List<ThrownCoinEntity> list = world.getEntitiesByType(TypeFilter.instanceOf(ThrownCoinEntity.class), getBoundingBox().expand(16f),
					e -> !e.equals(this) && e.isUnused());
			if(list.size() > 0)
			{
				if(state == 0)
				{
					state = 1;
					damage = (int)(isDamageRicochet ? amount : 1);
					setOwner(source.getAttacker());
					return false;
				}
				
				ThrownCoinEntity closestCoin = null;
				float closestDistance = Float.MAX_VALUE;
				for (ThrownCoinEntity coin : list)
				{
					float dist = distanceTo(coin);
					if (dist < closestDistance)
					{
						closestDistance = dist;
						closestCoin = coin;
					}
				}
				if(closestCoin != null && isUnused())
				{
					ServerHitscanHandler.sendPacket((ServerWorld)world, getPos(), closestCoin.getPos(), (byte)6);
					closestCoin.damage(DamageSources.get(world, DamageSources.RICOCHET, attacker), isDamageRicochet ? amount + 1 : 1);
				}
			}
			else
			{
				List<Entity> potentialTargets = world.getOtherEntities(attacker, getBoundingBox().expand(32f),
						e -> !e.equals(this) && e.isAlive() && !e.isTeammate(attacker) && !(e instanceof ThrownCoinEntity));
				if(potentialTargets.size() > 0)
				{
					Entity closest = null;
					float closestDistance = Float.MAX_VALUE;
					for (Entity e : potentialTargets)
					{
						float dist = distanceTo(e);
						if (dist < closestDistance)
						{
							closestDistance = dist;
							closest = e;
						}
					}
					if(closest != null)
					{
						ServerHitscanHandler.sendPacket((ServerWorld)world, getPos(), closest.getEyePos(), (byte)6);
						closest.damage(DamageSources.get(world, DamageSources.RICOCHET, attacker), isDamageRicochet ? 5 * amount : 5);
					}
				}
				else
				{
					ServerHitscanHandler.sendPacket((ServerWorld)world, getPos(),
							getPos().add(Vec3d.fromPolar(random.nextFloat() * 360, random.nextFloat() * 360 - 180).multiply(64)), (byte)6);
				}
			}
			world.sendEntityStatus(this, (byte)3);
			kill();
		}
		return super.damage(source, amount);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(state == 1)
			state++;
		if(state == 2)
		{
			if(Math.max(1f - Math.abs(getVelocity().y * 6.5f), 0f) > 0.3f)
			{
				for (int i = 0; i < 2; i++)
					damage(DamageSources.get(world, DamageSources.RICOCHET, getOwner()), damage);
			}
			else
				damage(DamageSources.get(world, DamageSources.RICOCHET, getOwner()), damage);
		}
	}
	
	@Override
	public void move(MovementType movementType, Vec3d movement)
	{
		if(isUnused())
			super.move(movementType, movement);
	}
	
	@Override
	public void setParried(boolean val, PlayerEntity parrier)
	{
		setOwner(parrier);
	}
	
	@Override
	public boolean isParried()
	{
		return false;
	}
	
	@Override
	public boolean isParriable()
	{
		return state == 0;
	}
	
	@Override
	public boolean isBoostable()
	{
		return false;
	}
	
	@Override
	public void onParriedCollision(HitResult hitResult)
	{
	
	}
	
	@Override
	public boolean isHitscanHittable()
	{
		return state == 0;
	}
	
	public boolean isUnused()
	{
		return state != 1;
	}
}
