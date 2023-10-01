package absolutelyaya.ultracraft.mixin.client;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.TerminalBlock;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.block.TerminalDisplayBlock;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.screen.IntroScreen;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.resource.ResourceReload;
import net.minecraft.sound.MusicSound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{
	@Shadow @Nullable public ClientPlayerEntity player;
	
	@Shadow @Nullable public ClientWorld world;
	
	@Shadow @Final public GameOptions options;
	
	@Shadow @Final public Mouse mouse;
	
	@Shadow @Nullable public Screen currentScreen;
	
	@Shadow @Nullable public HitResult crosshairTarget;
	boolean isShooting, wasBreaking;
	
	@Redirect(method = "handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
	void OnHandSwap(ClientPlayNetworkHandler networkHandler, Packet<?> packet)
	{
		if(world != null && !UltracraftClient.isHandSwapEnabled())
			networkHandler.sendPacket(packet);
		else if(player != null)
			player.sendMessage(Text.translatable("message.ultracraft.handswap-disabled"), true);
	}
	
	@Inject(method = "handleInputEvents()V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I"), locals = LocalCapture.CAPTURE_FAILHARD)
	void OnHotbarKeyPress(CallbackInfo ci, int i)
	{
		if(player.getInventory().selectedSlot == i && player.getInventory().getStack(i).getItem() instanceof AbstractWeaponItem)
			AbstractWeaponItem.cycleVariant(player);
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
		if(player == null || player.isSpectator())
		{
			if(isShooting)
				stopShooting();
			return;
		}
		if(currentScreen == null && mouse.isCursorLocked())
		{
			if(options.attackKey.isPressed() != isShooting && player.getInventory().getMainHandStack().getItem() instanceof AbstractWeaponItem w)
			{
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeByte(options.attackKey.isPressed() ? 1 : 0);
				buf.writeVector3f(player.getVelocity().toVector3f());
				ClientPlayNetworking.send(PacketRegistry.PRIMARY_SHOT_C2S_PACKET_ID, buf);
				w.onPrimaryFire(world, player, player.getVelocity());
				isShooting = options.attackKey.isPressed();
				UltraComponents.WINGED_ENTITY.get(player).setPrimaryFiring(isShooting);
			}
		}
		else if(isShooting)
			stopShooting();
	}
	
	void stopShooting()
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeByte(0);
		ClientPlayNetworking.send(PacketRegistry.PRIMARY_SHOT_C2S_PACKET_ID, buf);
		isShooting = false;
		UltraComponents.WINGED_ENTITY.get(player).setPrimaryFiring(false);
	}
	
	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	void onDoAttack(CallbackInfoReturnable<Boolean> cir)
	{
		if(player == null || player.isSpectator())
			return;
		HitResult hit = crosshairTarget;
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
		
		if(!(hit instanceof BlockHitResult bhit))
			return;
		BlockState state = player.getWorld().getBlockState(bhit.getBlockPos());
		if(state.isOf(BlockRegistry.TERMINAL_DISPLAY))
		{
			if(options.sneakKey.isPressed())
				return;
			((TerminalDisplayBlock)player.getWorld().getBlockState(bhit.getBlockPos()).getBlock()).onHit(world, bhit.getBlockPos(), bhit, player);
			player.swingHand(Hand.MAIN_HAND);
			cir.setReturnValue(false);
		}
		if(player.isCreative() && state.isOf(BlockRegistry.TERMINAL) && UltracraftClient.isTerminalProtEnabled())
		{
			Direction dir = state.get(TerminalBlock.HALF).equals(DoubleBlockHalf.LOWER) ? Direction.UP : Direction.DOWN;
			BlockPos pos = bhit.getBlockPos().offset(dir, 1);
			BlockEntity be = world.getBlockEntity(pos);
			if(be instanceof TerminalBlockEntity e && e.isCannotBreak(player))
			{
				player.swingHand(Hand.MAIN_HAND);
				player.sendMessage(Text.translatable("message.ultracraft.flamethrower.terminal-prot"));
				cir.setReturnValue(false);
				return;
			}
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
		HitResult hit = crosshairTarget;
		if(!(hit instanceof BlockHitResult bhit))
			return;
		BlockPos hitPos = bhit.getBlockPos();
		BlockState state = player.getWorld().getBlockState(hitPos);
		if(player.isCreative() && state.isOf(BlockRegistry.PEDESTAL) && options.sneakKey.isPressed())
			ci.cancel();
		if(player.isCreative() && state.isOf(BlockRegistry.TERMINAL) && UltracraftClient.isTerminalProtEnabled())
		{
			Direction dir = state.get(TerminalBlock.HALF).equals(DoubleBlockHalf.LOWER) ? Direction.UP : Direction.DOWN;
			BlockPos pos = bhit.getBlockPos().offset(dir, 1);
			BlockEntity be = world.getBlockEntity(pos);
			if(be instanceof TerminalBlockEntity e && e.isCannotBreak(player))
			{
				ci.cancel();
				return;
			}
		}
		if(state.isOf(BlockRegistry.TERMINAL_DISPLAY))
		{
			((TerminalDisplayBlock)player.getWorld().getBlockState(bhit.getBlockPos()).getBlock()).onPoint(world, bhit.getBlockPos(), bhit, player);
			ci.cancel();
		}
		if(player.getInventory().getMainHandStack().getItem() instanceof AbstractWeaponItem w && w.shouldCancelPunching())
			ci.cancel();
		wasBreaking = bl;
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
