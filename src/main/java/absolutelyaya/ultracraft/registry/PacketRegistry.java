package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.MeleeInterruptable;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.IPunchableBlock;
import absolutelyaya.ultracraft.entity.projectile.ShotgunPelletEntity;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PacketRegistry
{
	public static final Identifier PUNCH_ENTITY_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "punch_entiy");
	public static final Identifier PUNCH_BLOCK_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "punch_block");
	public static final Identifier PRIMARY_SHOT_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "primary_shot");
	public static final Identifier SET_HIGH_VELOCITY_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "sethivel_c2s");
	public static final Identifier DASH_C2S_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "dash_c2s");
	public static final Identifier GROUND_POUND_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "ground_pound");
	
	public static final Identifier FREEZE_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "freeze");
	public static final Identifier HITSCAN_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "hitscan");
	public static final Identifier DASH_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "dash_s2c");
	public static final Identifier BLEED_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "bleed");
	public static final Identifier SET_HIGH_VELOCITY_S2C_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "sethivel_s2c");
	public static final Identifier UPDATE_GUNCD_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "update_gcd");
	public static final Identifier SET_GUNCD_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "set_gcd");
	public static final Identifier CATCH_FISH_ID = new Identifier(Ultracraft.MOD_ID, "fish");
	public static final Identifier SYNC_RULE = new Identifier(Ultracraft.MOD_ID, "sync_rule");
	public static final Identifier ENTITY_TRAIL = new Identifier(Ultracraft.MOD_ID, "entity_trail");
	
	public static void registerC2S()
	{
		ServerPlayNetworking.registerGlobalReceiver(PUNCH_ENTITY_PACKET_ID, (server, player, handler, buf, sender) -> {
			World world = player.getWorld();
			Entity target = world.getEntityById(buf.readInt());
			boolean flame = buf.readBoolean();
			
			if(target != null)
			{
				if(flame)
					target.setFireTicks(100);
				if(target instanceof ProjectileEntity p)
				{
					if(!((ProjectileEntityAccessor)p).isParriable())
						return;
					if(player.equals(p.getOwner()) && p instanceof ThrownItemEntity thrown)
					{
						if(player.world.getGameRules().getBoolean(GameruleRegistry.ALLOW_PROJ_BOOST_THROWABLE) || thrown instanceof ShotgunPelletEntity)
						{
							Ultracraft.freeze((ServerWorld) player.world, 5);
							world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.75f, 2f);
							ProjectileEntityAccessor pa = (ProjectileEntityAccessor)p;
							pa.setParried(true, player);
						}
						else
							return;
					}
					else
					{
						Ultracraft.freeze((ServerWorld) player.world, 10);
						world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.75f, 2f);
						ProjectileEntityAccessor pa = (ProjectileEntityAccessor)p;
						pa.setParried(true, player);
					}
				}
				else if (target instanceof MeleeInterruptable mp && (!(mp instanceof MobEntity) || ((MobEntity)mp).isAttacking()))
				{
					Ultracraft.freeze((ServerWorld) player.world, 10);
					target.damage(DamageSources.getInterrupted(player), 15);
					mp.onInterrupt(player);
					world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.75f, 2f);
					player.heal(4);
				}
				else
				{
					world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 0.75f, 0.5f);
					target.damage(DamageSource.mob(player), 4);
				}
				boolean fatal = !target.isAlive();
				Vec3d vel = player.getRotationVecClient().normalize().multiply(fatal ? 1.5f : 0.75f).multiply(target instanceof ProjectileEntity ? 2.5f : 1f);
				if(target instanceof ProjectileEntity || (target instanceof LivingEntityAccessor && ((LivingEntityAccessor)target).takePunchKnockback()))
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
			
			server.execute(() -> {
				player.setVelocity(dir);
				((WingedPlayerEntity)player).onDash();
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(GROUND_POUND_PACKET_ID, (server, player, handler, buf, sender) -> {
			boolean start = buf.readBoolean();
			boolean strong = buf.readBoolean();
			server.execute(() -> {
				WingedPlayerEntity winged = (WingedPlayerEntity)player;
				if(start)
					winged.startGroundPound();
				else
					winged.completeGroundPound(strong);
			});
		});
	}
}
