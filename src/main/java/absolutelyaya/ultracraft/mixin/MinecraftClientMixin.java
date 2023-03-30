package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.MusicSound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{
	@Shadow @Nullable public ClientPlayerEntity player;
	
	@Shadow @Nullable public ClientWorld world;
	
	@Redirect(method = "handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
	void OnHandSwap(ClientPlayNetworkHandler networkHandler, Packet<?> packet)
	{
		if(world != null && !UltracraftClient.isHandSwapEnabled())
			networkHandler.sendPacket(packet);
		else if(player != null)
			player.sendMessage(Text.translatable("message.ultrakill.handswap-disabled"), true);
	}
	
	@Inject(method = "getMusicType", at = @At("RETURN"), cancellable = true)
	void onGetMusicType(CallbackInfoReturnable<MusicSound> cir)
	{
		if (cir.getReturnValue().equals(MusicType.MENU) && UltracraftClient.REPLACE_MENU_MUSIC)
			cir.setReturnValue(new MusicSound(SoundRegistry.THE_FIRE_IS_GONE, 20, 600, true));
	}
	
	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	void onDoAttack(CallbackInfoReturnable<Boolean> cir)
	{
		if(player != null && player.getInventory().getMainHandStack().getItem() instanceof AbstractWeaponItem)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			ClientPlayNetworking.send(PacketRegistry.PRIMARY_SHOT_PACKET_ID, buf);
			cir.setReturnValue(false);
		}
	}
	
	@Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
	void onHandleBlockBreaking(boolean bl, CallbackInfo ci)
	{
		if(player != null && player.getInventory().getMainHandStack().getItem() instanceof AbstractWeaponItem)
			ci.cancel();
	}
}
