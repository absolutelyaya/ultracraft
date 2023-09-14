package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.*;
import absolutelyaya.ultracraft.block.IPunchableBlock;
import absolutelyaya.ultracraft.block.PedestalBlock;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.projectile.ThrownCoinEntity;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.item.SoapItem;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import net.bettercombat.utils.MathHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
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
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PacketRegistry
{
	public static final Identifier PUNCH_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "parry");
	public static final Identifier PUNCH_BLOCK_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "punch_block");
	public static final Identifier PRIMARY_SHOT_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "primary_shot_c2s");
	public static final Identifier SEND_WING_STATE_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "set_winged_state_c2s");
	public static final Identifier SEND_WINGED_DATA_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "set_winged_data_c2s");
	public static final Identifier DASH_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "dash_c2s");
	public static final Identifier GROUND_POUND_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "ground_pound_c2s");
	public static final Identifier REQUEST_WINGED_DATA_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "request_wing_data");
	public static final Identifier SKIM_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "skim_c2s");
	public static final Identifier THROW_COIN_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "throw_coin");
	public static final Identifier LOCK_PEDESTAL_ID = new Identifier(Ultracraft.MOD_ID, "lock_pedestal");
	public static final Identifier ANIMATION_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "animation_c2s");
	public static final Identifier KILLER_FISH_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "killerfish");
	public static final Identifier TERMINAL_SYNC_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "terminal_c2s");
	public static final Identifier GRAFFITI_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "graffiti_c2s");
	public static final Identifier TERMINAL_REDSTONE_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "terminal_redstone");
	
	public static final Identifier FREEZE_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "freeze");
	public static final Identifier HITSCAN_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "hitscan");
	public static final Identifier DASH_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "dash_s2c");
	public static final Identifier BLEED_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "bleed");
	public static final Identifier WING_STATE_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "wing_state_s2c");
	public static final Identifier WING_DATA_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "wing_data_s2c");
	public static final Identifier SET_GUNCD_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "set_gcd");
	public static final Identifier CATCH_FISH_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "fish");
	public static final Identifier SYNC_RULE_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "sync_rule");
	public static final Identifier ENTITY_TRAIL_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "entity_trail");
	public static final Identifier GROUND_POUND_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "ground_pound_s2c");
	public static final Identifier EXPLOSION_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "explosion");
	public static final Identifier PRIMARY_SHOT_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "primary_shot_s2c");
	public static final Identifier DEBUG_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "debug");
	public static final Identifier SKIM_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "skim_s2c");
	public static final Identifier COIN_PUNCH_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "coinpunch");
	public static final Identifier WORLD_INFO_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "world-info");
	public static final Identifier BLOCK_PLAYER_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "block");
	public static final Identifier UNBLOCK_PLAYER_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "unblock");
	public static final Identifier OPEN_SERVER_CONFIG_MENU_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "server_config");
	public static final Identifier RICOCHET_WARNING_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "warn_ricochet");
	public static final Identifier REPLENISH_STAMINA_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "replenish_stamina");
	public static final Identifier ANIMATION_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "animation_s2c");
	public static final Identifier SOAP_KILL_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "soapkill");
	
	public static void registerC2S()
	{
		ServerPlayNetworking.registerGlobalReceiver(PUNCH_PACKET_ID, (server, player, handler, buf, sender) -> {
			World world = player.getWorld();
			Entity target;
			if(buf.readBoolean())
				target = world.getEntityById(buf.readInt());
			else
				target = null;
			Vector3f clientVel = buf.readVector3f(); //velocity the player has on the client
			boolean debug = buf.readBoolean();
			
			server.execute(() -> {
				Vec3d forward = player.getRotationVector().normalize();
				player.swingHand(Hand.OFF_HAND, true);
				
				if(player.getOffHandStack().getItem() instanceof SoapItem soap)
				{
					soap.onOffhandThrow(world, player);
					return;
				}
				
				//Punch Entity; Takes Priority over Projectile Parries
				if(target != null)
				{
					if(player.getOffHandStack().isIn(TagRegistry.PUNCH_FLAMES))
						target.setFireTicks(100);
					if (target instanceof MeleeInterruptable mp && (!(mp instanceof MobEntity) || ((MobEntity)mp).isAttacking()))
					{
						Ultracraft.freeze(player, 10);
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
				//Fetch all Parry Candidate Projectiles
				boolean chainingAllowed = world.getGameRules().getBoolean(GameruleRegistry.PARRY_CHAINING);
				Vec3d pos = player.getEyePos();
				Box check = new Box(pos.x - 0.3f, pos.y - 0.3f, pos.z - 0.3f,
						pos.x + 0.3f, pos.y + 0.3f, pos.z + 0.3f)
									.stretch(forward.multiply(0.9)).offset(new Vec3d(clientVel.mul(-0.5f)))
									.stretch(clientVel.x * 16, clientVel.y * 16, clientVel.z * 16);
				//Get Projectiles that absolutely are in the Parry Check
				List<ProjectileEntity> projectiles = player.getWorld().getEntitiesByClass(ProjectileEntity.class, check,
						e -> (!((ProjectileEntityAccessor)e).isParried()) || chainingAllowed);
				//Get Projectiles that could move into the Parry Check
				List<ProjectileEntity> potentialProjectiles = player.getWorld().getEntitiesByClass(ProjectileEntity.class, player.getBoundingBox().expand(4),
						e -> (!((ProjectileEntityAccessor)e).isParried() || chainingAllowed) && !projectiles.contains(e));
				for (ProjectileEntity proj : potentialProjectiles)
				{
					//Predict position within the last 1 and next 3 ticks. If it is or was within the parry check, then the parry is successful
					//This is mainly intended for combatting latency and shit
					for (int i = 0; i < 4; i++)
					{
						Vec3d predictedPos = proj.getLerpedPos(i);
						if (check.contains(predictedPos))
						{
							if(debug)
								addDebugParticle(player, predictedPos);
							projectiles.add(proj);
							break;
						}
					}
				}
				
				if(debug)
				{
					addDebugParticle(player, new Vec3d(check.minX, check.minY, check.minZ));
					addDebugParticle(player, new Vec3d(check.maxX, check.minY, check.minZ));
					addDebugParticle(player, new Vec3d(check.minX, check.minY, check.maxZ));
					addDebugParticle(player, new Vec3d(check.maxX, check.minY, check.maxZ));
					addDebugParticle(player, new Vec3d(check.minX, check.maxY, check.minZ));
					addDebugParticle(player, new Vec3d(check.maxX, check.maxY, check.minZ));
					addDebugParticle(player, new Vec3d(check.minX, check.maxY, check.maxZ));
					addDebugParticle(player, new Vec3d(check.maxX, check.maxY, check.maxZ));
				}
				
				//The actual Parry Logic
				ProjectileEntity parried;
				if(projectiles.size() > 0)
					parried = getNearestProjectile(projectiles, pos);
				else
					return;
				if(!((ProjectileEntityAccessor)parried).isParriable())
					return;
				boolean heal = true;
				if(player.equals(parried.getOwner()) && parried.age < 4)
				{
					if(((ProjectileEntityAccessor)parried).isBoostable())
					{
						heal = false;
						Ultracraft.freeze(player, 5); //ProjBoost freezes are shorter
					}
					else
						return;
				}
				else if(!(parried instanceof ThrownCoinEntity))
					Ultracraft.freeze(player, 10);
				world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.75f, 2f);
				ProjectileEntityAccessor pa = (ProjectileEntityAccessor)parried;
				pa.setParried(true, player);
				parried.setVelocity(forward.multiply(chainingAllowed ? 2f + 0.2f * ((ChainParryAccessor)pa).getParryCount() : 2.5f));
				if(heal && !(parried instanceof ThrownCoinEntity))
					player.heal(6f);
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
					if(state.getBlock() instanceof IPunchableBlock punchable && !(mainHand && player.getMainHandStack().isOf(Items.DEBUG_STICK)))
						punchable.onPunch(player, target, mainHand);
					if(state.getBlock() instanceof BellBlock bell)
						bell.ring(player, player.getWorld(), target, player.getHorizontalFacing().getOpposite());
					if(state.isIn(TagRegistry.PUNCH_BREAKABLE) && player.canModifyAt(world, target))
						player.getWorld().breakBlock(target, true, player);
				}
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(PRIMARY_SHOT_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			byte action = buf.readByte();
			Vec3d velocity = action > 0 ? new Vec3d(buf.readVector3f()) : Vec3d.ZERO;
			server.execute(() -> {
				if (player.getMainHandStack().getItem() instanceof AbstractWeaponItem gun)
				{
					((WingedPlayerEntity)player).setPrimaryFiring(action > 0);
					if (action == 0 || !gun.onPrimaryFire(player.getWorld(), player, velocity))
						return;
					for (ServerPlayerEntity p : ((ServerWorld)player.getWorld()).getPlayers(p -> player.distanceTo(p) < 128f))
					{
						PacketByteBuf cbuf = new PacketByteBuf(Unpooled.buffer());
						cbuf.writeUuid(player.getUuid());
						ServerPlayNetworking.send(p, PRIMARY_SHOT_S2C_PACKET_ID, cbuf);
					}
				}
				else if(action == 0)
					((WingedPlayerEntity)player).setPrimaryFiring(false);
				else
					Ultracraft.LOGGER.warn(player + " tried to use primary fire action but is holding a non-weapon Item!");
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(SEND_WING_STATE_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			WingedPlayerEntity winged = (WingedPlayerEntity)player;
			if(winged == null)
				return;
			boolean wingsActive = buf.readBoolean();
			server.execute(() -> winged.setWingsVisible(wingsActive));
			for (ServerPlayerEntity p : ((ServerWorld)player.getWorld()).getPlayers())
			{
				buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeUuid(player.getUuid());
				buf.writeBoolean(wingsActive);
				ServerPlayNetworking.send(p, WING_STATE_S2C_PACKET_ID, buf);
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(SEND_WINGED_DATA_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			WingedPlayerEntity winged = (WingedPlayerEntity)player;
			if(winged == null)
				return;
			boolean wingsActive = buf.readBoolean();
			Vector3f wingColor = buf.readVector3f(), metalColor = buf.readVector3f();
			String pattern = Ultracraft.checkSupporter(player.getUuid(), false) ? buf.readString() : "";
			server.execute(() -> {
				winged.setWingsVisible(wingsActive);
				winged.setWingColor(new Vec3d(wingColor), 0);
				winged.setWingColor(new Vec3d(metalColor), 1);
				winged.setWingPattern(pattern);
			});
			for (ServerPlayerEntity p : ((ServerWorld)player.getWorld()).getPlayers())
			{
				buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeUuid(player.getUuid());
				buf.writeBoolean(wingsActive);
				buf.writeVector3f(wingColor);
				buf.writeVector3f(metalColor);
				buf.writeString(pattern);
				ServerPlayNetworking.send(p, WING_DATA_S2C_PACKET_ID, buf);
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(DASH_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			Vec3d dir = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeUuid(player.getUuid());
			buf.writeDouble(dir.x);
			buf.writeDouble(dir.y);
			buf.writeDouble(dir.z);
			for (ServerPlayerEntity p : ((ServerWorld)player.getWorld()).getPlayers())
				ServerPlayNetworking.send(p, DASH_S2C_PACKET_ID, buf);
			server.execute(() -> ((WingedPlayerEntity)player).onDash());
		});
		ServerPlayNetworking.registerGlobalReceiver(GROUND_POUND_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			boolean start = buf.readBoolean();
			boolean strong = buf.readBoolean();
			server.execute(() -> {
				WingedPlayerEntity winged = (WingedPlayerEntity)player;
				if(start)
					winged.startSlam();
				else
					winged.endSlam(strong);
				if(start)
					return;
				PacketByteBuf cbuf = new PacketByteBuf(Unpooled.buffer());
				cbuf.writeUuid(player.getUuid());
				cbuf.writeBlockPos(player.getSteppingPos());
				cbuf.writeBoolean(strong);
				for (ServerPlayerEntity p : ((ServerWorld)player.getWorld()).getPlayers())
					ServerPlayNetworking.send(p, GROUND_POUND_S2C_PACKET_ID, cbuf);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(REQUEST_WINGED_DATA_PACKET_ID, (server, player, handler, buf, sender) -> {
			UUID targetID = buf.readUuid();
			PlayerEntity target = server.getPlayerManager().getPlayer(targetID);
			if(target == null)
				return;
			WingedPlayerEntity winged = (WingedPlayerEntity)target;
			buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeUuid(targetID);
			buf.writeBoolean(winged.isWingsActive());
			buf.writeVector3f(winged.getWingColors()[0].toVector3f());
			buf.writeVector3f(winged.getWingColors()[1].toVector3f());
			buf.writeString(winged.getWingPattern());
			ServerPlayNetworking.send(player, WING_DATA_S2C_PACKET_ID, buf);
		});
		ServerPlayNetworking.registerGlobalReceiver(SKIM_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			if(player == null)
				return;
			Vec3d pos = new Vec3d(buf.readVector3f());
			PacketByteBuf cbuf = new PacketByteBuf(Unpooled.buffer());
			cbuf.writeVector3f(pos.toVector3f());
			player.getWorld().getPlayers().forEach(p -> {
				if(player.squaredDistanceTo(p) < 32f * 32f)
					ServerPlayNetworking.send((ServerPlayerEntity)p, SKIM_S2C_PACKET_ID, cbuf);
			});
			server.execute(() -> {
				player.playSound(SoundEvents.ENTITY_SALMON_FLOP, SoundCategory.PLAYERS, 1f, 0.8f + player.getRandom().nextFloat() * 0.4f);
				player.getWorld().addParticle(ParticleRegistry.RIPPLE, pos.x, pos.y, pos.z, 0, 0, 0);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(THROW_COIN_PACKET_ID, (server, player, handler, buf, sender) -> {
			if(player == null)
				return;
			Vec3d pos = new Vec3d(buf.readVector3f());
			Vec3d vel = new Vec3d(buf.readVector3f());
			boolean justJumped = buf.readBoolean();
			server.execute(() -> {
				ThrownCoinEntity coin = ThrownCoinEntity.spawn(player, player.getWorld());
				coin.setPos(pos.x, pos.y, pos.z);
				coin.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 0.5f, 0f);
				coin.addVelocity(vel.multiply(1f, justJumped ? 0.25f : 0.75f, 1f));
				coin.addVelocity(0f, 0.3f, 0f);
				coin.setPosition(coin.getPos().add(vel));
				player.getWorld().spawnEntity(coin);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(LOCK_PEDESTAL_ID, (server, player, handler, buf, sender) -> {
			BlockPos pos = buf.readBlockPos();
			Boolean b = buf.readBoolean();
			server.execute(() -> {
				if(player.getWorld().getBlockState(pos).isOf(BlockRegistry.PEDESTAL))
					player.getWorld().setBlockState(pos, player.getWorld().getBlockState(pos).with(PedestalBlock.LOCKED, b));
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(ANIMATION_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			PacketByteBuf cbuf = new PacketByteBuf(Unpooled.buffer());
			cbuf.writeUuid(player.getUuid()); //target
			cbuf.writeInt(buf.readInt()); //animID
			cbuf.writeInt(buf.readInt()); //fade
			cbuf.writeBoolean(buf.readBoolean()); //firstPerson
			player.getWorld().getPlayers().forEach(p -> {
				if(p != player)
					ServerPlayNetworking.send((ServerPlayerEntity)p, ANIMATION_S2C_PACKET_ID, cbuf);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(KILLER_FISH_PACKET_ID, (server, player, handler, buf, sender) -> {
			server.execute(() -> player.getWorld().playSound(null, player.getBlockPos(),
					SoundRegistry.KILLERFISH_SELECT.value(), SoundCategory.PLAYERS, 1f, 1f));
		});
		ServerPlayNetworking.registerGlobalReceiver(TERMINAL_SYNC_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			BlockPos pos = buf.readBlockPos();
			int textColor = buf.readInt();
			int base = buf.readInt();
			NbtCompound screenSaver = buf.readNbt();
			NbtCompound mainMenu = buf.readNbt();
			server.execute(() ->  {
				BlockEntity be = player.getWorld().getBlockEntity(pos);
				if(be instanceof TerminalBlockEntity terminal)
					terminal.applyCustomization(textColor, base, screenSaver, mainMenu);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(GRAFFITI_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			BlockPos pos = buf.readBlockPos();
			int[] palette = buf.readIntArray(15);
			byte[] pixels = buf.readByteArray();
			int revision = buf.readInt();
			server.execute(() -> {
				BlockEntity be = player.getWorld().getBlockEntity(pos);
				if(be instanceof TerminalBlockEntity terminal)
				{
					terminal.setPalette(Arrays.asList(ArrayUtils.toObject(palette)));
					terminal.setGraffiti(ByteArrayList.of(pixels));
					terminal.setGraffitiRevision(revision);
				}
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(TERMINAL_REDSTONE_PACKET_ID, (server, player, handler, buf, sender) -> {
			BlockPos pos = buf.readBlockPos();
			int strength = buf.readInt();
			server.execute(() -> {
				BlockEntity be = player.getWorld().getBlockEntity(pos);
				if(be instanceof TerminalBlockEntity terminal)
					terminal.redstoneImpulse((int)MathHelper.clamp(strength, 0, 15));
			});
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
	
	static void addDebugParticle(ServerPlayerEntity p, Vec3d pos)
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeVector3f(pos.toVector3f());
		ServerPlayNetworking.send(p, DEBUG_PACKET_ID, buf);
	}
}
