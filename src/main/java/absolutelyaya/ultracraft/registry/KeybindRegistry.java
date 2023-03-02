package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

public class KeybindRegistry
{
	public static final KeyBinding HIGH_VELOCITY_TOGGLE = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.ultracraft.hivel_toggle", InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_V, "category.ultracraft"));
	
	public static void register()
	{
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while(HIGH_VELOCITY_TOGGLE.wasPressed())
			{
				//TODO: add High Velocity Mode (dashing & sliding)
				//TODO: Visual Indicator for HiVelMode
				//TODO: render wings on Players in HiVelMode
				UltracraftClient.toggleHiVelEnabled();
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeBoolean(UltracraftClient.isHiVelEnabled());
				ClientPlayNetworking.send(PacketRegistry.SET_HIGH_VELOCITY_PACKET_ID, buf);
				((WingedPlayerEntity)client.player).setWingsVisible(UltracraftClient.isHiVelEnabled());
			}
		});
	}
}
