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

public class SupporterPopupScreen extends InfoPopupScreen
{
	protected SupporterPopupScreen(Screen parent)
	{
		super(Text.translatable("screen.ultracraft.info.supporter.title"), Text.translatable("screen.ultracraft.info.supporter.text"), parent);
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
		super.render(matrices, mouseX, mouseY, delta);
		RenderSystem.setShaderTexture(0, new Identifier(Ultracraft.MOD_ID, "textures/gui/urepic.png"));
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderingUtil.drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(width / 2f - 174 / 2f, height / 2f + 5, 175, 43),
				new Vec2f(174, 42), new Vector4f(0f, 0f, 174f, -42f));
		
		client.gameRenderer.loadPostProcessor(new Identifier(Ultracraft.MOD_ID, "shaders/post/blur.json"));
	}
}
