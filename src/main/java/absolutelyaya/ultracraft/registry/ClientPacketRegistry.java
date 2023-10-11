package absolutelyaya.ultracraft.registry;

import absolutelyaya.goop.api.WaterHandling;
import absolutelyaya.goop.client.GoopClient;
import absolutelyaya.goop.particles.GoopDropParticleEffect;
import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ITrailEnjoyer;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.screen.ServerConfigScreen;
import absolutelyaya.ultracraft.client.rendering.UltraHudRenderer;
import absolutelyaya.ultracraft.compat.PlayerAnimator;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import absolutelyaya.ultracraft.recipe.UltraRecipe;
import absolutelyaya.ultracraft.recipe.UltraRecipeManager;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

import static absolutelyaya.ultracraft.registry.PacketRegistry.*;

@SuppressWarnings("CodeBlock2Expr")
@Environment(EnvType.CLIENT)
public class ClientPacketRegistry
{
	public static void registerS2C()
	{
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.FREEZE_PACKET_ID, ((client, handler, buf, sender) -> {
			int ticks = buf.readInt();
			boolean freezePhysicsDisabled = buf.readBoolean();
			if(!UltracraftClient.getConfigHolder().get().freezeVFX)
				return;
			if(!freezePhysicsDisabled)
				Ultracraft.freeze((ServerWorld)null, ticks);
			UltracraftClient.freezeVFX(ticks);
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
			PlayerEntity player = client.player.getWorld().getPlayerByUuid(buf.readUuid());
			if(player == null || player.equals(client.player))
				return;
			Vec3d dir = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			MinecraftClient.getInstance().execute(() -> {
				Random rand = client.player.getRandom();
				UltraComponents.WINGED_ENTITY.get(player).onDash();
				Vec3d pos;
				for (int i = 0; i < 5; i++)
				{
					Vec3d particleVel = new Vec3d(-dir.x, -dir.y, -dir.z).multiply(rand.nextDouble() * 0.33 + 0.1);
					pos = player.getPos().add((rand.nextDouble() - 0.5) * player.getWidth() * 2,
							rand.nextDouble() * player.getHeight() + 0.5, (rand.nextDouble() - 0.5) * player.getWidth() * 2).add(dir.multiply(0.25));
					client.player.getWorld().addParticle(ParticleRegistry.DASH, pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
				}
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.BLEED_PACKET_ID, (((client, handler, buf, responseSender) -> {
			if(client.player == null)
				return;
			float amount = buf.readFloat();
			Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			double halfheight = buf.readDouble();
			boolean shotgun = buf.readBoolean();
			boolean water = client.player.getWorld().getFluidState(new BlockPos((int)pos.x, (int)pos.y, (int)pos.z)).isIn(FluidTags.WATER);
			MinecraftClient.getInstance().execute(() -> {
				Random rand = client.player.getRandom();
				for (int i = 0; i < Math.min(3 * amount, 32); i++)
				{
					if(!water)
						client.player.getWorld().addParticle(new GoopDropParticleEffect(
										new Vec3d(0.56, 0.09, 0.01), 0.6f + rand.nextFloat() * 0.4f * (amount / 10f), true,
										WaterHandling.REPLACE_WITH_CLOUD_PARTICLE), pos.x, pos.y + halfheight, pos.z,
								rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
					else
						client.player.getWorld().addParticle(new DustParticleEffect(
										GoopClient.getConfig().censorMature ? Vec3d.unpackRgb(GoopClient.getConfig().censorColor).toVector3f() : new Vector3f(0.56f, 0.09f, 0.01f),
										3.6f + rand.nextFloat() * 0.4f * (amount / 10f)),
								pos.x + (rand.nextDouble() - 0.5) * 0.5, pos.y + halfheight + (rand.nextDouble() - 0.5) * halfheight * 1.5, pos.z + (rand.nextDouble() - 0.5) * 0.5,
								(rand.nextDouble() - 0.5) * 0.05, (rand.nextDouble() - 0.5) * 0.05, (rand.nextDouble() - 0.5) * 0.05);
				}
				if(client.player.squaredDistanceTo(pos) < 10 && !water)
				{
					UltracraftClient.addBlood(amount / (shotgun ? 10f : 30f));
					client.player.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.PLAYERS, shotgun ? 0.2f : 0.8f, 1.7f);
				}
			});
		})));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.SET_GUNCD_PACKET_ID, ((client, handler, buf, sender) -> {
			Item item = buf.readItemStack().getItem();
			int ticks = buf.readInt();
			int idx = buf.readInt();
			MinecraftClient.getInstance().execute(() -> {
				if(client.player != null && item instanceof AbstractWeaponItem weapon)
					UltraComponents.WINGED_ENTITY.get(client.player).getGunCooldownManager().setCooldown(weapon, ticks, idx);
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.CATCH_FISH_PACKET_ID, ((client, handler, buf, sender) -> {
			UltraHudRenderer.onCatchFish(buf.readItemStack());
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.SYNC_RULE_PACKET_ID, ((client, handler, buf, sender) -> {
			byte b = buf.readByte();
			UltracraftClient.syncGameRule(b, buf.readInt());
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.ENTITY_TRAIL_PACKET_ID, ((client, handler, buf, sender) -> {
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
			BlockPos pos = buf.readBlockPos();
			BlockState state = client.player.getWorld().getBlockState(pos);
			boolean strong = buf.readBoolean();
			MinecraftClient.getInstance().execute(() -> {
				Random random = client.player.getRandom();
				for (int i = 0; i < 32; i++)
				{
					float x = (float)((random.nextDouble() * 6) - 3 + player.getX());
					float z = (float)((random.nextDouble() * 6) - 3 + player.getZ());
					client.player.getWorld().addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), true,
							x, pos.up().getY() + 0.1, z, 0f, 1f, 0f);
				}
				if(strong)
					client.player.getWorld().addParticle(ParticleTypes.EXPLOSION, true,
							player.getX(), pos.up().getY() - 0.2, player.getZ(), 0f, 0f, 0f);
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.EXPLOSION_PACKET_ID, (((client, handler, buf, responseSender) -> {
			if(client.player == null)
				return;
			Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			float radius = (float)buf.readDouble();
			MinecraftClient.getInstance().execute(() -> {
				ExplosionHandler.explosionClient((ClientWorld)client.player.getWorld(),
						pos, radius);
			});
		})));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.PRIMARY_SHOT_S2C_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			PlayerEntity player = client.player.getWorld().getPlayerByUuid(buf.readUuid());
			if(player == null)
				return;
			Vec3d velocity = player.getVelocity();
			MinecraftClient.getInstance().execute(() -> {
				if(player.getMainHandStack().getItem() instanceof AbstractWeaponItem gun)
					gun.onPrimaryFire(player.getWorld(), player, velocity);
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.DEBUG_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			Vector3f pos = buf.readVector3f();
			MinecraftClient.getInstance().execute(() -> {
				client.player.getWorld().addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0f, 0f, 0f);
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.SKIM_S2C_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			Vec3d pos = new Vec3d(buf.readVector3f());
			MinecraftClient.getInstance().execute(() -> client.player.getWorld().addParticle(ParticleRegistry.RIPPLE, pos.x, pos.y, pos.z, 0, 0, 0));
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.COIN_PUNCH_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			int score = buf.readInt();
			MinecraftClient.getInstance().execute(() -> UltraHudRenderer.onPunchCoin(score));
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.WORLD_INFO_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			MinecraftClient.getInstance().execute(() -> UltracraftClient.sendJoinInfo(MinecraftClient.getInstance(), true));
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.BLOCK_PLAYER_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			UUID target = buf.readUuid();
			MinecraftClient.getInstance().execute(() -> {
				boolean b = UltracraftClient.getConfigHolder().get().blockedPlayers.contains(target);
				if(!b)
				{
					UltracraftClient.getConfigHolder().get().blockedPlayers.add(target);
					UltracraftClient.getConfigHolder().save();
				}
				client.player.sendMessage(Text.translatable("command.ultracraft.block.client-" + (b ? "fail" : "success")));
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.UNBLOCK_PLAYER_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			UUID target = buf.readUuid();
			MinecraftClient.getInstance().execute(() -> {
				boolean b = UltracraftClient.getConfigHolder().get().blockedPlayers.remove(target);
				UltracraftClient.getConfigHolder().save();
				client.player.sendMessage(Text.translatable("command.ultracraft.unblock.client-" + (b ? "success" : "fail")));
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.OPEN_SERVER_CONFIG_MENU_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			NbtCompound rules = buf.readNbt();
			MinecraftClient.getInstance().execute(() -> {
				client.setScreen(new ServerConfigScreen(rules));
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(PacketRegistry.RICOCHET_WARNING_PACKET_ID, ((client, handler, buf, sender) -> {
			if(client.player == null)
				return;
			Vector3f source = buf.readVector3f();
			UUID target = buf.readUuid();
			MinecraftClient.getInstance().execute(() -> {
				if(client.player.getUuid().equals(target))
					client.player.getWorld().addParticle(ParticleRegistry.RICOCHET_WARNING, source.x, source.y, source.z, 0, 0, 0);
				else
					client.player.getWorld().addParticle(new ParryIndicatorParticleEffect(false), source.x, source.y, source.z, 0, 0, 0);
				client.player.getWorld().playSound(source.x, source.y, source.z, SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, SoundCategory.PLAYERS, 0.75f, 1.65f, false);
			});
		}));
		ClientPlayNetworking.registerGlobalReceiver(REPLENISH_STAMINA_PACKET_ID, (client, handler, buf, sender) -> {
			int i = buf.readInt();
			client.execute(() -> {
				UltraComponents.WINGED_ENTITY.get(client.player).replenishStamina(i);
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(ANIMATION_S2C_PACKET_ID, (client, handler, buf, sender) -> {
			UUID targetID = buf.readUuid();
			AbstractClientPlayerEntity target = (AbstractClientPlayerEntity)client.player.getWorld().getPlayerByUuid(targetID);
			if(target == null || target.equals(client.player))
				return;
			int animID = buf.readInt();
			int fade = buf.readInt();
			boolean firstperson = buf.readBoolean();
			client.execute(() -> {
				PlayerAnimator.playAnimation(target, animID, fade, firstperson, true);
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(SOAP_KILL_PACKET_ID, (((client, handler, buf, responseSender) -> {
			if(client.player == null)
				return;
			Vec3d pos = new Vec3d(buf.readVector3f());
			double width = buf.readDouble();
			double height = buf.readDouble();
			MinecraftClient.getInstance().execute(() -> {
				Random rand = client.player.getRandom();
				for (int i = 0; i < 24; i++)
				{
					Vec3d pos1 = pos.add((rand.nextFloat() - 0.5f) * width, (rand.nextFloat() - 0.5f) * height, (rand.nextFloat() - 0.5f) * width);
					Vec3d vel = Vec3d.ZERO.addRandom(rand, 0.2f);
					client.player.getWorld().addParticle(ParticleRegistry.SOAP_BUBBLE, pos1.x, pos1.y, pos1.z, vel.x, vel.y, vel.z);
				}
			});
		})));
		ClientPlayNetworking.registerGlobalReceiver(ULTRA_RECIPE_PACKET_ID, (((client, handler, buf, responseSender) -> {
			List<Pair<Identifier, UltraRecipe>> list = buf.readList(UltraRecipe::deserialize);
			ImmutableMap.Builder<Identifier, UltraRecipe> builder = ImmutableMap.builder();
			for(Pair<Identifier, UltraRecipe> pair : list)
				builder.put(pair.getLeft(), pair.getRight());
			UltraRecipeManager.setRecipes(builder.build());
		})));
		ClientPlayNetworking.registerGlobalReceiver(HIVEL_WHITELIST_PACKET_ID, (((client, handler, buf, responseSender) -> {
			MinecraftClient.getInstance().execute(UltraHudRenderer::onWhitelistHint);
		})));
	}
}
