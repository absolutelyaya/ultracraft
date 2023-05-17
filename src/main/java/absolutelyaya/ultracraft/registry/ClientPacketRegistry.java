package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ITrailEnjoyer;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.rendering.UltraHudRenderer;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.particle.goop.GoopDropParticleEffect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

@SuppressWarnings("CodeBlock2Expr")
@Environment(EnvType.CLIENT)
public class ClientPacketRegistry
{
	public static void registerS2C()
	{
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.FREEZE_PACKET_ID, ((client, handler, buf, sender) -> {
			if(UltracraftClient.isFreezeEnabled())
				Ultracraft.freeze(null, buf.readInt());
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.HITSCAN_PACKET_ID, ((client, handler, buf, sender) -> {
			UltracraftClient.HITSCAN_HANDLER.addEntry(
					new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
					new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
					buf.readByte());
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.DASH_S2C_PACKET_ID, (client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			PlayerEntity player = client.player.world.getPlayerByUuid(buf.readUuid());
			if(player == null)
				return;
			Vec3d dir = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			MinecraftClient.getInstance().execute(() -> {
				Random rand = client.player.getRandom();
				((WingedPlayerEntity)player).onDash();
				Vec3d pos;
				for (int i = 0; i < 5; i++)
				{
					Vec3d particleVel = new Vec3d(-dir.x, -dir.y, -dir.z).multiply(rand.nextDouble() * 0.33 + 0.1);
					pos = player.getPos().add((rand.nextDouble() - 0.5) * player.getWidth() * 2,
							rand.nextDouble() * player.getHeight() + 0.5, (rand.nextDouble() - 0.5) * player.getWidth() * 2).add(dir.multiply(0.25));
					client.player.world.addParticle(ParticleRegistry.DASH, pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
				}
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.BLEED_PACKET_ID, (((client, handler, buf, responseSender) -> {
			if(client.player == null)
				return;
			float amount = buf.readFloat();
			Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			if(client.player.getPos().squaredDistanceTo(pos) < 0.1)
				return;
			double halfheight = buf.readDouble();
			boolean shotgun = buf.readBoolean();
			MinecraftClient.getInstance().execute(() -> {
				Random rand = client.player.getRandom();
				for (int i = 0; i < Math.min(3 * amount, 32); i++)
					client.player.world.addParticle(new GoopDropParticleEffect(
									UltracraftClient.getConfigHolder().get().danganronpa ? new Vec3d(1.0, 0.32, 0.83) : new Vec3d(0.56, 0.09, 0.01),
									0.6f + rand.nextFloat() * 0.4f * (amount / 10f)), pos.x, pos.y + halfheight, pos.z,
							rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
				if(client.player.squaredDistanceTo(pos) < 10)
					UltracraftClient.addBlood(amount / (shotgun ? 10f : 30f));
			});
		})));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.SET_HIGH_VELOCITY_S2C_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			PlayerEntity player = client.player.world.getPlayerByUuid(buf.readUuid());
			if(player == null)
				return;
			boolean b = buf.readBoolean();
			MinecraftClient.getInstance().execute(() -> {
				((WingedPlayerEntity)player).setWingsVisible(b);
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.SET_GUNCD_PACKET_ID, ((client, handler, buf, sender) -> {
			Item item = buf.readItemStack().getItem();
			int ticks = buf.readInt();
			int idx = buf.readInt();
			MinecraftClient.getInstance().execute(() -> {
				if(client.player != null)
					((WingedPlayerEntity)client.player).getGunCooldownManager().setCooldown(item, ticks, idx);
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.CATCH_FISH_ID, ((client, handler, buf, sender) -> {
			UltraHudRenderer.onCatchFish(buf.readItemStack());
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.SYNC_RULE, ((client, handler, buf, sender) -> {
			byte b = buf.readByte();
			if(b == 40)
				UltracraftClient.syncGameRule(b, buf.readInt());
			else
				UltracraftClient.syncGameRule(b);
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.ENTITY_TRAIL, ((client, handler, buf, sender) -> {
			Entity e = client.world.getEntityById(buf.readInt());
			boolean b = buf.readBoolean();
			int data = buf.readInt();
			if(e instanceof ITrailEnjoyer trailer)
			{
				MinecraftClient.getInstance().execute(() -> {
					if(b)
						trailer.addEntityTrail(data);
					else
						UltracraftClient.TRAIL_RENDERER.removeTrail(trailer.getLastTrailID());
				});
			}
			else
				Ultracraft.LOGGER.warn("Received invalid Packet data: [entity_trail] -> Target entity isn't a TrailEnjoyer!" );
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.GROUND_POUND_S2C_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			PlayerEntity player = client.world.getPlayerByUuid(buf.readUuid());
			if(player == null)
				return;
			MinecraftClient.getInstance().execute(() -> {
				Random random = client.player.getRandom();
				for (int i = 0; i < 32; i++)
				{
					float x = (float)((random.nextDouble() * 6) - 3 + player.getX());
					float z = (float)((random.nextDouble() * 6) - 3 + player.getZ());
					client.player.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, player.getSteppingBlockState()),
							x, player.getY() + 0.1, z, 0f, 1f, 0f);
				}
				client.player.world.addBlockBreakParticles(player.getSteppingPos(), player.getSteppingBlockState());
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.EXPLOSION_PACKET_ID, (((client, handler, buf, responseSender) -> {
			if(client.player == null)
				return;
			Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			float radius = (float)buf.readDouble();
			MinecraftClient.getInstance().execute(() -> {
				ExplosionHandler.explosionClient((ClientWorld)client.player.world,
						pos, radius);
			});
		})));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.PRIMARY_SHOT_PACKET_ID_S2C, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			PlayerEntity player = client.player.world.getPlayerByUuid(buf.readUuid());
			if(player == null)
				return;
			MinecraftClient.getInstance().execute(() -> {
				if(player.getMainHandStack().getItem() instanceof AbstractWeaponItem gun)
					gun.onPrimaryFire(player.world, player);
			});
		}));
	}
}
