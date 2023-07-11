package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.Ultraconfig;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EpilepsyPopupScreen extends InfoPopupScreen
{
	Ultraconfig config;
	
	public EpilepsyPopupScreen(Screen parent)
	{
		super(Text.translatable("screen.ultracraft.info.epilepsy.title"), Text.translatable("screen.ultracraft.info.epilepsy.text"), parent);
		config = UltracraftClient.getConfigHolder().get();
	}
	
	@Override
	protected void init()
	{
		super.init();
		ButtonWidget button = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.ultracraft.info.epilepsy.affected"), (b) -> {
			close();
			config.safeVFX = true;
			config.showEpilepsyWarning = false;
		}).dimensions(width / 2 - 105, height / 2 + 50, 100, 20).build());
		button.setTooltip(Tooltip.of(Text.translatable("screen.ultracraft.info.epilepsy.affected.tooltip")));
		button = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.ultracraft.info.epilepsy.unaffected"), (b) -> {
			close();
			config.safeVFX = false;
			config.showEpilepsyWarning = false;
		}).dimensions(width / 2 + 5, height / 2 + 50, 100, 20).build());
		button.setTooltip(Tooltip.of(Text.translatable("screen.ultracraft.info.epilepsy.unaffected.tooltip")));
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		context.fill(0, 0, width, height, 0x88000000);
		super.render(context, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
}
