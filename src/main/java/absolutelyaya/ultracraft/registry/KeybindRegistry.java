package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.IPunchableBlock;
import absolutelyaya.ultracraft.client.UltracraftClient;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class KeybindRegistry
{
	public static final KeyBinding PUNCH = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.ultracraft.punch", InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_F, "category.ultracraft"));
	public static final KeyBinding HIGH_VELOCITY_TOGGLE = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.ultracraft.hivel_toggle", InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_V, "category.ultracraft"));
	
	static boolean hivelPressed = false, punchPressed = false;
	
	public static void register()
	{
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while(HIGH_VELOCITY_TOGGLE.wasPressed() && !hivelPressed)
			{
				UltracraftClient.toggleHiVelEnabled();
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeBoolean(UltracraftClient.isHiVelEnabled());
				ClientPlayNetworking.send(PacketRegistry.SET_HIGH_VELOCITY_C2S_PACKET_ID, buf);
				((WingedPlayerEntity)client.player).setWingsVisible(UltracraftClient.isHiVelEnabled());
				hivelPressed = true;
			}
			while(HIGH_VELOCITY_TOGGLE.wasPressed()); //remove stored hivel toggle presses
			hivelPressed = HIGH_VELOCITY_TOGGLE.isPressed();
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while(PUNCH.wasPressed() && !punchPressed)
			{
				ClientPlayerEntity player = client.player;
				if(player == null || !((LivingEntityAccessor)player).punch() || player.isSpectator())
					continue;
				
				HitResult crosshairTarget = client.crosshairTarget;
				Entity entity = null;
				if(crosshairTarget != null)
				{
					if(crosshairTarget.getType().equals(HitResult.Type.ENTITY))
						entity = ((EntityHitResult)crosshairTarget).getEntity();
					else if(crosshairTarget.getType().equals(HitResult.Type.BLOCK))
					{
						BlockHitResult hit = ((BlockHitResult)crosshairTarget);
						BlockState state = player.world.getBlockState(hit.getBlockPos());
						if(state.getBlock() instanceof IPunchableBlock || state.isIn(TagRegistry.FRAGILE) || state.getBlock() instanceof BellBlock)
						{
							PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
							buf.writeBlockPos(hit.getBlockPos());
							buf.writeBoolean(false);
							ClientPlayNetworking.send(PacketRegistry.PUNCH_BLOCK_PACKET_ID, buf);
							if (state.getBlock() instanceof IPunchableBlock punchable && punchable.onPunch(player, hit.getBlockPos(), false))
								return; //if punch interaction was successful, don't display break particles and stuff
						}
						Vec3d pos = hit.getPos();
						for (int i = 0; i < 6; i++)
							player.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.x, pos.y, pos.z, 0f, 0f, 0f);
						player.playSound(state.getSoundGroup().getHitSound(), 1f, 1f);
					}
				}
				boolean b = entity != null && !(entity instanceof ProjectileEntity);
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeBoolean(b);
				if(b)
					buf.writeInt(entity.getId());
				buf.writeVector3f(player.getVelocity().toVector3f());
				buf.writeBoolean(UltracraftClient.getConfigHolder().get().showPunchArea);
				ClientPlayNetworking.send(PacketRegistry.PUNCH_PACKET_ID, buf);
				punchPressed = true;
			}
			while(PUNCH.wasPressed()); //remove stored punch presses
			punchPressed = PUNCH.isPressed();
		});
	}
}
