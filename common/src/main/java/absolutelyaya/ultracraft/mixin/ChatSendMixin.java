package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import net.minecraft.client.gui.screen.ChatScreen;
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
			FilthEntity.throwback();
	}
}
