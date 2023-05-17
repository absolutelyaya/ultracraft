package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.MeleeInterruptable;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.IPunchableBlock;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.projectile.ShotgunPelletEntity;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class PacketRegistry
{
	public static final Identifier PUNCH_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "parry");
	public static final Identifier PUNCH_BLOCK_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "punch_block");
	public static final Identifier PRIMARY_SHOT_PACKET_ID_C2S = new Identifier(Ultracraft.MOD_ID, "primary_shot_c2s");
	public static final Identifier SET_HIGH_VELOCITY_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "sethivel_c2s");
	public static final Identifier DASH_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "dash_c2s");
	public static final Identifier GROUND_POUND_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "ground_pound_c2s");
	public static final Identifier REQUEST_HIVEL_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "request_hivel");
	
	public static final Identifier FREEZE_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "freeze");
	public static final Identifier HITSCAN_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "hitscan");
	public static final Identifier DASH_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "dash_s2c");
	public static final Identifier BLEED_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "bleed");
	public static final Identifier SET_HIGH_VELOCITY_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "sethivel_s2c");
	public static final Identifier SET_GUNCD_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "set_gcd");
	public static final Identifier CATCH_FISH_ID = new Identifier(Ultracraft.MOD_ID, "fish");
	public static final Identifier SYNC_RULE = new Identifier(Ultracraft.MOD_ID, "sync_rule");
	public static final Identifier ENTITY_TRAIL = new Identifier(Ultracraft.MOD_ID, "entity_trail");
	public static final Identifier GROUND_POUND_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "ground_pound_s2c");
	public static final Identifier EXPLOSION_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "explosion");
	public static final Identifier PRIMARY_SHOT_PACKET_ID_S2C = new Identifier(Ultracraft.MOD_ID, "primary_shot_s2c");
	
	public static void registerC2S()
	{
		ServerPlayNetworking.registerGlobalReceiver(PUNCH_PACKET_ID, (server, player, handler, buf, sender) -> {
			World world = player.world;
			Entity target;
			if(buf.readBoolean())
				target = world.getEntityById(buf.readInt());
			else
				target = null;
			
			server.execute(() -> {
				ProjectileEntity p;
				Vec3d forward = player.getRotationVector();
				Vec3d pos = player.getCameraPosVec(0f).add(forward.normalize());
				List<ProjectileEntity> projectiles = player.world.getEntitiesByClass(ProjectileEntity.class,
						new Box(pos.x - 0.75f, pos.y - 0.75f, pos.z - 0.75f, pos.x + 0.75f, pos.y + 0.75f, pos.z + 0.75f),
						(e) -> !((ProjectileEntityAccessor)e).isParried());
				
				player.swingHand(Hand.OFF_HAND, true);
				
				//Punch Entity
				if(target != null)
				{
					if(!player.getOffHandStack().isEmpty())
						target.setFireTicks(100);
					if (target instanceof MeleeInterruptable mp && (!(mp instanceof MobEntity) || ((MobEntity)mp).isAttacking()))
					{
						Ultracraft.freeze((ServerWorld) player.world, 10);
						target.damage(DamageSources.get(world, DamageSources.INTERRUPT, player), 6);
						mp.onInterrupt(player);
						world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.75f, 2f);
						player.heal(4);
					}
					else
					{
						world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 0.75f, 0.5f);
						target.damage(world.getDamageSources().mobAttack(player), 1);
					}
					boolean fatal = !target.isAlive();
					Vec3d vel = forward.multiply(fatal ? 1.5f : 0.75f);
					if(target instanceof ProjectileEntity || (target instanceof LivingEntityAccessor && ((LivingEntityAccessor)target).takePunchKnockback()))
						target.setVelocity(vel);
					return;
				}
				
				//Projectile Parry
				if(projectiles.size() > 0)
					p = getNearestProjectile(projectiles, pos);
				else
					return;
				if(!((ProjectileEntityAccessor)p).isParriable())
					return;
				if(player.equals(p.getOwner()) && p instanceof ThrownItemEntity thrown)
				{
					if(player.world.getGameRules().getBoolean(GameruleRegistry.ALLOW_PROJ_BOOST_THROWABLE) || thrown instanceof ShotgunPelletEntity)
						Ultracraft.freeze((ServerWorld) player.world, 5);
					else
						return;
				}
				else
					Ultracraft.freeze((ServerWorld) player.world, 10);
				world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.75f, 2f);
				ProjectileEntityAccessor pa = (ProjectileEntityAccessor)p;
				pa.setParried(true, player);
				p.setVelocity(forward.multiply(2.5f));
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(PUNCH_BLOCK_PACKET_ID, (server, player, handler, buf, sender) -> {
			World world = player.getWorld();
			BlockPos target = buf.readBlockPos();
			boolean mainHand = buf.readBoolean();
			
			server.execute(() -> {
				if(target != null)
				{
					BlockState state = world.getBlockState(target);
					if(state.getBlock() instanceof IPunchableBlock punchable)
						punchable.onPunch(player, target, mainHand);
				}
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(PRIMARY_SHOT_PACKET_ID_C2S, (server, player, handler, buf, sender) -> {
			server.execute(() -> {
				if (player.getMainHandStack().getItem() instanceof AbstractWeaponItem gun)
				{
					if (!gun.onPrimaryFire(player.world, player))
						return;
					for (ServerPlayerEntity p : ((ServerWorld)player.world).getPlayers())
					{
						PacketByteBuf cbuf = new PacketByteBuf(Unpooled.buffer());
						cbuf.writeUuid(player.getUuid());
						ServerPlayNetworking.send(p, PRIMARY_SHOT_PACKET_ID_S2C, cbuf);
					}
				}
				else
					Ultracraft.LOGGER.warn(player + " tried to use primary fire action but is holding a non-weapon Item!");
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(SET_HIGH_VELOCITY_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			WingedPlayerEntity winged = (WingedPlayerEntity)player;
			if(winged == null)
				return;
			boolean b = buf.readBoolean();
			server.execute(() -> winged.setWingsVisible(b));
			for (ServerPlayerEntity p : ((ServerWorld)player.world).getPlayers())
			{
				buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeUuid(player.getUuid());
				buf.writeBoolean(b);
				ServerPlayNetworking.send(p, SET_HIGH_VELOCITY_S2C_PACKET_ID, buf);
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(DASH_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			Vec3d dir = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeUuid(player.getUuid());
			buf.writeDouble(dir.x);
			buf.writeDouble(dir.y);
			buf.writeDouble(dir.z);
			for (ServerPlayerEntity p : ((ServerWorld)player.world).getPlayers())
				ServerPlayNetworking.send(p, DASH_S2C_PACKET_ID, buf);
		});
		ServerPlayNetworking.registerGlobalReceiver(GROUND_POUND_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			boolean start = buf.readBoolean();
			boolean strong = buf.readBoolean();
			server.execute(() -> {
				WingedPlayerEntity winged = (WingedPlayerEntity)player;
				if(start)
					winged.startGroundPound();
				else
					winged.completeGroundPound(strong);
			});
			if(start)
				return;
			buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeUuid(player.getUuid());
			buf.writeDouble(player.getX());
			buf.writeDouble(player.getY());
			buf.writeDouble(player.getZ());
			for (ServerPlayerEntity p : ((ServerWorld)player.world).getPlayers())
				ServerPlayNetworking.send(p, GROUND_POUND_S2C_PACKET_ID, buf);
		});
		ServerPlayNetworking.registerGlobalReceiver(REQUEST_HIVEL_PACKET_ID, (server, player, handler, buf, sender) -> {
			UUID targetID = buf.readUuid();
			PlayerEntity target = server.getPlayerManager().getPlayer(targetID);
			if(target == null)
				return;
			buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeUuid(targetID);
			buf.writeBoolean(((WingedPlayerEntity)target).isWingsActive());
			ServerPlayNetworking.send(player, SET_HIGH_VELOCITY_S2C_PACKET_ID, buf);
		});
	}
	
	static ProjectileEntity getNearestProjectile(List<ProjectileEntity> projectiles, Vec3d to)
	{
		double nearestDistance = 100.0;
		ProjectileEntity nearest = null;
		
		for (ProjectileEntity e : projectiles)
		{
			double distance = e.squaredDistanceTo(to);
			if(distance < nearestDistance)
			{
				nearest = e;
				nearestDistance = distance;
			}
		}
		return nearest;
	}
}
