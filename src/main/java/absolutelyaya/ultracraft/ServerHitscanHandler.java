package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.accessor.EntityAccessor;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.components.player.IWingedPlayerComponent;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.HitscanDamageSource;
import absolutelyaya.ultracraft.entity.other.AbstractOrbEntity;
import absolutelyaya.ultracraft.entity.other.BackTank;
import absolutelyaya.ultracraft.entity.projectile.IIgnoreSharpshooter;
import absolutelyaya.ultracraft.entity.projectile.ThrownCoinEntity;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.util.AutoAimUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.*;
import java.util.function.Predicate;

public class ServerHitscanHandler
{
	public static final byte NORMAL = 0;
	public static final byte REVOLVER_PIERCE = 1;
	public static final byte RAILGUN_ELEC = 2;
	public static final byte RAILGUN_DRILL = 3;
	public static final byte RAILGUN_MALICIOUS = 4;
	public static final byte MALICIOUS = 5;
	public static final byte COIN_RICOCHET = 6;
	public static final byte SHARPSHOOTER = 7;
	
	static final Queue<IScheduledHitscan> scheduleAdditions = new ArrayDeque<>();
	static final List<IScheduledHitscan> schedule = new ArrayList<>();
	static int time = 0;
	
	public static void tickSchedule()
	{
		if(Ultracraft.isTimeFrozen())
			return;
		time++;
		schedule.addAll(scheduleAdditions);
		scheduleAdditions.clear();
		List<IScheduledHitscan> remove = new ArrayList<>();
		for (IScheduledHitscan sh : schedule)
		{
			if(sh.isReady(time))
			{
				sh.perform();
				remove.add(sh);
			}
		}
		schedule.removeAll(remove);
	}
	
