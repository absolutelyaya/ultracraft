package absolutelyaya.ultracraft.mixin.client.gui;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen
{
	private final static Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/skull.png");
	float time;
	
	@Shadow @Final private List<ButtonWidget> buttons;
	
	@Shadow @Final private Text message;
	
	protected DeathScreenMixin(Text title)
	{
		super(title);
	}
	
	@Inject(method = "init", at = @At("TAIL"))
	void onInit(CallbackInfo ci)
	{
		if(!UltracraftClient.getConfig().deathScreen)
			return;
		for (ButtonWidget button : buttons)
		{
			button.setPosition(button.getX(), button.getY() + 85);
		}
	}
	
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci)
	{
		if(!UltracraftClient.getConfig().deathScreen)
			return;
		time += delta;
		context.fill(0, 0, width, height, 0xaa000000);
		context.fillGradient(0, height / 2, width, height, 0x00000000, 0x44000000);
		context.fillGradient(0, 0, width, height / 2, 0x44000000, 0x00000000);
		for (Drawable drawable : buttons)
			drawable.render(context, mouseX, mouseY, delta);
		context.drawTexture(TEXTURE, width / 2 - 64, height / 2 - 64, time <= 10f ? 128 : 0, 0, 128, 128);
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		int size = 5;
		matrices.scale(size, size, size);
		Text t = Text.translatable("screen.ultracraft.death.title");
		context.drawText(textRenderer, t, (width / 2 - textRenderer.getWidth(t) * size / 2) / size, 10 / size, 0xdff6f5, false);
		t = Text.translatable("screen.ultracraft.death.respawn");
		context.drawText(textRenderer, t, (width / 2 - textRenderer.getWidth(t) * size / 2) / size, (height - 50) / size, 0xdff6f5, false);
		matrices.pop();
		t = message;
		if(message != null)
			context.drawText(textRenderer, t, (width / 2 - textRenderer.getWidth(t) / 2), 80, 0xdff6f5, false);
		if(time >= 20f)
		{
			//TODO: get an actual "hah" sound effect
			//client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.MASTER, 0.5f, 0.256f);
			time = 0f;
		}
		ci.cancel();
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(!UltracraftClient.getConfig().deathScreen)
			return super.keyPressed(keyCode, scanCode, modifiers);
		if(keyCode == 82)
			client.player.requestRespawn();
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
