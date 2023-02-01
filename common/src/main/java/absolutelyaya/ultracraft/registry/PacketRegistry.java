package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PacketRegistry
{
	public static final Identifier PUNCH_ENTITY_PACKET_ID = new Identifier(Ultracraft.MOD_ID, "punch");
	
	public static void register()
	{
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
				else
					world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 0.75f, 0.5f);
				Vec3d vel = player.getRotationVecClient().normalize().multiply(fatal ? 2f : 1f).multiply(target instanceof ProjectileEntity ? 2.5f : 1f);
				target.setVelocity(vel);
			}
		});
	}
}
