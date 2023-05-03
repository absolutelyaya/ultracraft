package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.IPunchableBlock;
import absolutelyaya.ultracraft.client.UltracraftClient;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Environment(EnvType.CLIENT)
public class KeybindRegistry
{
	public static final KeyBinding PUNCH = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.ultracraft.punch", InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_F, "category.ultracraft"));
	public static final KeyBinding HIGH_VELOCITY_TOGGLE = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.ultracraft.hivel_toggle", InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_V, "category.ultracraft"));
	
	public static void register()
	{
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while(HIGH_VELOCITY_TOGGLE.wasPressed())
			{
				UltracraftClient.toggleHiVelEnabled();
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeBoolean(UltracraftClient.isHiVelEnabled());
				ClientPlayNetworking.send(PacketRegistry.SET_HIGH_VELOCITY_C2S_PACKET_ID, buf);
				((WingedPlayerEntity)client.player).setWingsVisible(UltracraftClient.isHiVelEnabled());
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while(PUNCH.wasPressed())
			{
				PlayerEntity player = client.player;
				HitResult crosshairTarget = client.crosshairTarget;
				
				if(player == null || !((LivingEntityAccessor)player).Punch())
					return;
				
				Entity entity = null;
				if(crosshairTarget == null)
					return;
				if(crosshairTarget.getType().equals(HitResult.Type.ENTITY))
					entity = ((EntityHitResult)crosshairTarget).getEntity();
				else if(crosshairTarget.getType().equals(HitResult.Type.BLOCK))
				{
					BlockHitResult hit = ((BlockHitResult)crosshairTarget);
					BlockState state = player.world.getBlockState(hit.getBlockPos());
					if(state.getBlock() instanceof IPunchableBlock punchable)
					{
						PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
						buf.writeBlockPos(hit.getBlockPos());
						buf.writeBoolean(false);
						ClientPlayNetworking.send(PacketRegistry.PUNCH_BLOCK_PACKET_ID, buf);
						if (punchable.onPunch(player, hit.getBlockPos(), false))
							return; //if punch interaction was successful, don't display break particles and stuff
					}
					Vec3d pos = hit.getPos();
					for (int i = 0; i < 6; i++)
					{
						player.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.x, pos.y, pos.z, 0f, 0f, 0f);
					}
					player.playSound(state.getSoundGroup().getHitSound(), 1f, 1f);
					if(state.isIn(BlockTagRegistry.PUNCH_BREAKABLE))
						player.world.breakBlock(hit.getBlockPos(), true, player);
					return;
				}
				
				Vec3d forward = player.getRotationVecClient();
				Vec3d pos = player.getCameraPosVec(0f).add(forward.normalize().multiply(1));
				List<ProjectileEntity> projectiles = player.world.getEntitiesByClass(ProjectileEntity.class,
						new Box(pos.x - 0.75f, pos.y - 0.75f, pos.z - 0.75f, pos.x + 0.75f, pos.y + 0.75f, pos.z + 0.75f),
						(e) -> !((ProjectileEntityAccessor)e).isParried());
				if(projectiles.size() > 0)
					entity = getNearestProjectile(projectiles, pos);
				
				if(entity != null)
				{
					PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
					buf.writeInt(entity.getId());
					buf.writeBoolean(!player.getStackInHand(Hand.OFF_HAND).isEmpty());
					ClientPlayNetworking.send(PacketRegistry.PUNCH_ENTITY_PACKET_ID, buf);
				}
			}
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
