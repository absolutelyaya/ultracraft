package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.util.RenderingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec2f;
import org.joml.Vector4f;

import java.net.MalformedURLException;
import java.net.URL;

public class InfoPopupScreen extends Screen
{
	Screen parent;
	
	protected InfoPopupScreen(Text title, Screen parent)
	{
		super(title);
		this.parent = parent;
	}
	
	@Override
	protected void init()
	{
		super.init();
		try
		{
			URL url = new URL("https://ko-fi.com/absolutelyaya");
			addDrawableChild(ButtonWidget.builder(Text.translatable("screen.ultracraft.info.supporter.accept"), (b) -> {
				client.setScreen(new ConfirmLinkScreen((confirmed) -> {
					if (confirmed)
						Util.getOperatingSystem().open(url);
					close();
				}, url.toString(), true));
			}).dimensions(width / 2 - 45, height / 2 + 50, 40, 20)
									 .tooltip(Tooltip.of(Text.translatable("screen.ultracraft.info.supporter.accept.tooltip"))).build());
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException(e);
		}
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.ultracraft.info.supporter.decline"), (b) -> close())
								 .dimensions(width / 2 + 5, height / 2 + 50, 40, 20).build());
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		fill(matrices, width / 2 - 150, height / 2 - 75, width / 2 + 150, height / 2 + 75, 0xff000000);
		drawBorder(matrices, width / 2 - 150, height / 2 - 75, 300, 150, 0xffffffff);
		super.render(matrices, mouseX, mouseY, delta);
		matrices.push();
		matrices.scale(2, 2, 2);
		drawCenteredTextWithShadow(matrices, textRenderer, Text.of(title.getString().replace("§6", "§6§n")),
				(int)(width / 4f), (int)(height / 4f) - 35, 0xffffffff);
		matrices.pop();
		String[] lines = Text.translatable("screen.ultracraft.info.supporter-text").getString().split("\n");
		for (int i = 0; i < lines.length; i++)
		{
			drawCenteredTextWithShadow(matrices,  textRenderer, Text.of(lines[i]),
					(int)(width / 2f), (int)(height / 2f) - 45 + 10 * i, 0xffffffff);
		}
		RenderSystem.setShaderTexture(0, new Identifier(Ultracraft.MOD_ID, "textures/gui/urepic.png"));
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderingUtil.drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(width / 2f - 174 / 2f, height / 2f + 5, 174, 42),
				new Vec2f(174, 42), new Vector4f(0f, 0f, 174f, -42f));
		
		client.gameRenderer.loadPostProcessor(new Identifier(Ultracraft.MOD_ID, "shaders/post/blur.json"));
	}
	
	@Override
	public void close()
	{
		client.setScreen(parent);
		client.gameRenderer.disablePostProcessor();
	}
}
