package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatSendMixin
{
	@Inject(method = "sendMessage", at = @At("HEAD"))
	void OnSendMessage(String message, boolean addToHistory, CallbackInfoReturnable<Boolean> cir)
	{
		if(message.equalsIgnoreCase("press alt to throw it back"))
		{
			PlayerEntity player = MinecraftClient.getInstance().player;
			if(player == null)
				return;
			player.world.getEntitiesByType(EntityRegistry.FILTH, player.getBoundingBox().expand(128.0), entity -> true)
					.forEach(FilthEntity::throwback);
		}
		else if(message.equalsIgnoreCase("/gamerule timeStopEffect FORCE_ON"))
		{
			PlayerEntity player = MinecraftClient.getInstance().player;
			player.sendMessage(Text.translatable("message.ultracraft.server.freeze-enable-warning"));
		}
	}
}
