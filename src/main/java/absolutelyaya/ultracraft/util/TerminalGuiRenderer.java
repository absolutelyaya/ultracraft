package absolutelyaya.ultracraft.util;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.*;

import java.lang.Math;
import java.text.MessageFormat;

public class TerminalGuiRenderer
{
	static final TextRenderer textRenderer;
	static final int LIGHT = 15728880;
	TerminalBlockEntity terminal;
	
	public TerminalGuiRenderer()
	{
	
	}
	
	public void setCurrentTerminal(TerminalBlockEntity terminal)
	{
		this.terminal = terminal;
	}
	
	public void drawTab(MatrixStack matrices, VertexConsumerProvider buffers, String title, boolean returnButton)
	{
		drawTab(matrices, buffers, title, "-", returnButton, "mainmenu");
	}
	
	public void drawTab(MatrixStack matrices, VertexConsumerProvider buffers, String title, boolean returnButton, String returnAction)
	{
		drawTab(matrices, buffers, title, "-", returnButton, returnAction);
	}
	
	public void drawTab(MatrixStack matrices, VertexConsumerProvider buffers, String title, String titleBorder, boolean returnButton, String returnAction)
	{
		String t = MessageFormat.format("{0} {1} {0}", titleBorder, Text.translatable(title).getString());
		drawBG(matrices, buffers);
		drawText(buffers, matrices, t, 50 - textRenderer.getWidth(t) / 2, - 100 + textRenderer.fontHeight, 0.01f);
		if(returnButton)
		{
			t = Text.translatable(TerminalBlockEntity.Button.RETURN_LABEL).getString();
			drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, 95 - textRenderer.fontHeight,
					textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, returnAction);
		}
	}
	
	public void drawBG(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		int BGcolor = 0xaa000000;
		Vector2f size = terminal.getCurWindowSize();
		drawBox(buffers, matrices, -Math.round((size.x - 100) / 2f), -Math.round((size.y - 100) / 2f), size.x, size.y, BGcolor);
	}
	
	public void drawMultiLine(VertexConsumerProvider buffers, MatrixStack matrices, String text, int x, int y, float z)
	{
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++)
			drawText(buffers, matrices, lines[i], x, y - textRenderer.fontHeight * i, z);
	}
	
	public void drawText(VertexConsumerProvider buffers, MatrixStack matrices, String text, int x, int y, float z)
	{
		drawText(buffers, matrices, text, x, y, z, terminal.getTextColor());
	}
	
	public void drawText(VertexConsumerProvider buffers, MatrixStack matrices, String text, int x, int y, float z, int color)
	{
		matrices.push();
		matrices.translate(0.5f, -0.5f, -z);
		matrices.scale(-0.01f, -0.01f, 0.01f);
		textRenderer.draw(text, x, y, color, false,
				new Matrix4f(matrices.peek().getPositionMatrix()), buffers, TextRenderer.TextLayerType.NORMAL, 0x00000000, LIGHT, false);
		matrices.pop();
	}
	
	public void drawButton(VertexConsumerProvider buffers, MatrixStack matrices, TerminalBlockEntity.Button button)
	{
		Vector2i pos = button.getPos();
		if(button.isCentered())
			drawButtonCentered(buffers, matrices, Text.translatable(button.getLabel()).getString(), pos.x, pos.y, button.getAction() + "@" + button.getValue());
		else
			drawButton(buffers, matrices, Text.translatable(button.getLabel()).getString(), pos.x, pos.y, button.getAction() + "@" + button.getValue());
	}
	
	public void drawButton(VertexConsumerProvider buffers, MatrixStack matrices, String text, int x, int y, String action)
	{
		text = Text.translatable(text).getString();
		int tWidth = textRenderer.getWidth(text);
		drawButton(buffers, matrices, text, x, y, tWidth + 3, textRenderer.fontHeight + 2, action);
	}
	
	public void drawButtonCentered(VertexConsumerProvider buffers, MatrixStack matrices, String text, int x, int y, String action)
	{
		text = Text.translatable(text).getString();
		int tWidth = textRenderer.getWidth(text);
		drawButton(buffers, matrices, text, x - tWidth / 2, y, tWidth + 3, textRenderer.fontHeight + 2, action);
	}
	
	public void drawButton(VertexConsumerProvider buffers, MatrixStack matrices, String text, int x, int y, int sizeX, int sizeY, String action)
	{
		text = Text.translatable(text).getString();
		Vector2d cursor = new Vector2d(terminal.getCursor()).mul(100f);
		boolean hovered = cursor.x > x && cursor.x < x + sizeX && cursor.y > y && cursor.y < y + sizeY;
		matrices.push();
		drawBoxOutline(buffers, matrices, x, y, sizeX, sizeY);
		if(hovered)
		{
			matrices.translate(0f, 0f, -0.001f);
			drawBox(buffers, matrices, x, y, sizeX, sizeY, terminal.getTextColor());
			terminal.setLastHovered(action);
		}
		else if(terminal.getLastHovered() != null && terminal.getLastHovered().equals(action))
			terminal.setLastHovered(null);
		matrices.translate(0f, 1f, -0.002f);
		drawText(buffers, matrices, text, x + 2, y + 2, 0f, hovered ? 0xff000000 : terminal.getTextColor());
		matrices.pop();
	}
	
	public void drawColorButton(VertexConsumerProvider buffers, MatrixStack matrices, int color, int x, int y, int sizeX, int sizeY, String action)
	{
		Vector2d cursor = new Vector2d(terminal.getCursor()).mul(100f);
		boolean hovered = cursor.x > x && cursor.x < x + sizeX && cursor.y > y && cursor.y < y + sizeY;
		matrices.push();
		drawBox(buffers, matrices, x, y, sizeX, sizeY, color, 0.005f);
		if(hovered)
		{
			drawBoxOutline(buffers, matrices, x, y, sizeX, sizeY, terminal.getTextColor());
			terminal.setLastHovered(action);
		}
		else if(terminal.getLastHovered() != null && terminal.getLastHovered().equals(action))
			terminal.setLastHovered(null);
		matrices.translate(0f, 1f, -0.01f);
		matrices.pop();
	}
	
	public void drawBoxOutline(VertexConsumerProvider buffers, MatrixStack matrices, int x, int y, int sizeX, int sizeY)
	{
		drawBoxOutline(buffers, matrices, x, y, sizeX, sizeY, terminal.getTextColor());
	}
	
	public void drawBoxOutline(VertexConsumerProvider buffers, MatrixStack matrices, int x, int y, int sizeX, int sizeY, int color)
	{
		matrices.push();
		matrices.translate(0f, 0f, -0.001f);
		drawBox(buffers, matrices, x, y + sizeY, sizeX, 1, color);
		drawBox(buffers, matrices, x, y - 1, sizeX, 1, color);
		drawBox(buffers, matrices, x - 1, y, 1, sizeY, color);
		drawBox(buffers, matrices, x + sizeX, y, 1, sizeY, color);
		matrices.pop();
	}
	
	public void drawBox(VertexConsumerProvider buffers, MatrixStack matrices, int x, int y, float sizeX, float sizeY, int color)
	{
		drawBox(buffers, matrices, x, y, sizeX, sizeY, color, 0f);
	}
	
	public void drawBox(VertexConsumerProvider buffers, MatrixStack matrices, int x, int y, float sizeX, float sizeY, int color, float z)
	{
		matrices.push();
		VertexConsumer consumer = buffers.getBuffer(RenderLayer.getGui());
		matrices.translate(0.5f, 0.5f, 0f);
		matrices.scale(-0.01f, -0.01f, -1f);
		matrices.translate(0f, 0f, z);
		Matrix4f poseMatrix = new Matrix4f(matrices.peek().getPositionMatrix());
		Matrix3f normalMatrix = new Matrix3f(matrices.peek().getNormalMatrix());
		consumer.vertex(poseMatrix, x, y, 0f).color(color).texture(0, 0).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, x, y + sizeY, 0f).color(color).texture(0, 1).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, x + sizeX, y + sizeY, 0f).color(color).texture(1, 1).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, x + sizeX, y, 0f).color(color).texture(1, 0).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		matrices.pop();
	}
	
	static {
		textRenderer = MinecraftClient.getInstance().textRenderer;
	}
}
