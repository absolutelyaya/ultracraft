package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.Ultracraft;
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
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class ThrownCoinEntity extends ThrownItemEntity implements ProjectileEntityAccessor
{
	protected static final TrackedData<Boolean> STOPPED = DataTracker.registerData(ThrownCoinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> DEADCOINED = DataTracker.registerData(ThrownCoinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> STOPPED_TICKS = DataTracker.registerData(ThrownCoinEntity.class, TrackedDataHandlerRegistry.INTEGER);
	Entity lastTarget;
	final float flashRotSpeed;
	byte hitTicks;
	int damage = 0, nextHitDelay = 5, realAge;
	boolean punched;
	
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
	
	public static ThrownCoinEntity spawn(LivingEntity owner, World world, Vec3d pos, int damage)
	{
		ThrownCoinEntity coin = new ThrownCoinEntity(owner, world);
		coin.setPos(pos.x, pos.y, pos.z);
		coin.setVelocity(0f, 0.4f, 0f);
		coin.damage = damage;
		world.spawnEntity(coin);
		return coin;
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(STOPPED, false);
		dataTracker.startTracking(DEADCOINED, false);
		dataTracker.startTracking(STOPPED_TICKS, 0);
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(STOPPED))
		{
			dataTracker.set(STOPPED_TICKS, 0);
			if(!dataTracker.get(STOPPED))
				age = realAge;
		}
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
			if(damage > 0 && !isDeadCoined() && !punched)
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
		if(!(source.getAttacker() instanceof LivingEntity attacker))
			return false;
		if(realAge <= 2) //deadcoin period
		{
			if(source.isOf(DamageSources.RICOCHET))
			{
				damage = Math.round(amount + 1);
				dataTracker.set(DEADCOINED, true);
				dataTracker.set(STOPPED, false);
			}
			return false;
		}
		if((source.isIn(DamageTypeTags.HITSCAN) || source.isOf(DamageSources.RICOCHET)))
			return hitNext(source, amount, attacker);
		return super.damage(source, amount);
	}
	
	boolean hitNext(DamageSource source, float amount, LivingEntity attacker)
	{
		boolean isDamageRicochet = source.isOf(DamageSources.RICOCHET);
		if(realAge <= 2) //deadcoin period
			return false;
		if (world.isClient)
			return true;
		else
			playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 1.2f + (isDamageRicochet ? 0.05f * amount : 0f));
		List<ThrownCoinEntity> list = world.getEntitiesByType(TypeFilter.instanceOf(ThrownCoinEntity.class), getBoundingBox().expand(16f),
				e -> !e.equals(this) && e.isUnused() && !e.isRemoved());
		if (list.size() > 0)
		{
			if (hitTicks == 0)
			{
				nextHitDelay = 2;
				hitTicks = 1;
				damage = Math.round(amount);
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
				if (hitTicks == 0)
				{
					nextHitDelay = 5;
					hitTicks = 1;
					setOwner(attacker);
					return false;
				}
				
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
					Ultracraft.freeze((ServerWorld)world, 3);
					//world.getPlayers().forEach(p -> p.sendMessage(Text.of("CHAIN END! final damage: " + (isDamageRicochet ? 5 * amount : 5))));
				}
			} else
			{
				ServerHitscanHandler.sendPacket((ServerWorld) world, getPos(),
						getPos().add(Vec3d.fromPolar(random.nextFloat() * 360, random.nextFloat() * 360 - 180).multiply(64)), (byte) 6);
			}
		}
		return true;
	}
	
	@Override
	public void tick()
	{
		if(!dataTracker.get(STOPPED))
		{
			super.tick();
			realAge = age;
		}
		else if(dataTracker.get(STOPPED_TICKS) < 10)
			dataTracker.set(STOPPED_TICKS, dataTracker.get(STOPPED_TICKS) + 1);
		else
			dataTracker.set(STOPPED, false);
		if(hitTicks > 0 && hitTicks < nextHitDelay)
			hitTicks++;
		if(hitTicks == nextHitDelay)
		{
			if(isSplittable())
			{
				for (int i = 0; i < 2; i++)
					hitNext(DamageSources.get(world, DamageSources.RICOCHET, getOwner()), damage, (LivingEntity)getOwner());
			}
			else
				hitNext(DamageSources.get(world, DamageSources.RICOCHET, getOwner()), damage, (LivingEntity)getOwner());
			world.sendEntityStatus(this, (byte) 3);
			kill();
		}
	}
	
	@Override
	public void setParried(boolean val, PlayerEntity parrier)
	{
		if(realAge <= 2)
			return;
		if(world.isClient)
		{
			world.sendEntityStatus(this, (byte) 3);
			kill();
			return;
		}
		if(damage == 0)
			damage = 1;
		ThrownCoinEntity coin = null;
		List<Entity> potentialTargets = world.getOtherEntities(parrier, getBoundingBox().expand(16f),
				e -> !e.equals(this) && e.isAlive() && !e.isTeammate(parrier) && (e instanceof LivingEntity));
		if(potentialTargets.size() > 0)
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
			Entity finalClosest = closest;
			EntityHitResult hit = ProjectileUtil.raycast(this, getPos(), closest.getEyePos(), getBoundingBox().expand(16f), e -> e == finalClosest, 32f * 32f);
			if(hit != null && hit.getType().equals(HitResult.Type.ENTITY))
			{
				coin = ThrownCoinEntity.spawn(parrier, world, hit.getPos(), damage + 1);
				ServerHitscanHandler.sendPacket((ServerWorld)world, getPos(), hit.getPos(), ServerHitscanHandler.RICOCHET);
				closest.damage(DamageSources.get(world, DamageSources.COIN_PUNCH, parrier), damage);
			}
		}
		else
		{
			BlockHitResult hit = world.raycast(new RaycastContext(getPos(), getPos().add(parrier.getRotationVec(0f).multiply(64f)),
					RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
			Vec3d to = hit.getPos();
			Vec3d normal = new Vec3d(hit.getSide().getUnitVector());
			
			ServerHitscanHandler.sendPacket((ServerWorld)world, getPos(), getPos().add(parrier.getRotationVec(0f).multiply(64f)),
					ServerHitscanHandler.RICOCHET);
			if(damage < 5)
				damage++;
			coin = ThrownCoinEntity.spawn(parrier, world, to.add(normal), damage);
		}
		if(coin != null)
			coin.punched = true;
		world.sendEntityStatus(this, (byte) 3);
		kill();
	}
	
	@Override
	public boolean isParried()
	{
		return false;
	}
	
	@Override
	public boolean isParriable()
	{
		return hitTicks == 0;
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
		return hitTicks == 0 && realAge > 2;
	}
	
	public boolean isUnused()
	{
		return hitTicks == 0 || hitTicks == nextHitDelay;
	}
	
	public float getFlashRotSpeed()
	{
		return flashRotSpeed;
	}
	
	public boolean isDeadCoined()
	{
		return dataTracker.get(DEADCOINED);
	}
	
	public boolean isSplittable()
	{
		return !punched && Math.max(1f - Math.abs(getVelocity().y * 6.5f), 0f) > 0.3f || realAge > 80;
	}
}