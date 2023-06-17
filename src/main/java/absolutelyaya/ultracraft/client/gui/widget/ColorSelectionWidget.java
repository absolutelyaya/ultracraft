package absolutelyaya.ultracraft.client.gui.widget;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.UltracraftClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.joml.*;

import java.lang.Math;

public class ColorSelectionWidget extends DrawableHelper implements Element, Drawable, Selectable
{
	TextRenderer textRenderer;
	Text title;
	int x, y, width, height;
	ChannelSlider red, green, blue;
	TextFieldWidget hexField;
	boolean type;
	
	public ColorSelectionWidget(TextRenderer textRenderer, Text title, Vector3i dimensions, boolean type)
	{
		this.textRenderer = textRenderer;
		this.title = title;
		x = dimensions.x;
		y = dimensions.y;
		width = dimensions.z;
		height = 80;
		this.type = type;
		
		red = new ChannelSlider(textRenderer, x + 2, y + 14, 108, 20, Text.of("Red"), 0.5, 0xffff0000);
		green = new ChannelSlider(textRenderer, x + 2, y + 36, 108, 20, Text.of("Green"), 0.5, 0xff00ff00);
		blue = new ChannelSlider(textRenderer, x + 2, y + 58, 108, 20, Text.of("Blue"), 0.5, 0xff0000ff);
		
		hexField = new HexTextField(textRenderer, x + 112, y + 60, 80, 16, Text.of("HexField"));
		hexField.setPlaceholder(Text.of("hex"));
		hexField.setDrawsBackground(false);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		fill(matrices, x, y, x + width, y + height, 0x66000000);
		drawBorder(matrices, x, y, width, height, 0xff000000);
		fill(matrices, x, y, x + width, y + 13, 0x88000000);
		drawCenteredTextWithShadow(matrices, textRenderer, title, x + width / 2, y + 3, 0xffffffff);
		
		red.render(matrices, mouseX, mouseY, delta);
		green.render(matrices, mouseX, mouseY, delta);
		blue.render(matrices, mouseX, mouseY, delta);
		
		fill(matrices, x + 111, y + 14 + 45, x + 111 + 42, y + 14 + 45 + 19, 0xff000000);
		drawBorder(matrices, x + 111, y + 58, 42, 20, 0xff888888);
		matrices.push();
		matrices.translate(1, 4, 0);
		hexField.render(matrices, mouseX, mouseY, delta);
		matrices.pop();
		
		ShaderProgram wingShader = UltracraftClient.getWingsColoredUIShaderProgram();
		wingShader.getUniform("WingColor").set((float)red.getValue(), (float)green.getValue(), (float)blue.getValue());
		wingShader.getUniform("MetalColor").set((float)red.getValue(), (float)green.getValue(), (float)blue.getValue());
		//RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShader(UltracraftClient::getWingsColoredUIShaderProgram);
		RenderSystem.setShaderTexture(0, new Identifier(Ultracraft.MOD_ID, "textures/gui/clr-preview.png"));
		drawTexture(new Matrix4f(matrices.peek().getPositionMatrix()), new Vector4f(x + 112, y + 15, 40, 40), new Vec2f(32, 32),
				new Vector4f(type ? 16f : 0f, 0f, 16f, 16f));
		drawBorder(matrices, x + 111, y + 14, 42, 42, 0xff000000);
		//fill(matrices, x + 111, y + 14, x + 111 + 42, y + 14 + 42, c);
	}
	
