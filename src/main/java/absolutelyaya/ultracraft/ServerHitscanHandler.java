package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.projectile.ThrownCoinEntity;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

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
	
	static final Queue<ScheduledHitscan> scheduleAdditions = new ArrayDeque<>();
	static final List<ScheduledHitscan> schedule = new ArrayList<>();
	static int time = 0;
	
	public static void tickSchedule()
	{
		if(Ultracraft.isTimeFrozen())
			return;
		time++;
		schedule.addAll(scheduleAdditions);
		scheduleAdditions.clear();
		List<ScheduledHitscan> remove = new ArrayList<>();
		for (ScheduledHitscan sh : schedule)
		{
			if(sh.scheduleTime + sh.delay <= time)
			{
				if(sh.bounces > 0)
					performBouncingHitscan(sh.owner, sh.from, sh.visualFrom, sh.dest, sh.type, sh.damage, sh.maxHits, sh.bounces);
				else
					performHitscan(sh.owner, sh.from, sh.visualFrom, sh.dest, sh.type, sh.damage, sh.maxHits, sh.explosion);
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
		performHitscan(user, origin, visualOrigin, dest, type, damage, maxHits, explosion);
	}
	
	public static HitscanResult performHitscan(LivingEntity user, Vec3d from, Vec3d visualFrom, Vec3d to, byte type, float damage, int maxHits, @Nullable HitscanExplosionData explosion)
	{
		World world = user.getWorld();
		BlockHitResult bHit = world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
		Vec3d modifiedTo = bHit.getPos();
		List<Entity> entities = new ArrayList<>();
		Vec3d dir = to.subtract(from).normalize();
		Box box = user.getBoundingBox().stretch(dir.multiply(64.0)).expand(1.0, 1.0, 1.0);
		EntityHitResult finalEHit = null;
		boolean searchForEntities = true;
		while (searchForEntities)
		{
			EntityHitResult eHit = ProjectileUtil.raycast(user, from, modifiedTo, box,
					(entity) -> (!entities.contains(entity) && (!(entity instanceof ProjectileEntity) || (entity instanceof  ProjectileEntity proj && ((ProjectileEntityAccessor)proj).isHitscanHittable()) || type == SHARPSHOOTER)), 64f * 64f);
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
		AtomicBoolean disableExplosion = new AtomicBoolean(false);
		entities.forEach(e -> {
			e.damage(DamageSources.get(world, DamageSources.GUN, user), damage);
			if(type == SHARPSHOOTER && e instanceof ProjectileEntity proj)
				proj.kill();
			if(e instanceof ThrownCoinEntity)
				disableExplosion.set(true);
		});
		if(explosion != null && bHit != null && !bHit.getType().equals(HitResult.Type.MISS) && !disableExplosion.get())
			ExplosionHandler.explosion(null, world, new Vec3d(modifiedTo.x, modifiedTo.y, modifiedTo.z), world.getDamageSources().explosion(user, user),
					explosion.damage, explosion.falloff, explosion.radius, explosion.breakBlocks);
		if(entities.size() == 0 && user instanceof PlayerEntity p)
		{
			BlockState state = world.getBlockState(bHit.getBlockPos());
			if(state.getBlock() instanceof BellBlock bell)
				bell.ring(world, state, bHit, p, false);
		}
		sendPacket((ServerWorld)user.world, visualFrom, modifiedTo, type);
		if(bHit != null && !(entities.size() > 0 && maxHits == 0))
			return new HitscanResult(bHit, dir, entities.size()); //BlockHit
		else if(entities.size() > 0 && finalEHit != null)
			return new HitscanResult(finalEHit, dir, entities.size()); //EntityHit
		else
			return null; //miss
	}
	
	public static void performBouncingHitscan(LivingEntity user, byte type, float damage, int maxHits, int bounces)
	{
		Vec3d origin = user.getEyePos();
		Vec3d visualOrigin = origin.add(
				new Vec3d(-0.5f * (user instanceof PlayerEntity player && player.getMainArm().equals(Arm.LEFT) ? -1 : 1), -0.2f, 0.4f)
						.rotateX(-(float)Math.toRadians(user.getPitch())).rotateY(-(float) Math.toRadians(user.getYaw())));
		Vec3d dest = origin.add(user.getRotationVec(0.5f).normalize().multiply(64));
		performBouncingHitscan(user, origin, visualOrigin, dest, type, damage, maxHits, bounces);
	}
	
	public static void performBouncingHitscan(LivingEntity user, Vec3d from, Vec3d visualFrom, Vec3d to, byte type, float damage, int maxHits, int bounces)
	{
		HitscanResult lastResult = performHitscan(user, from, visualFrom, to, type, damage, maxHits, null);
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
			Vec3d hitPos = lastResult.finalHit.getPos();
			Vec3d lastDir = lastResult.dir;
			Vec3d hitNormal = new Vec3d(((BlockHitResult)lastResult.finalHit).getSide().getUnitVector());
			Vec3d dest = hitPos.add(lastDir.subtract(hitNormal.multiply(2 * lastDir.dotProduct(hitNormal))).normalize().multiply(64));
			scheduleAdditions.add(new ScheduledHitscan(user, hitPos, hitPos, dest, type, damage, maxHits, bounces, null, time, 1));
		}
	}
	
	public record HitscanExplosionData(float radius, float damage, float falloff, boolean breakBlocks) {}
	
	public record HitscanResult(HitResult finalHit, Vec3d dir, int entitiesHit) {}
	
	public record ScheduledHitscan(LivingEntity owner, Vec3d from, Vec3d visualFrom, Vec3d dest, byte type, float damage, int maxHits, int bounces, HitscanExplosionData explosion, long scheduleTime, int delay) {}
}
