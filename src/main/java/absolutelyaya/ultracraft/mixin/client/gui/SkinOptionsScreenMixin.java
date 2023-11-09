package absolutelyaya.ultracraft.mixin.client.gui;

import absolutelyaya.ultracraft.client.Ultraconfig;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.screen.WingCustomizationScreen;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SkinOptionsScreen.class)
public abstract class SkinOptionsScreenMixin extends GameOptionsScreen
{
	public SkinOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title)
	{
		super(parent, gameOptions, title);
	}
	
	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/option/SkinOptionsScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 2), locals = LocalCapture.CAPTURE_FAILHARD)
	void onInit(CallbackInfo ci, int i)
	{
		Ultraconfig config = UltracraftClient.getConfig();
		addDrawableChild(
				CyclingButtonWidget.onOffBuilder(config.armVisible).build(width / 2 - 155, height / 6 + 24 * ((i - 2) >> 1), 150, 20,
						Text.translatable("screen.ultracraft.arm-visibility"),
						(b, v) -> {
							config.armVisible = v;
							PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
							buf.writeBoolean(v);
							ClientPlayNetworking.send(PacketRegistry.ARM_VISIBLE_PACKET_ID, buf);
						}));
		ButtonWidget b = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.ultracraft.wing-settings.title").append("..."),
						(button) -> client.setScreen(new WingCustomizationScreen(this)))
								 .dimensions(width / 2 + 5, height / 6 + 24 * ((i - 2) >> 1), 150, 20).build());
		if(client.player == null)
		{
			b.active = false;
			b.setTooltip(Tooltip.of(Text.translatable("screen.ultracraft.wing-settings.only-in-world")));
		}
		//if(UltracraftClient.IRIS)
		//{
		//	b.active = false;
		//	b.setTooltip(Tooltip.of(Text.translatable("screen.ultracraft.wing-settings.iris")));
		//}
	}
	
	@ModifyVariable(method = "init", ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/option/SkinOptionsScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 1))
	int modifyButtonCount(int i)
	{
		return i + 2;
	}
}
