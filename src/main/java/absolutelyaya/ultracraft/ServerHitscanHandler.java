package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
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

import java.util.ArrayList;
import java.util.List;

public class ServerHitscanHandler
{
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
	
	public static void performHitscan(LivingEntity user, byte type, float damage, int maxHits)
	{
		performHitscan(user, type, damage, maxHits, null);
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
		World world = user.getWorld();
		Vec3d origin = user.getEyePos();
		Vec3d from = origin;
		Vec3d origunalTo = user.getPos().add(0f, user.getStandingEyeHeight(), 0f).add(user.getRotationVec(0.5f).multiply(64.0));
		Vec3d modifiedTo;
		BlockHitResult bHit = world.raycast(new RaycastContext(from, origunalTo, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
		origunalTo = bHit.getPos();
		modifiedTo = origunalTo;
		List<Entity> entities = new ArrayList<>();
		Box box = user.getBoundingBox().stretch(user.getRotationVec(0.5f).multiply(64.0)).expand(1.0, 1.0, 1.0);
		boolean searchForEntities = true;
		while (searchForEntities)
		{
			EntityHitResult eHit = ProjectileUtil.raycast(user, from, origunalTo, box,
					(entity) -> (!entities.contains(entity) &&
										 (!(entity instanceof ProjectileEntity proj) ||
												  ((ProjectileEntityAccessor)proj).isHitscanHittable())), 64f * 64f);
			if(eHit == null)
				break;
			searchForEntities = eHit.getType() != HitResult.Type.MISS && maxHits > 0;
			if(searchForEntities)
			{
				maxHits--;
				from = eHit.getPos();
				if(maxHits == 0)
					modifiedTo = eHit.getPos();
				entities.add(eHit.getEntity());
			}
		}
		entities.forEach(e -> e.damage(DamageSources.get(world, DamageSources.GUN, user), damage));
		if(explosion != null)
			ExplosionHandler.explosion(null, world, new Vec3d(modifiedTo.x, modifiedTo.y, modifiedTo.z), world.getDamageSources().explosion(user, user),
					explosion.damage, explosion.falloff, explosion.radius, explosion.breakBlocks);
		if(entities.size() == 0 && user instanceof PlayerEntity p)
		{
			BlockState state = world.getBlockState(bHit.getBlockPos());
			if(state.getBlock() instanceof BellBlock bell)
				bell.ring(world, state, bHit, p, false);
			
		}
		sendPacket((ServerWorld)user.world, origin.add(
				new Vec3d(-0.5f * (user instanceof PlayerEntity player && player.getMainArm().equals(Arm.LEFT) ? -1 : 1), -0.2f, 0.4f)
						.rotateY(-(float)Math.toRadians(user.getYaw()))), modifiedTo, type);
	}
	
	public record HitscanExplosionData(float radius, float damage, float falloff, boolean breakBlocks) {}
}
