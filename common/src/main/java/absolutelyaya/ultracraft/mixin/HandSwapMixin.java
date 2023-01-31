package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.ClientPlayerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class HandSwapMixin
{
	@Shadow @Nullable public ClientPlayerEntity player;
	
	@Redirect(method = "handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
	void OnHandSwap(ClientPlayNetworkHandler instance, Packet<?> packet)
	{
		if(player != null)
			((ClientPlayerAccessor)player).Punch();
	}
}
