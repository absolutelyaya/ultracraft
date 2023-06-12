package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ThrownCoinEntity extends ThrownItemEntity implements ProjectileEntityAccessor
{
	protected static final TrackedData<Boolean> STOPPED = DataTracker.registerData(ThrownCoinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> STOPPED_TICKS = DataTracker.registerData(ThrownCoinEntity.class, TrackedDataHandlerRegistry.INTEGER);
	Entity lastTarget;
	final float flashRotSpeed;
	byte state;
	int damage = 0;
	
	public ThrownCoinEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
		flashRotSpeed = random.nextFloat() - 0.5f;
	}
	
	private ThrownCoinEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.THROWN_COIN, world);
		setOwner(owner);
		flashRotSpeed = random.nextFloat();
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
		super.initDataTracker();
		dataTracker.startTracking(STOPPED, false);
		dataTracker.startTracking(STOPPED_TICKS, 0);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(STOPPED))
			dataTracker.set(STOPPED_TICKS, 0);
	}
	
	@Override
	public boolean hasNoGravity()
	{
		return false;
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		if(hitResult.getType().equals(HitResult.Type.ENTITY))
			return;
		super.onCollision(hitResult);
		if (!world.isClient)
		{
			if(damage > 0)
				hitNext(DamageSources.get(world, DamageSources.RICOCHET, getOwner()), damage, (LivingEntity)getOwner());
			world.sendEntityStatus(this, (byte)3);
			discard();
		}
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(isRemoved())
			return false;
		if((source.isIn(DamageTypeTags.HITSCAN) || source.isOf(DamageSources.RICOCHET)) && source.getAttacker() instanceof LivingEntity attacker)
			return hitNext(source, amount, attacker);
		return super.damage(source, amount);
	}
	
	boolean hitNext(DamageSource source, float amount, LivingEntity attacker)
	{
		boolean isDamageRicochet = source.isOf(DamageSources.RICOCHET);
		if (world.isClient)
		{
			world.sendEntityStatus(this, (byte) 3);
			kill();
			return true;
		}
		else
			playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 1.2f + (isDamageRicochet ? 0.05f * amount : 0f));
		List<ThrownCoinEntity> list = world.getEntitiesByType(TypeFilter.instanceOf(ThrownCoinEntity.class), getBoundingBox().expand(16f),
				e -> !e.equals(this) && e.isUnused() && !e.isRemoved());
		if (list.size() > 0)
		{
			if (state == 0)
			{
				state = 1;
				damage = (int) (isDamageRicochet ? amount : 1);
				setOwner(attacker);
				return false;
			}
			
			ThrownCoinEntity closestCoin = null;
			float closestDistance = Float.MAX_VALUE;
			for (ThrownCoinEntity coin : list)
			{
				coin.dataTracker.set(STOPPED, true);
				dataTracker.set(STOPPED_TICKS, 0);
				float dist = distanceTo(coin);
				if (dist < closestDistance)
				{
					closestDistance = dist;
					closestCoin = coin;
				}
			}
			if (closestCoin != null && isUnused())
			{
				ServerHitscanHandler.sendPacket((ServerWorld) world, getPos(), closestCoin.getPos(), (byte) 6);
				closestCoin.damage(DamageSources.get(world, DamageSources.RICOCHET, attacker), isDamageRicochet ? amount + 1 : 1);
			}
		}
		else
		{
			List<Entity> potentialTargets = world.getOtherEntities(attacker, getBoundingBox().expand(32f),
					e -> !e.equals(this) && e.isAlive() && !e.isTeammate(attacker) && (e instanceof LivingEntity));
			if (potentialTargets.size() > 0)
			{
				Entity closest = null;
				float closestDistance = Float.MAX_VALUE;
				for (Entity e : potentialTargets)
				{
					float dist = distanceTo(e) + (e.equals(lastTarget) ? 10f : 0f);
					if (dist < closestDistance)
					{
						closestDistance = dist;
						closest = e;
					}
				}
				if (closest != null)
				{
					ServerHitscanHandler.sendPacket((ServerWorld) world, getPos(), closest.getEyePos(), (byte) 6);
					closest.damage(DamageSources.get(world, DamageSources.RICOCHET, attacker), isDamageRicochet ? 5 * amount : 5);
					//world.getPlayers().forEach(p -> p.sendMessage(Text.of("CHAIN END! final damage: " + (isDamageRicochet ? 5 * amount : 5))));
				}
			} else
			{
				ServerHitscanHandler.sendPacket((ServerWorld) world, getPos(),
						getPos().add(Vec3d.fromPolar(random.nextFloat() * 360, random.nextFloat() * 360 - 180).multiply(64)), (byte) 6);
			}
		}
		world.sendEntityStatus(this, (byte) 3);
		kill();
		return true;
	}
	
	@Override
	public void tick()
	{
		if(!dataTracker.get(STOPPED))
			super.tick();
		else if(dataTracker.get(STOPPED_TICKS) < 10)
			dataTracker.set(STOPPED_TICKS, dataTracker.get(STOPPED_TICKS) + 1);
		else
			dataTracker.set(STOPPED, false);
		if(state == 1)
			state++;
		if(state == 2)
		{
			if(Math.max(1f - Math.abs(getVelocity().y * 6.5f), 0f) > 0.3f || age > 80)
			{
				for (int i = 0; i < 2; i++)
					hitNext(DamageSources.get(world, DamageSources.RICOCHET, getOwner()), damage, (LivingEntity)getOwner());
			}
			else
				hitNext(DamageSources.get(world, DamageSources.RICOCHET, getOwner()), damage, (LivingEntity)getOwner());
		}
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
	public boolean isHitscanHittable()
	{
		return state == 0;
	}
	
	public boolean isUnused()
	{
		return state != 1;
	}
	
	public float getFlashRotSpeed()
	{
		return flashRotSpeed;
	}
	
	public boolean isStopped()
	{
		return dataTracker.get(STOPPED);
	}
}
