package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.accessor.EntityAccessor;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.other.AbstractOrbEntity;
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
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.PacketByteBuf;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ServerHitscanHandler
{
	public static final byte NORMAL = 0;
	public static final byte REVOLVER_PIERCE = 1;
	public static final byte RAILGUN_ELEC = 2;
	public static final byte RAILGUN_DRILL = 3;
	public static final byte RAILGUN_MALICIOUS = 4;
	public static final byte MALICIOUS = 5;
	public static final byte RICOCHET = 6;
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
		performHitscan(user, origin, visualOrigin, dest, type, damage, DamageSources.get(user.getWorld(), DamageSources.GUN, user), maxHits, explosion);
	}
	
	public static HitscanResult performHitscan(LivingEntity user, Vec3d from, Vec3d visualFrom, Vec3d to, byte type, float damage, DamageSource source, int maxHits, @Nullable HitscanExplosionData explosion)
	{
		World world = user.getWorld();
		BlockHitResult bHit = world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
		Vec3d modifiedTo = bHit.getPos();
		List<Entity> entities = new ArrayList<>();
		Vec3d dir = to.subtract(from).normalize();
		Box box = new Box(from.subtract(-1f, -1f, -1f), from.add(1f, 1f, 1f)).stretch(dir.multiply(64.0)).expand(1.0, 1.0, 1.0);
		EntityHitResult finalEHit = null;
		boolean searchForEntities = true;
		while (searchForEntities)
		{
			EntityHitResult eHit = ProjectileUtil.raycast(user, from, modifiedTo, box,
					(entity) -> !entities.contains(entity) && isValidTarget(entity, type), 64f * 64f);
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
		boolean explodeProjectile = type == SHARPSHOOTER && user instanceof WingedPlayerEntity p && p.getSharpshooterCooldown() <= 0;
		for (int i = 0; i < entities.size(); i++)
		{
			Entity e = entities.get(i);
			//hit the last pierced enemy with up to 10 of the remaining pierce shots. A Pierce revolver shot that hits just one enemy, will damage it 3 times.
			for (int j = 0; j < Math.min(10, i == entities.size() - 1 && maxHits < 16 ? maxHits + 1 : 1); j++)
				e.damage(source, damage * getDamageMultipier(world, type));
			if(explodeProjectile && e instanceof ProjectileEntity proj && !(e instanceof ThrownCoinEntity) && user instanceof WingedPlayerEntity p)
			{
				ExplosionHandler.explosion(user, world, proj.getPos(), DamageSources.get(world, DamageTypes.EXPLOSION, user), 5f, 1f, 5f, true);
				proj.kill();
				explodeProjectile = false;
				p.setSharpshooterCooldown(5);
			}
			if(e instanceof ThrownCoinEntity)
				disableExplosion = true;
		}
		if(explosion != null && bHit != null && !bHit.getType().equals(HitResult.Type.MISS) && !disableExplosion)
			ExplosionHandler.explosion(null, world, new Vec3d(modifiedTo.x, modifiedTo.y, modifiedTo.z), world.getDamageSources().explosion(user, user),
					explosion.damage, explosion.falloff, explosion.radius, explosion.breakBlocks);
		if(entities.size() == 0 && user instanceof PlayerEntity p)
		{
			BlockState state = world.getBlockState(bHit.getBlockPos());
			if(state.getBlock() instanceof BellBlock bell)
				bell.ring(world, state, bHit, p, false);
		}
		sendPacket((ServerWorld)user.getWorld(), visualFrom, modifiedTo, type);
		if(bHit != null && !(entities.size() > 0 && maxHits == 0))
			return new HitscanResult(bHit, dir, entities.size()); //BlockHit
		else if(entities.size() > 0 && finalEHit != null)
			return new HitscanResult(finalEHit, dir, entities.size()); //EntityHit
		else
			return null; //miss
	}
	
	static boolean isValidTarget(Entity entity, byte type)
	{
		if(entity instanceof ProjectileEntity proj && ((ProjectileEntityAccessor)proj).isHitscanHittable(type))
			return true;
		if(entity.isSpectator())
			return false;
		return !(entity instanceof ProjectileEntity || entity instanceof AbstractOrbEntity);
	}
	
	static float getDamageMultipier(World world, byte type)
	{
		return world.getGameRules().getInt(GameruleRegistry.GUN_DAMAGE); //TODO: check if type is revolver
	}
	
	public static void performBouncingHitscan(LivingEntity user, byte type, float damage, int maxHits, int bounces, float autoAim)
	{
		performBouncingHitscan(user, type, damage, maxHits, bounces, null, autoAim);
	}
	
	public static void performBouncingHitscan(LivingEntity user, byte type, float damage, int maxHits, int bounces, HitscanExplosionData explosion, float autoAim)
	{
		Vec3d origin = user.getEyePos();
		Vec3d visualOrigin = origin.add(
				new Vec3d(-0.5f * (user instanceof PlayerEntity player && player.getMainArm().equals(Arm.LEFT) ? -1 : 1), -0.2f, 0.4f)
						.rotateX(-(float)Math.toRadians(user.getPitch())).rotateY(-(float) Math.toRadians(user.getYaw())));
		Vec3d dest = origin.add(user.getRotationVec(0.5f).normalize().multiply(64));
		performBouncingHitscan(user, origin, visualOrigin, dest, type, damage, DamageSources.get(user.getWorld(), DamageSources.GUN, user), maxHits, bounces, explosion, autoAim);
	}
	
	public static void performBouncingHitscan(LivingEntity user, Vec3d from, Vec3d visualFrom, Vec3d to, byte type, float damage, DamageSource source, int maxHits, int bounces, HitscanExplosionData explosion, float autoAimThreshold)
	{
		HitscanResult lastResult = performHitscan(user, from, visualFrom, to, type, damage * getDamageMultipier(user.getWorld(), type), source, maxHits, explosion);
		if(bounces > 0)
		{
			if(lastResult.finalHit != null)
			{
				if(lastResult.finalHit.getType().equals(HitResult.Type.BLOCK))
					bounces--;
				else
					return; //final hit was not a Block, thus either maxHits are used up or there was no Block to be hit.
				maxHits -= lastResult.entitiesHit;
				if(maxHits <= 0)
					return; //maxHits are used up! No more bounces.
			}
			else
				return; //no hit at all! So, no bounces.
			if(lastResult.entitiesHit > 0 && type == SHARPSHOOTER && user instanceof ServerPlayerEntity sp)
				Ultracraft.freeze(sp, Math.round(2 * (damage / 3f)));
			Vec3d hitPos = lastResult.finalHit.getPos();
			Vec3d lastDir = lastResult.dir;
			Vec3d hitNormal = new Vec3d(((BlockHitResult)lastResult.finalHit).getSide().getUnitVector());
			Vec3d dest = hitPos.add(lastDir.subtract(hitNormal.multiply(2 * lastDir.dotProduct(hitNormal))).normalize().multiply(64));
			scheduleHitscan(user, hitPos, hitPos, dest, type, damage + (type == SHARPSHOOTER && damage < 6 ? 2 : 0), DamageSources.get(user.getWorld(), DamageSources.GUN, user), maxHits, bounces, explosion, 1, autoAimThreshold);
		}
	}
	
	public static void scheduleHitscan(LivingEntity user, Vec3d from, Vec3d visualFrom, Vec3d to, byte type, float damage, DamageSource source, int maxHits, int bounces, HitscanExplosionData explosion, int delay, float autoAimThreshold)
	{
		scheduleAdditions.add(new ScheduledHitscan(user, from, visualFrom, to, type, damage, source, maxHits, bounces, explosion, time, delay, autoAimThreshold));
	}
	
	public static void scheduleDelayedAimingHitscan(LivingEntity user, Vec3d from, Vec3d visualFrom, LivingEntity target, byte type, float damage, DamageSource source, int maxHits, int bounces, HitscanExplosionData explosion, int reAimTime, int delay, boolean warn)
	{
		scheduleAdditions.add(new DelayedAimingHitscan(user, from, visualFrom, target, type, damage, source, maxHits, bounces, explosion, time, reAimTime, delay, warn));
	}
	
	public record HitscanExplosionData(float radius, float damage, float falloff, boolean breakBlocks) {}
	
	public record HitscanResult(HitResult finalHit, Vec3d dir, int entitiesHit) {}
	
	public record ScheduledHitscan(LivingEntity owner, Vec3d from, Vec3d visualFrom, Vec3d dest, byte type, float damage, DamageSource source, int maxHits, int bounces, HitscanExplosionData explosion, long scheduleTime, int delay, float autoAimThreshold) implements IScheduledHitscan {
		@Override
		public boolean isReady(int time)
		{
			return scheduleTime + delay <= time;
		}
		
		@Override
		public void perform()
		{
			Vec3d dest = this.dest;
			if(autoAimThreshold > 0)
			{
				Entity closest = AutoAimUtil.getTarget(owner, owner, owner.getWorld(), from);
				if(closest != null)
				{
					Vec3d originalDir = dest.subtract(from).normalize();
					Vector2f originalPolar = AutoAimUtil.dirToPolar(originalDir);
					Vec3d aimDir = ((EntityAccessor)closest).getRelativeTargetPoint().subtract(from).normalize();
					Vector2f aimPolar = AutoAimUtil.dirToPolar(aimDir);
					if(originalPolar.distance(aimPolar) < autoAimThreshold)
						dest = from.add(aimDir.multiply(64));
				}
			}
			
			if(bounces > 0)
				performBouncingHitscan(owner, from, visualFrom, dest, type, damage, source, maxHits, bounces, explosion, autoAimThreshold);
			else
				performHitscan(owner, from, visualFrom, dest, type, damage, source, maxHits, explosion);
		}
	}
	
	public record DelayedAimingHitscan(LivingEntity owner, Vec3d from, Vec3d visualFrom, LivingEntity target, byte type, float damage, DamageSource source, int maxHits, int bounces, HitscanExplosionData explosion, long scheduleTime, int reAimTime, int delay, boolean warn) implements IScheduledHitscan
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
					buf.writeVector3f(visualFrom.toVector3f());
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
			dest = from.add(target.getBoundingBox().getCenter().subtract(from).normalize().multiply(64f));
		}
		
		@Override
		public void perform()
		{
			if(bounces > 0)
				performBouncingHitscan(owner, from, visualFrom, dest, type, damage, source, maxHits, bounces, explosion, 0f);
			else
				performHitscan(owner, from, visualFrom, dest, type, damage, source, maxHits, explosion);
			warned = false;
		}
	}
	
	interface IScheduledHitscan
	{
		boolean isReady(int time);
		
		void perform();
	}
}
