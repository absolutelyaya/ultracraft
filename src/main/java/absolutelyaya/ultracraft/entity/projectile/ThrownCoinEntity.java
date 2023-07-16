package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import absolutelyaya.ultracraft.item.CoinItem;
import absolutelyaya.ultracraft.registry.CriteriaRegistry;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
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
	protected static final TrackedData<Boolean> CHARGEBACK = DataTracker.registerData(ThrownCoinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Boolean> PUNCHED = DataTracker.registerData(ThrownCoinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Integer> STOPPED_TICKS = DataTracker.registerData(ThrownCoinEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> SPLITS = DataTracker.registerData(ThrownCoinEntity.class, TrackedDataHandlerRegistry.INTEGER);
	Entity lastTarget, chargebackCauser;
	final float flashRotSpeed;
	byte hitTicks, hitscanType = ServerHitscanHandler.RICOCHET;
	int damage = 1, punchCounter = 0, nextHitDelay = 5, realAge;
	boolean splitting;
	float startY;
	
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
	
	public static ThrownCoinEntity spawn(LivingEntity owner, World world, Vec3d pos, int damage) //Used during Coin Punch
	{
		ThrownCoinEntity coin = new ThrownCoinEntity(owner, world);
		coin.setPos(pos.x, pos.y, pos.z);
		coin.startY = (float)coin.getY();
		coin.setVelocity(0f, 0.4f, 0f);
		coin.damage = damage;
		coin.dataTracker.set(PUNCHED, true);
		world.spawnEntity(coin);
		return coin;
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(STOPPED, false);
		dataTracker.startTracking(DEADCOINED, false);
		dataTracker.startTracking(CHARGEBACK, false);
		dataTracker.startTracking(PUNCHED, false);
		dataTracker.startTracking(STOPPED_TICKS, 0);
		dataTracker.startTracking(SPLITS, 0);
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
		if (!getWorld().isClient)
		{
			if(damage > 1 && !isDeadCoined() && !dataTracker.get(PUNCHED))
				hitNext(DamageSources.get(getWorld(), DamageSources.RICOCHET, getOwner()), damage, (LivingEntity)getOwner());
			if(dataTracker.get(PUNCHED) && punchCounter > 25)
				dropStack(CoinItem.getStack(getOwner().getDisplayName().getString(), punchCounter));
			if (!isRemoved())
			{
				getWorld().sendEntityStatus(this, (byte)3);
				discard();
			}
		}
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(isRemoved())
			return false;
		if(!(source.getAttacker() instanceof LivingEntity attacker))
			return false;
		if(attacker instanceof MaliciousFaceEntity malicious)
		{
			damage = (int)Math.ceil(amount);
			dataTracker.set(CHARGEBACK, true);
			chargebackCauser = attacker;
			hitscanType = ServerHitscanHandler.MALICIOUS;
			if(getOwner() instanceof ServerPlayerEntity player)
				CriteriaRegistry.CHARGEBACK.trigger(player, malicious);
			return hitNext(source, amount, attacker);
		}
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
		{
			if(isSplittable() && !splitting)
				dataTracker.set(SPLITS, dataTracker.get(SPLITS) + 1);
			return hitNext(source, amount, attacker);
		}
		return super.damage(source, amount);
	}
	
	boolean hitNext(DamageSource source, float amount, LivingEntity attacker)
	{
		boolean isDamageChargeback = source.isOf(DamageSources.CHARGEBACK);
		boolean isDamageRicochet = source.isOf(DamageSources.RICOCHET) || isDamageChargeback;
		if(realAge <= 2 && !isDamageChargeback) //deadcoin period
			return false;
		if (getWorld().isClient)
			return true;
		else
			playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 1.2f + (isDamageRicochet ? 0.05f * amount : 0f));
		List<ThrownCoinEntity> list = getWorld().getEntitiesByType(TypeFilter.instanceOf(ThrownCoinEntity.class), getBoundingBox().expand(16f),
				e -> e.isUnused() && !e.isRemoved() && !(isDamageChargeback && e.age <= 2));
		if (list.size() > 1 && !splitting)
		{
			if (hitTicks == 0)
			{
				nextHitDelay = 2;
				hitTicks = 1;
				damage = Math.round(amount);
				if(!dataTracker.get(CHARGEBACK))
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
				if (dist < closestDistance && !coin.equals(this))
				{
					closestDistance = dist;
					closestCoin = coin;
				}
			}
			if (closestCoin != null && isUnused())
			{
				boolean cb = dataTracker.get(CHARGEBACK);
				ServerHitscanHandler.sendPacket((ServerWorld) getWorld(), getPos(), closestCoin.getPos(), hitscanType);
				closestCoin.dataTracker.set(CHARGEBACK, cb);
				if(!splitting)
					closestCoin.dataTracker.set(SPLITS, dataTracker.get(SPLITS));
				closestCoin.damage(DamageSources.get(getWorld(), cb ? DamageSources.CHARGEBACK : DamageSources.RICOCHET, attacker), isDamageRicochet ? amount + 1 : 1);
			}
			getWorld().sendEntityStatus(this, (byte) 3);
			kill();
		}
		if((hitTicks == 0 || hitTicks == nextHitDelay) && (list.size() <= (dataTracker.get(CHARGEBACK) ? 2 : 1) || splitting)) // if it's 1, the chargeback never hits an entity for some reason.
		{
			List<Entity> potentialTargets;
			if(dataTracker.get(CHARGEBACK))
				potentialTargets = getWorld().getOtherEntities(getOwner(), getBoundingBox().expand(32f),
						e -> !e.equals(this) && (e instanceof LivingEntity) && ((LivingEntityAccessor)e).isRicochetHittable() && !e.isTeammate(getOwner()));
			else
			{
				potentialTargets = getWorld().getOtherEntities(attacker, getBoundingBox().expand(32f),
						e -> !e.equals(this) && (e instanceof EjectedCoreEntity));
				if(potentialTargets.size() == 0)
					potentialTargets = getWorld().getOtherEntities(attacker, getBoundingBox().expand(32f),
							e -> !e.equals(this) && (e instanceof LivingEntity) && ((LivingEntityAccessor)e).isRicochetHittable() && !e.isTeammate(attacker));
			}
			if (potentialTargets.size() > 0)
			{
				if (hitTicks == 0)
				{
					nextHitDelay = 5;
					hitTicks = 1;
					if(!dataTracker.get(CHARGEBACK))
						setOwner(attacker);
					return false;
				}
				
				Entity closest = null;
				float closestDistance = Float.MAX_VALUE;
				for (Entity e : potentialTargets)
				{
					float dist = distanceTo(e) + (e.equals(lastTarget) ? 10f : 0f);
					if (dist < closestDistance && !(getOwner().isPlayer() && e.isPlayer() && !getWorld().getServer().isPvpEnabled()))
					{
						closestDistance = dist;
						closest = e;
					}
				}
				if (closest != null)
				{
					if(dataTracker.get(CHARGEBACK))
					{
						ServerHitscanHandler.sendPacket((ServerWorld) getWorld(), getPos(), closest.getEyePos(), hitscanType);
						closest.damage(DamageSources.get(getWorld(), DamageSources.CHARGEBACK, chargebackCauser), chargebackCauser == closest ? Float.MAX_VALUE : damage);
						ExplosionHandler.explosion(getOwner(), getWorld(), closest.getPos(),
								DamageSources.get(getWorld(), DamageTypes.EXPLOSION, this, getOwner()), 10, 0f, 5.5f, true);
						Ultracraft.freeze((ServerWorld)getWorld(), 5);
						list.forEach(Entity::kill); //necessary because otherwise *two* final chargeback attacks occur
						return true;
					}
					if(closest instanceof ServerPlayerEntity player)
					{
						ServerHitscanHandler.scheduleDelayedAimingHitscan((LivingEntity)getOwner(), getPos(), getPos(), player, hitscanType,
								(isDamageRicochet ? Math.max(amount, 1) : 1), DamageSources.get(getWorld(), DamageSources.RICOCHET,
										this, getOwner()), 1, 0, null,
								15 + 10 * (dataTracker.get(SPLITS) + 1), 20 + 10 * (dataTracker.get(SPLITS) + 1), true);
						if(getOwner() instanceof ServerPlayerEntity attackingPlayer)
						{
							CriteriaRegistry.RICOCHET.trigger(attackingPlayer, damage);
							attackingPlayer.setAttacking(player);
						}
					}
					else
					{
						ServerHitscanHandler.performBouncingHitscan(attacker, getPos(), getPos(), closest.getBoundingBox().getCenter(), hitscanType,
								isDamageRicochet ? 5 * amount : 5, DamageSources.get(getWorld(), DamageSources.RICOCHET, this, getOwner()), 1, 0);
						Ultracraft.freeze((ServerWorld)getWorld(), 3);
						if(getOwner() instanceof ServerPlayerEntity player)
							CriteriaRegistry.RICOCHET.trigger(player, damage);
						//world.getPlayers().forEach(p -> p.sendMessage(Text.of("CHAIN END! final damage: " + (isDamageRicochet ? 5 * amount : 5))));
					}
				}
				if(dataTracker.get(SPLITS) > 0)
					return performSplits(source, amount, attacker, potentialTargets.size() > 1);
			}
			else
			{
				ServerHitscanHandler.sendPacket((ServerWorld) getWorld(), getPos(),
						getPos().add(Vec3d.fromPolar(random.nextFloat() * 360, random.nextFloat() * 360 - 180).multiply(64)), hitscanType);
				if(dataTracker.get(SPLITS) > 0)
					return performSplits(source, amount, attacker, false);
			}
			getWorld().sendEntityStatus(this, (byte) 3);
			kill();
		}
		return true;
	}
	
	boolean performSplits(DamageSource source, float amount, LivingEntity attacker, boolean hasTargets)
	{
		dataTracker.set(SPLITS, dataTracker.get(SPLITS) - 1);
		if(hasTargets)
		{
			nextHitDelay = 2;
			hitTicks = 1;
		}
		else
			hitTicks = (byte)nextHitDelay;
		hitNext(source, amount, attacker);
		splitting = true;
		if(dataTracker.get(SPLITS) < 0)
		{
			getWorld().sendEntityStatus(this, (byte) 3);
			kill();
		}
		return true;
	}
	
	@Override
	public void tick()
	{
		if(!dataTracker.get(STOPPED) && !splitting)
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
			hitNext(DamageSources.get(getWorld(), DamageSources.RICOCHET, getOwner()), damage, (LivingEntity)getOwner());
		baseTick();
	}
	
	@Override
	public void setParried(boolean val, PlayerEntity parrier)
	{
		if(damage == 0)
			damage = 1;
		ThrownCoinEntity coin = null;
		List<Entity> potentialTargets = getWorld().getOtherEntities(parrier, getBoundingBox().expand(16f),
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
				coin = ThrownCoinEntity.spawn(parrier, getWorld(), hit.getPos(), damage + 1);
				ServerHitscanHandler.sendPacket((ServerWorld)getWorld(), getPos(), hit.getPos(), ServerHitscanHandler.RICOCHET);
				closest.damage(DamageSources.get(getWorld(), DamageSources.COIN_PUNCH, parrier), damage);
			}
		}
		else
		{
			BlockHitResult hit = getWorld().raycast(new RaycastContext(getPos(), getPos().add(parrier.getRotationVec(0f).multiply(64f)),
					RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
			Vec3d to = hit.getPos();
			Vec3d normal = new Vec3d(hit.getSide().getUnitVector());
			
			ServerHitscanHandler.sendPacket((ServerWorld)getWorld(), getPos(), getPos().add(parrier.getRotationVec(0f).multiply(64f)),
					ServerHitscanHandler.RICOCHET);
			if(damage < 5)
				damage++;
			if(!hit.getType().equals(HitResult.Type.MISS))
				coin = ThrownCoinEntity.spawn(parrier, getWorld(), to.add(normal), damage);
		}
		if(coin != null)
			coin.punchCounter = punchCounter + 1;
		if(parrier instanceof ServerPlayerEntity player)
		{
			CriteriaRegistry.COIN_PUNCH.trigger(player, punchCounter);
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeInt(punchCounter);
			ServerPlayNetworking.send(player, PacketRegistry.COIN_PUNCH_PACKET_ID, buf);
		}
		getWorld().sendEntityStatus(this, (byte) 3);
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
		return !dataTracker.get(PUNCHED) && !dataTracker.get(CHARGEBACK) && (Math.max(1f - Math.abs(getVelocity().y * 6.5f), 0f) > 0.35f || getY() < startY + 0.25f);
	}
	
	public void setStartY(float val)
	{
		startY = val;
	}
}
