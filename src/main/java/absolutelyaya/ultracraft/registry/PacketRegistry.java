package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.MeleeParriable;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.IPunchableBlock;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.particle.goop.GoopDropParticleEffect;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class PacketRegistry
{
	public static final Identifier PUNCH_ENTITY_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "punch_entiy");
	public static final Identifier PUNCH_BLOCK_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "punch_block");
	public static final Identifier PRIMARY_SHOT_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "primary_shot");
	public static final Identifier SET_HIGH_VELOCITY_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "sethivel");
	public static final Identifier DASH_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "dash_c2s");
	
	public static final Identifier HITSCAN_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "hitscan");
	public static final Identifier SERVER_OPTIONS_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "join_server");
	public static final Identifier DASH_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "dash_s2c");
	public static final Identifier RESPAWN_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "respawn");
	public static final Identifier BLEED_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "bleed");
	
	public static void registerC2S()
	{
		ServerPlayNetworking.registerGlobalReceiver(PUNCH_ENTITY_PACKET_ID, (server, player, handler, buf, sender) -> {
			World world = player.getWorld();
			Entity target = world.getEntityById(buf.readInt());
			boolean flame = buf.readBoolean();
			
			if(target != null)
			{
				target.damage(DamageSource.mob(player), 10);
				if(flame)
					target.setFireTicks(100);
				boolean fatal = !target.isAlive();
				if(target instanceof ProjectileEntity p)
				{
					Ultracraft.freeze(10);
					world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.75f, 2f);
					ProjectileEntityAccessor pa = (ProjectileEntityAccessor)p;
					pa.setParried(true, player);
				}
				else if (target instanceof MeleeParriable mp && (!(mp instanceof MobEntity) || ((MobEntity)mp).isAttacking()))
				{
					Ultracraft.freeze(10);
					target.damage(DamageSource.mob(player), 20); //total damage: 30
					world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.75f, 2f);
					player.heal(4);
				}
				else
					world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 0.75f, 0.5f);
				Vec3d vel = player.getRotationVecClient().normalize().multiply(fatal ? 2f : 1f).multiply(target instanceof ProjectileEntity ? 2.5f : 1f);
				target.setVelocity(vel);
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(PUNCH_BLOCK_PACKET_ID, (server, player, handler, buf, sender) -> {
			World world = player.getWorld();
			BlockPos target = buf.readBlockPos();
			
			server.execute(() -> {
				if(target != null)
				{
					BlockState state = world.getBlockState(target);
					if(state.getBlock() instanceof IPunchableBlock punchable)
						punchable.onPunch(player, target);
				}
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(PRIMARY_SHOT_PACKET_ID, (server, player, handler, buf, sender) -> {
			if (player.getMainHandStack().getItem() instanceof AbstractWeaponItem gun)
				gun.onPrimaryFire(player.world, player);
			else
				Ultracraft.LOGGER.warn(player + " tried to use primary fire action but is holding a non-weapon Item!");
		});
		ServerPlayNetworking.registerGlobalReceiver(SET_HIGH_VELOCITY_PACKET_ID, (server, player, handler, buf, sender) -> {
			((WingedPlayerEntity)player).setWingsVisible(buf.readBoolean());
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
			
			server.execute(() -> {
				player.setVelocity(dir);
				((WingedPlayerEntity)player).onDash();
			});
		});
	}
	
	public static void registerS2C()
	{
		ClientPlayNetworking.registerGlobalReceiver(HITSCAN_PACKET_ID, ((client, handler, buf, sender) -> {
			UltracraftClient.HITSCAN_HANDLER.addEntry(
					new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
					new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
					buf.readByte());
		}));
		ClientPlayNetworking.registerGlobalReceiver(SERVER_OPTIONS_PACKET_ID, ((client, handler, buf, sender) -> {
			Ultracraft.FreezeOption = buf.readEnumConstant(Ultracraft.Option.class);
			Ultracraft.HiVelOption = buf.readEnumConstant(Ultracraft.Option.class);
			Ultracraft.LOGGER.info("Synced Server Options!");
		}));
		ClientPlayNetworking.registerGlobalReceiver(DASH_S2C_PACKET_ID, (client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			PlayerEntity player = client.player;
			Vec3d dir = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			Random rand = new Random();
			
			((WingedPlayerEntity)player).onDash();
			Vec3d pos;
			for (int i = 0; i < 5; i++)
			{
				Vec3d particleVel = new Vec3d(-dir.x, -dir.y, -dir.z).multiply(rand.nextDouble() * 0.33 + 0.1);
				pos = player.getPos().add((rand.nextDouble() - 0.5) * player.getWidth() * 2,
						rand.nextDouble() * player.getHeight() + 0.5, (rand.nextDouble() - 0.5) * player.getWidth() * 2).add(dir.multiply(0.25));
				player.world.addParticle(ParticleRegistry.DASH, pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(RESPAWN_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player != null)
				((WingedPlayerEntity)client.player).setWingsVisible(buf.readBoolean());
		}));
		ClientPlayNetworking.registerGlobalReceiver(BLEED_PACKET_ID, (((client, handler, buf, responseSender) -> {
			if(client.player == null)
				return;
			float amount = buf.readFloat();
			Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			double halfheight = buf.readDouble();
			Random rand = new Random();
			for (int i = 0; i < 3 * amount; i++)
				client.player.world.addParticle(new GoopDropParticleEffect(new Vec3d(0.56, 0.09, 0.01),
								0.6f + rand.nextFloat() * 0.4f), pos.x, pos.y + halfheight, pos.z,
						rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
			//TODO: if within healing distance, add splatters to screen
		})));
	}
}
