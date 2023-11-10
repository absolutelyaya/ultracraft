package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.*;
import absolutelyaya.ultracraft.block.HellObserverBlockEntity;
import absolutelyaya.ultracraft.block.IPunchableBlock;
import absolutelyaya.ultracraft.block.PedestalBlock;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.components.level.IUltraLevelComponent;
import absolutelyaya.ultracraft.components.player.IArmComponent;
import absolutelyaya.ultracraft.components.player.IWingDataComponent;
import absolutelyaya.ultracraft.components.player.IWingedPlayerComponent;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.projectile.AbstractSkewerEntity;
import absolutelyaya.ultracraft.entity.projectile.ThrownCoinEntity;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.item.SoapItem;
import absolutelyaya.ultracraft.recipe.UltraRecipeManager;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
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
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
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
	public static final Identifier SEND_WING_DATA_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "set_winged_data_c2s");
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
	public static final Identifier TERMINAL_WEAPON_CRAFT_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "terminal_weapon_craft");
	public static final Identifier TERMINAL_WEAPON_DISPENSE_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "terminal_weapon_dispense");
	public static final Identifier CYCLE_WEAPON_VARIANT_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "cycle_weapon_variant");
	public static final Identifier HELL_OBSERVER_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "hell_observer_c2s");
	public static final Identifier REQUEST_GRAFFITI_WHITELIST_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "request_graffiti_whitelist");
	public static final Identifier ARM_CYCLE_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "arm_cycle");
	public static final Identifier ARM_VISIBLE_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "arm_visible");
	public static final Identifier PUNCH_PRESSED_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "punch_pressed");
	
	public static final Identifier FREEZE_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "freeze");
	public static final Identifier HITSCAN_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "scan");
	public static final Identifier DASH_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "dash_s2c");
	public static final Identifier BLEED_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "bleed");
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
	public static final Identifier WORLD_INFO_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "world_info");
	public static final Identifier BLOCK_PLAYER_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "block");
	public static final Identifier UNBLOCK_PLAYER_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "unblock");
	public static final Identifier OPEN_SERVER_CONFIG_MENU_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "server_config");
	public static final Identifier RICOCHET_WARNING_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "warn_ricochet");
	public static final Identifier REPLENISH_STAMINA_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "replenish_stamina");
	public static final Identifier ANIMATION_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "animation_s2c");
	public static final Identifier SOAP_KILL_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "soapkill");
	public static final Identifier ULTRA_RECIPE_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "ultra_recipe");
	public static final Identifier HIVEL_WHITELIST_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "hivel_whitelist");
	public static final Identifier GRAFFITI_WHITELIST_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "graffiti_whitelist");
	public static final Identifier HELL_OBSERVER_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "hell_observer");
	
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
				if(player instanceof LivingEntityAccessor accessor)
					accessor.punch();
				Vec3d forward = player.getRotationVector().normalize();
				player.swingHand(Hand.OFF_HAND, true);
				IArmComponent arm = UltraComponents.ARMS.get(player);
				
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
					if (arm.isFeedbacker() && target instanceof MeleeInterruptable mp && (!(mp instanceof MobEntity) || ((MobEntity)mp).isAttacking()))
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
						boolean knuckle = arm.isKnuckleblaster();
						target.damage(DamageSources.get(world, knuckle ? DamageSources.KNUCKLE_PUNCH : DamageSources.PUNCH, player), knuckle ? 2.5f : 1f);
						//TODO: make punch damage configurable
					}
					boolean fatal = !target.isAlive();
					Vec3d vel = forward.multiply(fatal ? 1.5f : 0.75f);
					if(arm.isKnuckleblaster())
						vel = vel.multiply(1.5f);
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
					Vec3d vel = proj.getVelocity();
					if (check.intersects(proj.getBoundingBox().expand(vel.x, vel.y, vel.z)))
					{
						projectiles.add(proj);
						break;
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
				boolean feedbacker = arm.isFeedbacker();
				if(parried instanceof AbstractSkewerEntity skewer)
				{
					skewer.damage(DamageSources.get(world, feedbacker ? DamageSources.PUNCH : DamageSources.KNUCKLE_PUNCH), 1f);
					return;
				}
				if(!arm.isFeedbacker())
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
				player.incrementStat(StatisticRegistry.PARRY);
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
				IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(player);
				if (player.getMainHandStack().getItem() instanceof AbstractWeaponItem gun)
				{
					winged.setPrimaryFiring(action > 0);
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
					winged.setPrimaryFiring(false);
				else
					Ultracraft.LOGGER.warn(player + " tried to use primary fire action but is holding a non-weapon Item!");
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(SEND_WING_STATE_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			IWingDataComponent wings = UltraComponents.WING_DATA.get(player);
			IUltraLevelComponent level = UltraComponents.GLOBAL.get(player.getWorld().getLevelProperties());
			boolean whitelisted = level.isPlayerAllowedToHivel(player);
			boolean wingsActive = buf.readBoolean() && whitelisted;
			server.execute(() ->
			{
				wings.setVisible(wingsActive);
				wings.sync();
				if(whitelisted)
					return;
				PacketByteBuf cbuf = new PacketByteBuf(Unpooled.buffer());
				ServerPlayNetworking.send(player, PacketRegistry.HIVEL_WHITELIST_PACKET_ID, cbuf);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(SEND_WING_DATA_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			IWingDataComponent wings = UltraComponents.WING_DATA.get(player);
			if(wings == null)
				return;
			boolean wingsActive = buf.readBoolean();
			Vector3f wingColor = buf.readVector3f(), metalColor = buf.readVector3f();
			String pattern = Ultracraft.checkSupporter(player.getUuid(), false) ? buf.readString() : "";
			server.execute(() -> {
				wings.setVisible(wingsActive);
				wings.setColor(wingColor, 0);
				wings.setColor(metalColor, 1);
				wings.setPattern(pattern);
				wings.sync();
			});
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
			server.execute(() -> {
				UltraComponents.WINGED_ENTITY.get(player).onDash();
				player.incrementStat(StatisticRegistry.DASH);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(GROUND_POUND_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			boolean start = buf.readBoolean();
			boolean strong = buf.readBoolean();
			server.execute(() -> {
				WingedPlayerEntity winged = (WingedPlayerEntity)player;
				if(start)
					winged.startSlam();
				else
				{
					winged.endSlam(strong);
					player.incrementStat(StatisticRegistry.SLAM);
				}
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
			UltraComponents.WING_DATA.get(player).sync();
			//buf = new PacketByteBuf(Unpooled.buffer());
			//buf.writeUuid(targetID);
			//buf.writeBoolean(wings.isVisible());
			//buf.writeVector3f(wings.getColors()[0]);
			//buf.writeVector3f(wings.getColors()[1]);
			//buf.writeString(wings.getPattern());
			//ServerPlayNetworking.send(player, WING_DATA_S2C_PACKET_ID, buf);
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
					SoundRegistry.KILLERFISH_SELECT, SoundCategory.PLAYERS, 1f, 1f));
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
					terminal.redstoneImpulse(MathHelper.clamp(strength, 0, 15));
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(TERMINAL_WEAPON_CRAFT_PACKET_ID, (server, player, handler, buf, sender) -> {
			Identifier recipe = buf.readIdentifier();
			server.execute(() -> UltraRecipeManager.getRecipe(recipe).craft(player));
		});
		ServerPlayNetworking.registerGlobalReceiver(TERMINAL_WEAPON_DISPENSE_PACKET_ID, (server, player, handler, buf, sender) -> {
			Identifier weapon = buf.readIdentifier();
			server.execute(() ->
			{
				if(UltraComponents.PROGRESSION.get(player).isOwned(weapon))
					player.giveItemStack(Registries.ITEM.get(weapon).getDefaultStack());
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(CYCLE_WEAPON_VARIANT_PACKET_ID, (server, player, handler, buf, sender) -> {
			server.execute(() -> AbstractWeaponItem.cycleVariant(player));
		});
		ServerPlayNetworking.registerGlobalReceiver(HELL_OBSERVER_C2S_PACKET_ID, (server, player, handler, buf, sender) -> {
			BlockPos pos = buf.readBlockPos();
			int playerCount = buf.readInt();
			int playerOperator = buf.readInt();
			int enemyCount = buf.readInt();
			int enemyOperator = buf.readInt();
			boolean requireBoth = buf.readBoolean();
			Vector3f offset = buf.readVector3f();
			Vector3f size = buf.readVector3f();
			boolean previewArea = buf.readBoolean();
			server.execute(() -> {
				if(!(player.getWorld().getBlockEntity(pos) instanceof HellObserverBlockEntity observer))
					return;
				observer.sync(playerCount, playerOperator, enemyCount, enemyOperator, requireBoth,
						new Vec3i((int)offset.x, (int)offset.y, (int)offset.z), new Vec3i((int)size.x, (int)size.y, (int)size.z), previewArea);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(REQUEST_GRAFFITI_WHITELIST_PACKET_ID, (server, player, handler, buf, sender) -> {
			server.execute(() ->
			{
				PacketByteBuf cbuf = new PacketByteBuf(Unpooled.buffer());
				cbuf.writeBoolean(UltraComponents.GLOBAL.get(player.getWorld().getLevelProperties()).isPlayerAllowedToGraffiti(player));
				ServerPlayNetworking.send(player, GRAFFITI_WHITELIST_PACKET_ID, cbuf);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(ARM_CYCLE_PACKET_ID, (server, player, handler, buf, sender) -> {
			server.execute(() -> UltraComponents.ARMS.get(player).cycleArms());
		});
		ServerPlayNetworking.registerGlobalReceiver(ARM_VISIBLE_PACKET_ID, (server, player, handler, buf, sender) -> {
			boolean v = buf.readBoolean();
			server.execute(() -> UltraComponents.ARMS.get(player).setArmVisible(v));
		});
		ServerPlayNetworking.registerGlobalReceiver(PUNCH_PRESSED_PACKET_ID, (server, player, handler, buf, sender) -> {
			boolean v = buf.readBoolean();
			server.execute(() -> UltraComponents.ARMS.get(player).setPunchPressed(v));
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