	public static void sendPacket(ServerWorld world, Vec3d from, Vec3d to, byte type)
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeDouble(from.x);
		buf.writeDouble(from.y);
		buf.writeDouble(from.z);
		buf.writeDouble(to.x);
		buf.writeDouble(to.y);
		buf.writeDouble(to.z);
		buf.writeByte(type);
		for (ServerPlayerEntity player : world.getPlayers())
			ServerPlayNetworking.send(player, PacketRegistry.HITSCAN_PACKET_ID, buf);
	}
	
	public static void performHitscan(LivingEntity user, byte type, float damage)
	{
		performHitscan(user, type, damage, 1, null);
	}
	
	public static void performHitscan(LivingEntity user, byte type, float damage, HitscanExplosionData explosion)
	{
		performHitscan(user, type, damage, 1, explosion);
	}
	
	public static void performHitscan(LivingEntity user, byte type, int damage, int maxHits, boolean breakBlocks)
	{
		performHitscan(user, type, damage, maxHits, new HitscanExplosionData(2f, 0f, 0f, breakBlocks));
	}
	
	public static void performHitscan(LivingEntity user, byte type, float damage, int maxHits, @Nullable HitscanExplosionData explosion)
	{
		Vec3d origin = user.getEyePos();
		Vec3d visualOrigin = origin.add(
				new Vec3d(-0.5f * (user instanceof PlayerEntity player && player.getMainArm().equals(Arm.LEFT) ? -1 : 1), -0.2f, 0.4f)
						.rotateX(-(float)Math.toRadians(user.getPitch())).rotateY(-(float) Math.toRadians(user.getYaw())));
		Vec3d dest = user.getEyePos().add(user.getRotationVec(0.5f).multiply(64.0));
		new Hitscan(user, origin, visualOrigin, dest, type, damage, DamageSources.GUN).maxHits(maxHits).explosion(explosion).perform();
	}
	
	static boolean isValidTarget(Entity entity, byte type)
	{
		if(entity instanceof ProjectileEntity proj && ((ProjectileEntityAccessor)proj).isHitscanHittable(type))
			return true;
		if(entity.isSpectator() || !entity.canHit())
			return false;
		return !(entity instanceof ProjectileEntity || entity instanceof AbstractOrbEntity);
	}
	
	static float getDamageMultipier(World world, byte type)
	{
		if(type == NORMAL || type == REVOLVER_PIERCE || type == COIN_RICOCHET || type == SHARPSHOOTER)
			return world.getGameRules().getInt(GameruleRegistry.REVOLVER_DAMAGE);
		return 1f;
	}
	
	public static void performBouncingHitscan(LivingEntity user, byte type, float damage, RegistryKey<DamageType> damageType, int maxHits, int bounces, HitscanExplosionData explosion, float autoAim)
	{
		Vec3d origin = user.getEyePos();
		Vec3d visualOrigin = origin.add(
				new Vec3d(-0.5f * (user instanceof PlayerEntity player && player.getMainArm().equals(Arm.LEFT) ? -1 : 1), -0.2f, 0.4f)
						.rotateX(-(float)Math.toRadians(user.getPitch())).rotateY(-(float) Math.toRadians(user.getYaw())));
		Vec3d dest = origin.add(user.getRotationVec(0.5f).normalize().multiply(64));
		performBouncingHitscan(new Hitscan(user, origin, visualOrigin, dest, type, damage, damageType).maxHits(maxHits).bounces(bounces).explosion(explosion).autoAim(autoAim));
	}
	
	public static void performBouncingHitscan(Hitscan scan)
	{
		HitscanResult lastResult = scan.damageMult(getDamageMultipier(scan.owner.getWorld(), scan.type)).perform();
		if(scan.bounces < scan.maxBounces)
		{
			if(lastResult.finalHit != null)
			{
				if(lastResult.finalHit.getType().equals(HitResult.Type.BLOCK))
					scan.bounces++;
				else
					return; //final hit was not a Block, thus either maxHits are used up or there was no Block to be hit.
				scan.maxHits -= lastResult.entitiesHit;
				if(scan.maxHits <= 0)
					return; //maxHits are used up! No more bounces.
				if(lastResult.entitiesHit > 0 && scan.type == SHARPSHOOTER && scan.maxBounces < 5)
					scan.maxBounces++;
			}
			else
				return; //no hit at all! So, no bounces.
			if(lastResult.entitiesHit > 0 && scan.type == SHARPSHOOTER && scan.owner instanceof ServerPlayerEntity sp)
				Ultracraft.freeze(sp, Math.round(2 * (scan.damage / 3f)));
			Vec3d hitPos = lastResult.finalHit.getPos();
			Vec3d lastDir = lastResult.dir;
			Vec3d hitNormal = new Vec3d(((BlockHitResult)lastResult.finalHit).getSide().getUnitVector());
			Vec3d dest = hitPos.add(lastDir.subtract(hitNormal.multiply(2 * lastDir.dotProduct(hitNormal))).normalize().multiply(64));
			scheduleHitscan(scan.from(hitPos, hitPos).dest(dest), 1);
		}
	}
	
	public static void scheduleHitscan(Hitscan hitscan, int delay)
	{
		scheduleAdditions.add(new ScheduledHitscan(hitscan, time, delay));
	}
	
	public static void scheduleDelayedAimingHitscan(LivingEntity user, Vec3d from, Vec3d visualFrom, LivingEntity target, byte type, float damage, RegistryKey<DamageType> damageType, int maxHits, int bounces, HitscanExplosionData explosion, int reAimTime, int delay, boolean warn)
	{
		scheduleAdditions.add(new DelayedAimingHitscan(new Hitscan(user, from, visualFrom, Vec3d.ZERO, type, damage, damageType).maxHits(maxHits).bounces(bounces).explosion(explosion), target, time, reAimTime, delay, warn));
	}
	
	public record HitscanExplosionData(float radius, float damage, float falloff, boolean breakBlocks) {}
	
	public record HitscanResult(HitResult finalHit, Vec3d dir, int entitiesHit) {}
	
	public static class Hitscan
	{
		public LivingEntity owner;
		public Vec3d from, visualFrom, dest;
		public byte type;
		public float damage, autoAim = 0f, damageMultiplier;
		public HitscanDamageSource damageSource;
		public int maxHits = 1, bounces = 0, maxBounces = 0;
		public HitscanExplosionData explosion = null;
		
		public Hitscan(LivingEntity owner, Vec3d from, Vec3d visualFrom, Vec3d dest, byte type, float damage, RegistryKey<DamageType> damageType)
		{
			this.owner = owner;
			this.from = from;
			this.visualFrom = visualFrom;
			this.dest = dest;
			this.type = type;
			this.damage = damage;
			this.damageSource = DamageSources.getHitscan(owner.getWorld(), damageType, owner, this);
		}
		
		public Hitscan from(Vec3d from, Vec3d visualFrom)
		{
			this.from = from;
			this.visualFrom = visualFrom;
			return this;
		}
		
		public Hitscan dest(Vec3d dest)
		{
			this.dest = dest;
			return this;
		}
		
		public Hitscan damageMult(float mult)
		{
			damageMultiplier = mult;
			return this;
		}
		
		public Hitscan explosion(HitscanExplosionData data)
		{
			explosion = data;
			return this;
		}
		
		public Hitscan autoAim(float threshold)
		{
			autoAim = threshold;
			return this;
		}
		
		public Hitscan maxHits(int i)
		{
			maxHits = i;
			return this;
		}
		
		public Hitscan bounces(int i)
		{
			maxBounces = i;
			return this;
		}
		
		public HitscanResult perform()
		{
			World world = owner.getWorld();
			BlockHitResult bHit = world.raycast(new RaycastContext(from, dest, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, owner));
			Vec3d modifiedTo = bHit.getPos();
			List<Entity> entities = new ArrayList<>();
			Vec3d dir = dest.subtract(from).normalize();
			Box box = new Box(from.subtract(-1f, -1f, -1f), from.add(1f, 1f, 1f)).stretch(dir.multiply(64.0)).expand(1.0, 1.0, 1.0);
			EntityHitResult finalEHit = null;
			boolean searchForEntities = true;
			while (searchForEntities)
			{
				world.raycast(new RaycastContext(from, modifiedTo, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, owner));
				EntityHitResult eHit = raycast(owner, from, modifiedTo, box,
						(entity) -> !entities.contains(entity) && isValidTarget(entity, type), 0.25f, 64f);
				if(eHit == null)
					break;
				searchForEntities = eHit.getType() != HitResult.Type.MISS && maxHits > 0;
				if(eHit.getType() != HitResult.Type.MISS)
					finalEHit = eHit;
				if(searchForEntities)
				{
					maxHits--;
					from = eHit.getPos();
					if(maxHits == 0)
						modifiedTo = eHit.getPos();
					entities.add(eHit.getEntity());
				}
			}
			boolean disableExplosion = false;
			IWingedPlayerComponent winged = null;
			if(owner instanceof WingedPlayerEntity)
				winged = UltraComponents.WINGED_ENTITY.get(owner);
			boolean explodeProjectile = type == SHARPSHOOTER && winged != null && winged.getSharpshooterCooldown() <= 0;
			for (int i = 0; i < entities.size(); i++)
			{
				Entity e = entities.get(i);
				if((e instanceof BackTank && i > 0) || e == null) //Back Tanks shouldn't be hit after an entity is pierced
					continue;
				//hit the last pierced enemy with up to 10 of the remaining pierce shots. A Pierce revolver shot that hits just one enemy, will damage it 3 times.
				for (int j = 0; j < Math.min(10, i == entities.size() - 1 && maxHits < 16 ? maxHits + 1 : 1); j++)
					e.damage(damageSource, damage * getDamageMultipier(world, type));
				if(explodeProjectile && e instanceof ProjectileEntity proj && !(e instanceof IIgnoreSharpshooter || e instanceof ThrownCoinEntity))
				{
					ExplosionHandler.explosion(owner, world, proj.getPos(), DamageSources.get(world, DamageTypes.EXPLOSION, owner), 5f, 1f, 5f, true);
					proj.kill();
					explodeProjectile = false;
					if(winged != null)
						winged.setSharpshooterCooldown(5);
				}
				if(e instanceof ThrownCoinEntity)
					disableExplosion = true;
			}
			if(explosion != null && bHit != null && !bHit.getType().equals(HitResult.Type.MISS) && !disableExplosion)
				ExplosionHandler.explosion(null, world, new Vec3d(modifiedTo.x, modifiedTo.y, modifiedTo.z), world.getDamageSources().explosion(owner, owner),
						explosion.damage, explosion.falloff, explosion.radius, explosion.breakBlocks);
			if(entities.size() == 0 && owner instanceof PlayerEntity p)
			{
				BlockState state = world.getBlockState(bHit.getBlockPos());
				if(state.getBlock() instanceof BellBlock bell)
					bell.ring(world, state, bHit, p, false);
			}
			sendPacket((ServerWorld)owner.getWorld(), visualFrom, modifiedTo, type);
			if(bHit != null && !(entities.size() > 0 && maxHits == 0))
				return new HitscanResult(bHit, dir, entities.size()); //BlockHit
			else if(entities.size() > 0 && finalEHit != null)
				return new HitscanResult(finalEHit, dir, entities.size()); //EntityHit
			else
				return null; //miss
		}
	}
	
	public record ScheduledHitscan(Hitscan scan, long scheduleTime, int delay) implements IScheduledHitscan {
		@Override
		public boolean isReady(int time)
		{
			return scheduleTime + delay <= time;
		}
		
		@Override
		public void perform()
		{
			Vec3d dest = scan.dest;
			if(scan.autoAim > 0)
			{
				Entity closest = AutoAimUtil.getTarget(scan.owner, scan.owner, scan.owner.getWorld(), scan.from);
				if(closest != null)
				{
					Vec3d originalDir = dest.subtract(scan.from).normalize();
					Vector2f originalPolar = AutoAimUtil.dirToPolar(originalDir);
					Vec3d aimDir = ((EntityAccessor)closest).getRelativeTargetPoint().subtract(scan.from).normalize();
					Vector2f aimPolar = AutoAimUtil.dirToPolar(aimDir);
					if(originalPolar.distance(aimPolar) < scan.autoAim)
						dest = scan.from.add(aimDir.multiply(64));
				}
			}
			
			if(scan.maxBounces > 0)
				performBouncingHitscan(scan.dest(dest));
			else
				scan.perform();
		}
	}
	
	public record DelayedAimingHitscan(Hitscan scan, LivingEntity target, long scheduleTime, int reAimTime, int delay, boolean warn) implements IScheduledHitscan
	{
		static Vec3d dest;
		static boolean warned;
		
		@Override
		public boolean isReady(int time)
		{
			int warnTime = (int)(scheduleTime + reAimTime - 20);
			if((warnTime == time || (warnTime <= time && !warned)) && warn)
			{
				if(target instanceof ServerPlayerEntity player)
				{
					PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
					buf.writeVector3f(scan.visualFrom.toVector3f());
					buf.writeUuid(target.getUuid());
					player.getServer().getPlayerManager().getPlayerList().forEach(p ->
						ServerPlayNetworking.send(p, PacketRegistry.RICOCHET_WARNING_PACKET_ID, buf));
				}
				warned = true;
			}
			if(scheduleTime + reAimTime == time)
				reAim();
			return scheduleTime + delay <= time;
		}
		
		private void reAim()
		{
			dest = scan.from.add(target.getBoundingBox().getCenter().subtract(scan.from).normalize().multiply(64f));
		}
		
		@Override
		public void perform()
		{
			if(scan.maxBounces > 0)
				performBouncingHitscan(scan.dest(dest));
			else
				scan.dest(dest).perform();
			warned = false;
		}
	}
	
	public static EntityHitResult raycast(Entity source, Vec3d from, Vec3d to, Box box, Predicate<Entity> predicate, float rayWidth, float maxDist)
	{
		//Do a raycast in a much smaller area first to see if there's a hit directly at `from`.
		//Doing only the full scan could lead to a lot of unnecessary calculations while the target is actually right in the sources face.
		EntityHitResult result = entityRaycast(source, from, to,
				new Box(from.subtract(0.1, 0.1, 0.1), from.add(0.1, 0.1, 0.1)), predicate, rayWidth, maxDist);
		if (result == null)
			result = entityRaycast(source, from, to, box, predicate, rayWidth, maxDist);
		return result;
	}
	
	static EntityHitResult entityRaycast(Entity source, Vec3d from, Vec3d to, Box box, Predicate<Entity> predicate, float rayWidth, float maxDist)
	{
		World world = source.getWorld();
		Entity closest = null;
		Vec3d closestHit = null;
		float closestDist = maxDist;
		for (Entity e : world.getOtherEntities(source, box, predicate))
		{
			if(e.getRootVehicle().equals(source.getRootVehicle()))
				continue; //if entity is riding the same entity as the source, skip before doing any math
			Vec3d vel = e.getVelocity();
			Box hitbox = e.getBoundingBox().expand(vel.x, vel.y, vel.z).expand(e.getTargetingMargin() + rayWidth);
			if(hitbox.contains(from))
				return new EntityHitResult(closest, from); //if entity hitbox contains from, that's a guaranteed hit; no further math necessary
			Optional<Vec3d> hit = hitbox.raycast(from, to);
			if(hit.isEmpty())
				continue; //if entity isn't hit, skip
			float dist = (float)from.distanceTo(hit.get());
			if(dist < closestDist)
			{
				closest = e;
				closestHit = hit.get();
				closestDist = dist;
			}
		}
		if(closest == null)
			return null;
		return new EntityHitResult(closest, closestHit);
	}
	
	interface IScheduledHitscan
	{
		boolean isReady(int time);
		
		void perform();
	}
}
