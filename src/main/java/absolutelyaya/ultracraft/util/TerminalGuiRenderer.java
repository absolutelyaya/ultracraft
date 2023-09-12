package absolutelyaya.ultracraft.util;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Button;
import absolutelyaya.ultracraft.client.gui.terminal.elements.ColorButton;
import absolutelyaya.ultracraft.client.gui.terminal.elements.TextBox;
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
import java.util.List;

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
	
	public void drawTab(MatrixStack matrices, VertexConsumerProvider buffers, String title, Button returnButton)
	{
		drawTab(matrices, buffers, title, "-", returnButton);
	}
	
	public void drawTab(MatrixStack matrices, VertexConsumerProvider buffers, String title, String titleBorder, Button returnButton)
	{
		String t = MessageFormat.format("{0} {1} {0}", titleBorder, Text.translatable(title).getString());
		drawBG(matrices, buffers);
		drawText(buffers, matrices, t, 50 - textRenderer.getWidth(t) / 2, - 100 + textRenderer.fontHeight, 0.01f);
		if(returnButton != null)
			drawButton(buffers, matrices, returnButton);
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
	
	public void drawButton(VertexConsumerProvider buffers, MatrixStack matrices, Button button)
	{
		drawButton(buffers, matrices, button, button.getLabel(), terminal.getTextColor());
	}
	
	public void drawButton(VertexConsumerProvider buffers, MatrixStack matrices, Button button, int color)
	{
		drawButton(buffers, matrices, button, button.getLabel(), color);
	}
	
	public void drawButton(VertexConsumerProvider buffers, MatrixStack matrices, Button button, String label)
	{
		drawButton(buffers, matrices, button, label, terminal.getTextColor());
	}
	
	public void drawButton(VertexConsumerProvider buffers, MatrixStack matrices, Button button, String label, int color)
	{
		String text = Text.translatable(label).getString();
		int x = button.getPos().x, y = button.getPos().y;
		int sizeX = textRenderer.getWidth(text) + 3, sizeY = textRenderer.fontHeight + 2;
		if(button.isCentered())
			x -= sizeX / 2;
		Vector2d cursor = new Vector2d(terminal.getCursor()).mul(100f);
		boolean hovered = cursor.x > x && cursor.x < x + sizeX && cursor.y > y && cursor.y < y + sizeY;
		matrices.push();
		drawBoxOutline(buffers, matrices, x, y, sizeX, sizeY, color);
		if(hovered)
		{
			matrices.translate(0f, 0f, -0.001f);
			drawBox(buffers, matrices, x, y, sizeX, sizeY, color);
			terminal.setLastHovered(button);
		}
		else if(terminal.getLastHovered() != null && terminal.getLastHovered().equals(button))
			terminal.setLastHovered(null);
		matrices.translate(0f, 1f, -0.002f);
		drawText(buffers, matrices, text, x + 2, y + 2, 0f, hovered ? 0xff000000 : color);
		matrices.pop();
	}
	
	public void drawColorButton(VertexConsumerProvider buffers, MatrixStack matrices, int sizeX, int sizeY, ColorButton button)
	{
		int x = button.getPos().x, y = button.getPos().y, color = button.getColor();
		Vector2d cursor = new Vector2d(terminal.getCursor()).mul(100f);
		boolean hovered = cursor.x > x && cursor.x < x + sizeX && cursor.y > y && cursor.y < y + sizeY;
		matrices.push();
		drawBox(buffers, matrices, x, y, sizeX, sizeY, color, 0.005f);
		if(hovered)
		{
			drawBoxOutline(buffers, matrices, x, y, sizeX, sizeY, terminal.getTextColor());
			terminal.setLastHovered(button);
		}
		else if(terminal.getLastHovered() != null && terminal.getLastHovered().equals(button))
			terminal.setLastHovered(null);
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
	
	public void drawTextBox(VertexConsumerProvider buffers, MatrixStack matrices, int x, int y, TextBox box, int color)
	{
		int sizeX = box.getMaxLength(), sizeY = box.getMaxLines() * (textRenderer.fontHeight) + 2;
		Vector2d cursor = new Vector2d(terminal.getCursor()).mul(100f);
		boolean hovered = cursor.x > x && cursor.x < x + sizeX && cursor.y > y && cursor.y < y + sizeY;
		boolean centered = box.isCentered();
		matrices.push();
		//drawBox(buffers, matrices, x, y, sizeX, sizeY, color, 0.005f);
		if(hovered)
			terminal.setLastHovered(box);
		else if(terminal.getLastHovered() != null && terminal.getLastHovered().equals(box))
			terminal.setLastHovered(null);
		
		drawBoxOutline(buffers, matrices, x, y, sizeX, sizeY, color);
		List<String> lines = box.getLines();
		for (int i = 0; i < lines.size(); i++)
		{
			String s = lines.get(i);
			boolean translation = box.hasTranslation(s);
			int l = textRenderer.getWidth(s);
			int xOffset = 2 + (centered ? (box.getMaxLength() / 2 - l / 2) : 0);
			if(box.hasTranslation(s) && !box.equals(terminal.getFocusedTextbox()))
			{
				s = Text.translatable(s).getString();
				l = textRenderer.getWidth(s);
				xOffset = 2 + (centered ? (box.getMaxLength() / 2 - l / 2) : 0);
				drawText(buffers, matrices, s, x + xOffset, y + textRenderer.fontHeight * (i + 1) - 107, 0.001f, 0xffffff00);
			}
			else
				drawText(buffers, matrices, s, x + xOffset, y + textRenderer.fontHeight * (i + 1) - 107, 0.002f, translation ? 0xff00ff00 : 0xffffffff);
		}
		if(box.equals(terminal.getFocusedTextbox()) && terminal.getCaretTimer() <= 1f)
		{
			Vector2i caret = terminal.getCaret();
			String before = (caret.x == 0 || lines.get(caret.y).length() == 0 ? "" : lines.get(caret.y).substring(0, caret.x));
			matrices.push();
			matrices.translate(0f, 0f, -0.003f);
			int l = textRenderer.getWidth(lines.get(caret.y));
			int xOffset = 1 + (centered ? (box.getMaxLength() / 2 - l / 2) : 0);
			drawBox(buffers, matrices, x + textRenderer.getWidth(before) + xOffset, y + textRenderer.fontHeight * caret.y + 1, 1, textRenderer.fontHeight,
					terminal.getTextColor());
			matrices.pop();
		}
		matrices.pop();
	}
	
	static {
		textRenderer = MinecraftClient.getInstance().textRenderer;
	}
}