	void updateHex()
	{
		int c = (int)(red.getValue() * 0xff);
		c = (c << 8) + (int)(green.getValue() * 0xff);
		c = (c << 8) + (int)(blue.getValue() * 0xff);
		hexField.setText(Integer.toHexString(c));
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		updateHex();
		if (red.mouseClicked(mouseX, mouseY, button))
			return true;
		if (green.mouseClicked(mouseX, mouseY, button))
			return true;
		if (blue.mouseClicked(mouseX, mouseY, button))
			return true;
		return hexField.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	{
		updateHex();
		if(red.isMouseOver(mouseX, mouseY))
			return red.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		if(green.isMouseOver(mouseX, mouseY))
			return green.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		if(blue.isMouseOver(mouseX, mouseY))
			return blue.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		if (red.mouseReleased(mouseX, mouseY, button))
			return true;
		if (green.mouseReleased(mouseX, mouseY, button))
			return true;
		if (blue.mouseReleased(mouseX, mouseY, button))
			return true;
		return hexField.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		if (red.mouseScrolled(mouseX, mouseY, amount))
			return true;
		if (green.mouseScrolled(mouseX, mouseY, amount))
			return true;
		if (blue.mouseScrolled(mouseX, mouseY, amount))
			return true;
		return hexField.mouseScrolled(mouseX, mouseY, amount);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		return hexField.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers)
	{
		return hexField.charTyped(chr, modifiers);
	}
	
	@Override
	public void setFocused(boolean focused)
	{
	
	}
	
	@Override
	public boolean isFocused()
	{
		return false;
	}
	
	@Override
	public SelectionType getType()
	{
		return SelectionType.NONE;
	}
	
	@Override
	public void appendNarrations(NarrationMessageBuilder builder)
	{
	
	}
	
	void drawTexture(Matrix4f matrix, Vector4f transform, Vec2f textureSize, Vector4f uv)
	{
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		uv = new Vector4f(uv.x() / textureSize.x, uv.y() / textureSize.y,
				uv.z() / textureSize.x, uv.w() / textureSize.y);
		bufferBuilder.vertex(matrix, transform.x(), transform.y() + transform.w(), 0)
				.texture(uv.x(), uv.y()).next();
		bufferBuilder.vertex(matrix, transform.x() + transform.z(), transform.y() + transform.w(), 0)
				.texture(uv.x() + uv.z(), uv.y()).next();
		bufferBuilder.vertex(matrix, transform.x() + transform.z(), transform.y(), 0)
				.texture(uv.x() + uv.z(), uv.y() + uv.w()).next();
		bufferBuilder.vertex(matrix, transform.x(), transform.y(), 0)
				.texture(uv.x(), uv.y() + uv.w()).next();
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		BufferRenderer.resetCurrentVertexBuffer();
	}
	
	static class ChannelSlider extends SliderWidget
	{
		TextRenderer textRenderer;
		int color;
		
		public ChannelSlider(TextRenderer textRenderer, int x, int y, int width, int height, Text text, double value, int color)
		{
			super(x, y, width, height, text, value);
			this.color = color;
			this.textRenderer = textRenderer;
		}
		
		@Override
		protected void updateMessage()
		{
		
		}
		
		@Override
		protected void applyValue()
		{
		
		}
		
		@Override
		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta)
		{
			super.renderButton(matrices, mouseX, mouseY, delta);
			matrices.push();
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(90), 0, 0, 1)));
			fillGradient(matrices, getY() + 1, -getX() - width + 1, getY() + height - 1, -getX() - 1, color, 0xff000000);
			matrices.pop();
			int i = (!hovered && !isFocused() ? 2 : 3) * 20;
			RenderSystem.setShaderTexture(0, new Identifier("textures/gui/slider.png"));
			drawNineSlicedTexture(matrices, getX() + (int)(value * (double)(width - 8)), getY(), 8, 20, 20, 4, 200, 20, 0, i);
			i = active ? 16777215 : 10526880;
			drawScrollableText(matrices, textRenderer, 2, i | MathHelper.ceil(alpha * 255.0F) << 24);
		}
		
		public double getValue()
		{
			return value;
		}
	}
	
	static class HexTextField extends TextFieldWidget
	{
		boolean editable;
		
		public HexTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text text)
		{
			super(textRenderer, x, y, width, height, text);
		}
		
		@Override
		public boolean charTyped(char chr, int modifiers)
		{
			if (!isActive())
				return false;
			else if (isValidChar(chr))
			{
				if (editable)
					write(Character.toString(chr));
				return true;
			}
			else
				return false;
		}
		
		boolean isValidChar(char c)
		{
			if(getText().length() >= 7)
			{
				System.out.println("maxLength reached");
				return false;
			}
			if((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))
				return SharedConstants.isValidChar(c);
			else
			{
				System.out.println("char out of range");
				return false;
			}
		}
	}
}
