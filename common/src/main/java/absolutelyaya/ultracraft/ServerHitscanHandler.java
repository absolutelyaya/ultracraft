package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.registry.PacketRegistry;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

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
		NetworkManager.sendToPlayers(world.getPlayers(), PacketRegistry.HITSCAN_PACKET_ID, buf);
	}
	
	public static void performHitscan(LivingEntity user, byte type, int damage, boolean pierces)
	{
		World world = user.getWorld();
		Vec3d origin = user.getPos().add(new Vec3d(0f, user.getStandingEyeHeight(), 0f));
		Vec3d from = origin;
		Vec3d to = user.getPos().add(0f, user.getStandingEyeHeight(), 0f).add(user.getRotationVec(0.5f).multiply(64.0));
		Vec3d visualTo;
		BlockHitResult bHit = world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
		to = bHit.getPos();
		visualTo = to;
		List<Entity> entities = new ArrayList<>();
		Box box = user.getBoundingBox().stretch(user.getRotationVec(0.5f).multiply(64.0)).expand(1.0, 1.0, 1.0);
		boolean searchForEntities = true;
		while (searchForEntities)
		{
			EntityHitResult eHit = ProjectileUtil.raycast(user, from, to, box, (entity) -> !entities.contains(entity), 64f * 64f);
			if(eHit == null)
				break;
			searchForEntities = eHit.getType() != HitResult.Type.MISS && pierces;
			if(eHit.getType() != HitResult.Type.MISS)
			{
				from = eHit.getPos();
				if(entities.size() == 0 && !pierces)
					visualTo = eHit.getPos();
				entities.add(eHit.getEntity());
			}
		}
		entities.forEach((e) -> e.damage(ProjectileDamageSource.mob(user), damage));
		sendPacket((ServerWorld)user.world, origin.add(new Vec3d(-0.5f, -0.1f, 0f).rotateY(-(float)Math.toRadians(user.getYaw()))), visualTo, type);
	}
}
