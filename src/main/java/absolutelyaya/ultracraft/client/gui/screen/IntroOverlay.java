package absolutelyaya.ultracraft.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;

public class IntroOverlay extends Overlay
{
	float alpha = 2;
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
		int height = MinecraftClient.getInstance().getWindow().getScaledHeight();
		MatrixStack matrices = context.getMatrices();
		matrices.translate(0f, 0f, -500f);
		MinecraftClient.getInstance().currentScreen.render(context, 0, 0, delta);
		matrices.translate(0f, 0f, 500f);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1f, 1f, 1f, MathHelper.clamp(alpha, 0f, 1f));
		renderBackground(width, height);
		context.fillGradient(0, 0, width, height / 3,
				new Color(0, 0, 0, 150).getRGB(), new Color(0, 0, 0, 0).getRGB());
		context.fillGradient(0, height - height / 3, width, height,
				new Color(0, 0, 0, 0).getRGB(), new Color(0, 0, 0, 150).getRGB());
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		List<OrderedText> lines = textRenderer.wrapLines(
				StringVisitable.plain(Text.translatable("intro.ultracraft.status",
						MinecraftClient.getInstance().getSession().getUsername()).getString()), width);
		for (int i = 0; i < lines.size(); i++)
			context.drawTextWithShadow(textRenderer, lines.get(i), 32, 32 + i * (textRenderer.fontHeight + 2),
					new Color(1f, 1f, 1f, MathHelper.clamp(alpha, 0.05f, 1f)).getRGB());
		alpha -= 1f / 120f;
		if(alpha <= -0.1f)
			MinecraftClient.getInstance().setOverlay(null);
	}
	
	void renderBackground(float width, float height)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.setShaderTexture(0, new Identifier("gui/options_background.png"));
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(0, height, 0).texture(0f, height / 32f).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(width, height, 0).texture(width / 32f, height / 32f).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(width, 0, 0).texture(width / 32f, 0f).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(0, 0, 0).texture(0f, 0f).color(64, 64, 64, 255).next();
		tessellator.draw();
	}
}
