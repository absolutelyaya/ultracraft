package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.MeleeParriable;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.block.IPunchableBlock;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.logging.Logger;

public class PacketRegistry
{
	public static final Identifier PUNCH_ENTITY_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "punch_entiy");
	public static final Identifier PUNCH_BLOCK_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "punch_block");
	public static final Identifier PRIMARY_SHOT_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "primary_shot");
	public static final Identifier HITSCAN_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "hitscan");
	
	public static void register()
	{
		//Client to Server
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PUNCH_ENTITY_PACKET_ID, (buf, context) -> {
			PlayerEntity player = context.getPlayer();
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
					Ultracraft.Freeze(10);
					world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.75f, 2f);
					ProjectileEntityAccessor pa = (ProjectileEntityAccessor)p;
					pa.setParried(true, player);
				}
				else if (target instanceof MeleeParriable mp && (!(mp instanceof MobEntity) || ((MobEntity)mp).isAttacking()))
				{
					Ultracraft.Freeze(10);
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
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PUNCH_BLOCK_PACKET_ID, (buf, context) -> {
			PlayerEntity player = context.getPlayer();
			World world = player.getWorld();
			BlockPos target = buf.readBlockPos();
			
			context.queue(() -> {
				if(target != null)
				{
					BlockState state = world.getBlockState(target);
					if(state.getBlock() instanceof IPunchableBlock punchable)
						punchable.onPunch(player, target);
				}
			});
		});
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PRIMARY_SHOT_PACKET_ID, (buf, context) -> {
			PlayerEntity player = context.getPlayer();
			if (player.getMainHandStack().getItem() instanceof AbstractWeaponItem gun)
				gun.onPrimaryFire(player.world, player);
		});
		
		//Server to Client
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, HITSCAN_PACKET_ID, ((buf, context) -> {
			UltracraftClient.HITSCAN_HANDLER.addEntry(
					new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
					new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
					buf.readByte());
		}));
	}
}
