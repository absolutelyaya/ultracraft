package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.screen.IntroScreen;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.resource.ResourceReload;
import net.minecraft.sound.MusicSound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{
	@Shadow @Nullable public ClientPlayerEntity player;
	
	@Shadow @Nullable public ClientWorld world;
	
	@Shadow @Final public GameOptions options;
	
	@Shadow @Nullable public ClientPlayerInteractionManager interactionManager;
	
	@Shadow @Final public Mouse mouse;
	
	@Shadow @Nullable public Screen currentScreen;
	
	boolean isShooting;
	
	@Redirect(method = "handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
	void OnHandSwap(ClientPlayNetworkHandler networkHandler, Packet<?> packet)
	{
		if(world != null && !UltracraftClient.isHandSwapEnabled())
			networkHandler.sendPacket(packet);
		else if(player != null)
			player.sendMessage(Text.translatable("message.ultracraft.handswap-disabled"), true);
	}
	
	@Inject(method = "getMusicType", at = @At("RETURN"), cancellable = true)
	void onGetMusicType(CallbackInfoReturnable<MusicSound> cir)
	{
		if (cir.getReturnValue().equals(MusicType.MENU) && UltracraftClient.REPLACE_MENU_MUSIC)
			cir.setReturnValue(new MusicSound(SoundRegistry.THE_FIRE_IS_GONE, 20, 600, true));
	}
	
	@Inject(method = "handleInputEvents", at = @At("TAIL"))
	void onHandleInputs(CallbackInfo ci)
	{
		if(currentScreen == null && mouse.isCursorLocked())
		{
			if(options.attackKey.isPressed() != isShooting && player.getInventory().getMainHandStack().getItem() instanceof AbstractWeaponItem w)
			{
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeByte(options.attackKey.isPressed() ? 1 : 0);
				buf.writeVector3f(player.getVelocity().toVector3f());
				ClientPlayNetworking.send(PacketRegistry.PRIMARY_SHOT_PACKET_ID_C2S, buf);
				w.onPrimaryFire(world, player, player.getVelocity());
				isShooting = options.attackKey.isPressed();
				((WingedPlayerEntity)player).setPrimaryFiring(isShooting);
			}
		}
		else if(isShooting)
			stopShooting();
	}
	
	void stopShooting()
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeByte(0);
		ClientPlayNetworking.send(PacketRegistry.PRIMARY_SHOT_PACKET_ID_C2S, buf);
		isShooting = false;
		((WingedPlayerEntity)player).setPrimaryFiring(false);
	}
	
	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	void onDoAttack(CallbackInfoReturnable<Boolean> cir)
	{
		if(player == null || player.isSpectator())
			return;
		HitResult hit = player.raycast(interactionManager.getReachDistance(), 0f, false);
		boolean pedestal = hit instanceof BlockHitResult bhit && player.getWorld().getBlockState(bhit.getBlockPos()).isOf(BlockRegistry.PEDESTAL);
		if(player.getInventory().getMainHandStack().getItem() instanceof AbstractWeaponItem w && w.shouldCancelPunching())
		{
			if(options.sneakKey.isPressed() && pedestal && !player.getMainHandStack().isOf(Items.DEBUG_STICK))
			{
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeBlockPos(((BlockHitResult)hit).getBlockPos());
				buf.writeBoolean(true);
				ClientPlayNetworking.send(PacketRegistry.PUNCH_BLOCK_PACKET_ID, buf);
				player.swingHand(Hand.MAIN_HAND);
				cir.setReturnValue(false);
				return;
			}
			cir.setReturnValue(false);
			return;
		}
		if(!pedestal)
			return;
		if(player.isCreative())
		{
			if(options.sneakKey.isPressed())
			{
				player.swingHand(Hand.MAIN_HAND);
				cir.setReturnValue(false);
			}
			else
				return;
		}
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(((BlockHitResult)hit).getBlockPos());
		buf.writeBoolean(true);
		ClientPlayNetworking.send(PacketRegistry.PUNCH_BLOCK_PACKET_ID, buf);
	}
	
	@Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
	void onHandleBlockBreaking(boolean bl, CallbackInfo ci)
	{
		if(player == null)
			return;
		HitResult hit = player.raycast(interactionManager.getReachDistance(), 0f, false);
		boolean pedestal = hit instanceof BlockHitResult bhit && player.getWorld().getBlockState(bhit.getBlockPos()).isOf(BlockRegistry.PEDESTAL);
		if(player.isCreative() && pedestal && options.sneakKey.isPressed())
			ci.cancel();
		else if(player.getInventory().getMainHandStack().getItem() instanceof AbstractWeaponItem w && w.shouldCancelPunching())
			ci.cancel();
	}
	
	@ModifyVariable(method = "<init>", at = @At(value = "STORE"))
	ResourceReload onInitialResourceLoad(ResourceReload resourceReload)
	{
		resourceReload.whenComplete().thenRun(() -> {
			if(IntroScreen.INSTANCE != null)
			{
				IntroScreen.INSTANCE.resourceLoadFinished();
				IntroScreen.RESOURCES_LOADED = true;
			}
		});
		return resourceReload;
	}
}
