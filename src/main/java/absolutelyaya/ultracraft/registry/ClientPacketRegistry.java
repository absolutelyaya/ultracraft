package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.particle.goop.GoopDropParticleEffect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

@SuppressWarnings("CodeBlock2Expr")
@Environment(EnvType.CLIENT)
public class ClientPacketRegistry
{
	public static void registerS2C()
	{
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.FREEZE_PACKET_ID, ((client, handler, buf, sender) -> {
			Ultracraft.freeze(null, buf.readInt());
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.HITSCAN_PACKET_ID, ((client, handler, buf, sender) -> {
			UltracraftClient.HITSCAN_HANDLER.addEntry(
					new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
					new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
					buf.readByte());
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.SERVER_OPTIONS_PACKET_ID, ((client, handler, buf, sender) -> {
			Ultracraft.FreezeOption = buf.readEnumConstant(Ultracraft.Option.class);
			Ultracraft.HiVelOption = buf.readEnumConstant(Ultracraft.Option.class);
			Ultracraft.LOGGER.info("Synced Server Options!");
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.DASH_S2C_PACKET_ID, (client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			PlayerEntity player = client.player.world.getPlayerByUuid(buf.readUuid());
			if(player == null)
				return;
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
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.RESPAWN_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			PlayerEntity player = client.player.world.getPlayerByUuid(buf.readUuid());
			if(player != null)
				((WingedPlayerEntity)player).setWingsVisible(buf.readBoolean());
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.BLEED_PACKET_ID, (((client, handler, buf, responseSender) -> {
			if(client.player == null)
				return;
			float amount = buf.readFloat();
			Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			if(client.player.getPos().squaredDistanceTo(pos) < 0.1)
				return;
			double halfheight = buf.readDouble();
			Random rand = new Random();
			for (int i = 0; i < Math.min(3 * amount, 32); i++)
				client.player.world.addParticle(new GoopDropParticleEffect(new Vec3d(0.56, 0.09, 0.01),
								0.6f + rand.nextFloat() * 0.4f * (amount / 10f)), pos.x, pos.y + halfheight, pos.z,
						rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
			//TODO: if within healing distance, add splatters to screen
		})));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.SET_HIGH_VELOCITY_S2C_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			PlayerEntity player = client.player.world.getPlayerByUuid(buf.readUuid());
			if(player != null)
				((WingedPlayerEntity)player).setWingsVisible(buf.readBoolean());
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.UPDATE_GUNCD_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player != null)
				((WingedPlayerEntity)client.player).getGunCooldownManager().tickCooldowns();
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.SET_GUNCD_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player != null)
				((WingedPlayerEntity)client.player).getGunCooldownManager().setCooldown(buf.readItemStack().getItem(), buf.readInt(), buf.readInt());
		}));
	}
}