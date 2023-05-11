package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.Ultraconfig;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;

public class IntroScreen extends Screen
{
	public static IntroScreen INSTANCE;
	public static boolean SEQUENCE_FINISHED, RESOURCES_LOADED;
	
	ButtonWidget closeButton;
	boolean waitingForInput, waitingForButton, hurry, popupGrow, broken = true;
	int timer, step;
	String goalText, curText = "something broke... sorry about that.";
	float closeButtonAlpha = 0f, popupSize = 0f;
	Ultraconfig config;
	
	public IntroScreen()
	{
		super(Text.of("ultra-intro"));
	}
	
	@Override
	protected void init()
	{
		super.init();
		config = UltracraftClient.getConfigHolder().getConfig();
		INSTANCE = this;
		SEQUENCE_FINISHED = false;
		closeButton = addDrawableChild(new ButtonWidget.Builder(Text.translatable("message.ultracraft.consent"), (button) -> {
			config.lastVersion = Ultracraft.VERSION;
			waitingForButton = false;
		}).dimensions(width / 2 - 49, height - 36, 98, 20).build());
		GameOptions options = MinecraftClient.getInstance().options;
		if(!config.startedBefore && options.swapHandsKey.isDefault())
		{
			options.setKeyCode(options.swapHandsKey, InputUtil.fromKeyCode(InputUtil.GLFW_KEY_R, 19));
			config.startedBefore = true;
			UltracraftClient.getConfigHolder().save();
		}
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		super.render(matrices, mouseX, mouseY, delta);
		renderBackground(matrices);
		fillGradient(matrices, 0, 0, width, height / 3,
				new Color(0, 0, 0, 150).getRGB(), new Color(0, 0, 0, 0).getRGB());
		fillGradient(matrices, 0, height - height / 3, width, height,
				new Color(0, 0, 0, 0).getRGB(), new Color(0, 0, 0, 150).getRGB());
		
		if(step == 1 && closeButtonAlpha < 1f)
		{
			closeButtonAlpha += delta;
			closeButtonAlpha = Math.min(closeButtonAlpha, 1f);
			closeButton.active = true;
		}
		else if(closeButton.active && closeButtonAlpha < 1f)
			closeButton.active = false;
		closeButton.setAlpha(closeButtonAlpha);
		List<OrderedText> lines = textRenderer.wrapLines(StringVisitable.plain(curText), width);
		for (int i = 0; i < lines.size(); i++)
			textRenderer.drawWithShadow(matrices, lines.get(i), 32, 32 + i * (textRenderer.fontHeight + 2), Color.WHITE.getRGB());
		if(waitingForInput)
			drawCenteredTextWithShadow(matrices, textRenderer, Text.translatable("intro.ultracraft.input"), width / 2, height - 25, Color.WHITE.getRGB());
		if(popupGrow)
		{
			if(popupSize < 1f)
				popupSize += 1 / 30f;
		}
		else if (popupSize > 0f)
			popupSize -= 1 / 30f;
		if(popupSize > 0f)
		{
			fill(matrices, (int)(width / 2 - (width / 2 - 25) * popupSize) - 2, (int)(height / 2 - (height / 2 - 35) * popupSize) - 2,
					(int)(width / 2 + (width / 2 - 25) * popupSize) + 2, (int)(height / 2 + (height / 2 - 35) * popupSize) + 2,
					Color.WHITE.getRGB());
			fill(matrices, (int)(width / 2 - (width / 2 - 25) * popupSize), (int)(height / 2 - (height / 2 - 35) * popupSize),
					(int)(width / 2 + (width / 2 - 25) * popupSize), (int)(height / 2 + (height / 2 - 35) * popupSize),
					Color.BLACK.getRGB());
		}
		if(popupSize >= 1f)
		{
			lines = textRenderer.wrapLines(Text.translatable("message.ultracraft.content"), width - 60);
			for (int i = 0; i < lines.size(); i++)
				textRenderer.drawWithShadow(matrices, lines.get(i), 28, 38 + i * (textRenderer.fontHeight + 2), Color.WHITE.getRGB());
			fill(matrices, width / 2 - 51, height - 35, width / 2 + 51, height - 14, Color.WHITE.getRGB());
			closeButton.render(matrices, mouseX, mouseY, delta);
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(config.repeatIntro && RESOURCES_LOADED)
		{
			resourceLoadFinished();
			config.repeatIntro = false;
			UltracraftClient.getConfigHolder().save();
		}
		if(goalText == null)
			return;
		if(goalText.length() > 0 && !waitingForInput && popupSize <= 0f && timer-- <= 0)
		{
			char c = goalText.charAt(0);
			if(c == '\n' && curText.charAt(curText.length() - 1) != '\n')
				timer = 10;
			curText += c;
			goalText = goalText.substring(1);
			if(hurry)
			{
				curText += goalText;
				goalText = "";
				hurry = false;
			}
		}
		if(goalText.length() == 0 && !waitingForInput && !waitingForButton)
		{
			switch (++step)
			{
				case 0, 3, 5 -> waitingForInput = true;
				case 1 -> {
					popupGrow = true;
					waitingForButton = true;
				}
				case 2 -> {
					popupGrow = false;
					goalText += Text.translatable("intro.ultracraft.calibration-complete").getString();
				}
				case 4 -> {
					curText = "";
					goalText = Text.translatable("intro.ultracraft.status", MinecraftClient.getInstance().getSession().getUsername()).getString();
				}
				case 6 -> {
					SEQUENCE_FINISHED = true;
					MinecraftClient.getInstance().setScreen(new TitleScreen(true));
					INSTANCE = null;
					close();
					MinecraftClient.getInstance().setOverlay(new IntroOverlay());
				}
			}
		}
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return broken && RESOURCES_LOADED;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(waitingForInput && step != 2)
			waitingForInput = false;
		if(!waitingForInput && goalText.length() > 0)
			hurry = true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public void resourceLoadFinished()
	{
		closeButton.setAlpha(0f);
		curText = "";
		goalText = Text.translatable("intro.ultracraft.calibration", Ultracraft.VERSION).getString();
		timer = 40;
		broken = false;
		init();
	}
}
