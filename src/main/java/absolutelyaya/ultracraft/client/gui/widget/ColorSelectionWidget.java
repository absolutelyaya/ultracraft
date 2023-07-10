package absolutelyaya.ultracraft.client.gui.widget;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WidgetAccessor;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.util.RenderingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ColorSelectionWidget implements Element, Drawable, Selectable, WidgetAccessor
{
	Function<Boolean, Vec3d> startColorSupplier;
	TextRenderer textRenderer;
	Text title;
	int x, y, width, height, offsetX, offsetY;
	ChannelSlider red, green, blue;
	TextFieldWidget hexField;
	boolean type, showTypeSwitch;
	ButtonWidget reset, revert, typeSwitch;
	List<Drawable> children = new ArrayList<>();
	float alpha;
	
	
	
	public ColorSelectionWidget(TextRenderer textRenderer, Vector3i dimensions, boolean type, Function<Boolean, Vec3d> startColorSupplier)
	{
		this.textRenderer = textRenderer;
		x = dimensions.x;
		y = dimensions.y;
		width = dimensions.z;
		height = 102;
		this.startColorSupplier = startColorSupplier;
		
		red = new ChannelSlider(textRenderer, x + 2, y + 14, 108, 20, Text.translatable("screen.ultracraft.wing-settings.red"), 0.5, 0);
		green = new ChannelSlider(textRenderer, x + 2, y + 36, 108, 20, Text.translatable("screen.ultracraft.wing-settings.green"), 0.5, 1);
		blue = new ChannelSlider(textRenderer, x + 2, y + 58, 108, 20, Text.translatable("screen.ultracraft.wing-settings.blue"), 0.5, 2);
		
		hexField = new HexTextField(textRenderer, x + 112, y + 60, 80, 16, Text.of("HexField"));
		hexField.setPlaceholder(Text.of("hex"));
		hexField.setDrawsBackground(false);
		hexField.setChangedListener(this::updateSliders);
		setType(type);
		
		reset = ButtonWidget.builder(Text.translatable("screen.ultracraft.wing-settings.reset"), button -> {
			int idx = getPickerType() ? 1 : 0;
			UltracraftClient.setWingColor(UltracraftClient.getDefaultWingColors()[idx], idx);
			setType(getPickerType());
			UltracraftClient.wingPreset = "";
		}).dimensions(x + 2, y + 80, 155 / 2 - 2, 20).build();
		revert = ButtonWidget.builder(Text.translatable("screen.ultracraft.wing-settings.revert"), button -> {
			int idx = getPickerType() ? 1 : 0;
			UltracraftClient.setWingColor(startColorSupplier.apply(type), idx);
			setType(getPickerType());
		}).dimensions(x + 1 + 155 / 2, y + 80, 155 / 2 - 2, 20).build();
		typeSwitch = ButtonWidget.builder(Text.of(">"), button -> setType(!getPickerType())).dimensions(x + width - 13, y, 13, 13).build();
		
		children.add(red);
		children.add(green);
		children.add(blue);
		children.add(hexField);
		children.add(reset);
		children.add(revert);
		children.add(typeSwitch);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		int x = this.x;
		int y = this.y;
		x += offsetX;
		y += offsetY;
		
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		context.fill(x, y, x + width, y + height, 0x66000000);
		context.drawBorder(x, y, width, height, 0xff000000);
		context.fill(x, y, x + width, y + 13, 0x88000000);
		context.drawCenteredTextWithShadow(textRenderer, title, x + width / 2, y + 3, 0xffffffff);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		red.render(context, mouseX, mouseY, delta);
		green.render(context, mouseX, mouseY, delta);
		blue.render(context, mouseX, mouseY, delta);
		
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		context.fill(x + 111, y + 14 + 45, x + 111 + 42, y + 14 + 45 + 19, 0xff000000);
		context.drawBorder(x + 111, y + 58, 42, 20, 0xff888888);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(2, 4, 0);
		hexField.render(context, mouseX, mouseY, delta);
		matrices.pop();
		
		ShaderProgram wingShader = UltracraftClient.getWingsColoredUIShaderProgram();
		wingShader.getUniform("WingColor").set((float)red.getValue(), (float)green.getValue(), (float)blue.getValue());
		wingShader.getUniform("MetalColor").set((float)red.getValue(), (float)green.getValue(), (float)blue.getValue());
		RenderSystem.setShader(UltracraftClient::getWingsColoredUIShaderProgram);
		RenderSystem.setShaderTexture(0, new Identifier(Ultracraft.MOD_ID, "textures/gui/clr_preview.png"));
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		RenderingUtil.drawTexture(new Matrix4f(matrices.peek().getPositionMatrix()), new Vector4f(x + 112, y + 15, 40, 40), new Vec2f(32, 64),
				new Vector4f(type ? 16f : 0f, 0f, 16f, 16f));
		context.drawBorder(x + 111, y + 14, 42, 42, 0xff000000);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		reset.render(context, mouseX, mouseY, delta);
		revert.render(context, mouseX, mouseY, delta);
		typeSwitch.visible = typeSwitch.active = showTypeSwitch;
		typeSwitch.render(context, mouseX, mouseY, delta);
	}
	
	void updateHex(boolean initial)
	{
		String value = hexField.getText();
		int c = (int)(red.getValue() * 0xff);
		c = (c << 8) + (int)(green.getValue() * 0xff);
		c = (c << 8) + (int)(blue.getValue() * 0xff);
		String hex = Integer.toHexString(c);
		hexField.setText((StringUtils.repeat('0', 6 - hex.length())) + hex);
		if(hexField.getText().equals(value))
			return;
		
		red.setFullColor(c);
		green.setFullColor(c);
		blue.setFullColor(c);
		
		UltracraftClient.setWingColor(new Vec3d(red.getValue() * 0xff, green.getValue() * 0xff, blue.getValue() * 0xff), type ? 1 : 0);
		if(!initial)
			UltracraftClient.wingPreset = "";
	}
	
	void updateSliders(String hex)
	{
		if(hex.length() < 6)
			return;
		int r = Integer.parseInt(hex.substring(0, 2), 16);
		int g = Integer.parseInt(hex.substring(2, 4), 16);
		int b = Integer.parseInt(hex.substring(4, 6), 16);
		
		int lastC = ((int)(red.getValue() * 255) << 8) + (int)(green.getValue() * 255);
		lastC = (lastC << 8) + (int)(blue.getValue() * 255);
		int c = (r << 8) + g;
		c = (c << 8) + b;
		
		if(lastC == c)
			return;
		
		red.setValue(r / 255f);
		green.setValue(g / 255f);
		blue.setValue(b / 255f);
		
		red.setFullColor(c);
		green.setFullColor(c);
		blue.setFullColor(c);
		
		UltracraftClient.setWingColor(new Vec3d(r, g, b), type ? 1 : 0);
		UltracraftClient.wingPreset = "";
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		updateHex(false);
		for (Drawable c : children)
		{
			if(c instanceof ClickableWidget clickable && clickable.isMouseOver(mouseX, mouseY))
				if(clickable.mouseClicked(mouseX, mouseY, button))
					return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	{
		updateHex(false);
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
		for (Drawable c : children)
		{
			if(c instanceof ClickableWidget clickable)
				if(clickable.mouseReleased(mouseX, mouseY, button))
					return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		for (Drawable c : children)
		{
			if(c instanceof ClickableWidget clickable)
				if(clickable.mouseScrolled(mouseX, mouseY, amount))
					return true;
		}
		return false;
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
	
	@Override
	public Vector2i getOffset()
	{
		return new Vector2i(offsetX, offsetY);
	}
	
	@Override
	public void setOffset(Vector2i offset)
	{
		offsetX = offset.x;
		offsetY = offset.y;
		children.forEach(w -> ((WidgetAccessor)w).setOffset(offset));
	}
	
	@Override
	public void setActive(boolean b)
	{
		children.forEach(w -> ((WidgetAccessor)w).setActive(b));
	}
	
	@Override
	public void setAlpha(float alpha)
	{
		this.alpha = alpha;
		children.forEach(w -> ((WidgetAccessor)w).setAlpha(alpha));
	}
	
	public void setType(boolean b)
	{
		type = b;
		title = type ? Text.translatable("screen.ultracraft.wing-settings.metal") : Text.translatable("screen.ultracraft.wing-settings.wing");
		
		Vec3d[] colors = UltracraftClient.getWingColors();
		if(type)
		{
			red.setValue(colors[1].x / 255f);
			green.setValue(colors[1].y / 255f);
			blue.setValue(colors[1].z / 255f);
		}
		else
		{
			red.setValue(colors[0].x / 255f);
			green.setValue(colors[0].y / 255f);
			blue.setValue(colors[0].z / 255f);
		}
		updateHex(true);
	}
	
	public boolean getPickerType()
	{
		return type;
	}
	
	public void setShowTypeSwitch(boolean b)
	{
		showTypeSwitch = b;
	}
	
	static class ChannelSlider extends SliderWidget
	{
		TextRenderer textRenderer;
		int colorIdx, fullColor = 0x000000;
		
		public ChannelSlider(TextRenderer textRenderer, int x, int y, int width, int height, Text text, double value, int colorIdx)
		{
			super(x, y, width, height, text, value);
			this.colorIdx = colorIdx;
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
		public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
		{
			super.renderButton(context, mouseX, mouseY, delta);
			MatrixStack matrices = context.getMatrices();
			matrices.push();
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(90), 0, 0, 1)));
			int start = getPreviewColor(true);
			int end = getPreviewColor(false);
			RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
			context.fillGradient(getY() + 1, -getX() - width + 1, getY() + height - 1, -getX() - 1,
					start, end);
			matrices.pop();
			int i = (!hovered && !isFocused() ? 2 : 3) * 20;
			context.drawNineSlicedTexture(new Identifier("textures/gui/slider.png"), getX() + (int)(value * (double)(width - 8)), getY(), 8, 20, 20, 4, 200, 20, 0, i);
			i = active ? 16777215 : 10526880;
			drawScrollableText(context, textRenderer, 2, i | MathHelper.ceil(alpha * 255.0F) << 24);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
		
		int getPreviewColor(boolean b)
		{
			int red = (fullColor >> 16) & 0xff;
			int green = (fullColor >> 8) & 0xff;
			int blue = fullColor & 0xff;
			
			//System.out.println(red + " | " + green + " | " + blue);
			
			int c = 0xff;
			c = (c << 8) + (colorIdx == 0 && !b ? (colorIdx != 0 ? red : 0x00) : colorIdx == 0 ? 0xff : red);
			c = (c << 8) + (colorIdx == 1 && !b ? (colorIdx != 1 ? green : 0x00) : colorIdx == 1 ? 0xff : green);
			c = (c << 8) + (colorIdx == 2 && !b ? (colorIdx != 2 ? blue : 0x00) : colorIdx == 2 ? 0xff : blue);
			return c;
		}
		
		public double getValue()
		{
			return value;
		}
		
		public void setFullColor(int v)
		{
			fullColor = v;
		}
		
		public void setValue(double d)
		{
			value = d;
		}
	}
	
	static class HexTextField extends TextFieldWidget
	{
		public HexTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text text)
		{
			super(textRenderer, x, y, width, height, text);
		}
		
		@Override
		protected boolean clicked(double mouseX, double mouseY)
		{
			if(isMouseOver(mouseX, mouseY))
				setFocused(true);
			else if(isFocused())
				setFocused(false);
			return super.clicked(mouseX, mouseY);
		}
		
		@Override
		public boolean charTyped(char chr, int modifiers)
		{
			if (!isActive())
				return false;
			else if (isValidChar(chr))
			{
				if (active)
					write(Character.toString(chr));
				return true;
			}
			else
				return false;
		}
		
		boolean isValidChar(char c)
		{
			if(getText().length() >= 6)
				return false;
			if((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))
				return SharedConstants.isValidChar(c);
			else
				return false;
		}
		
		@Override
		public void write(String text)
		{
			for (char c : text.toCharArray())
			{
				if(isValidChar(c))
					super.write(String.valueOf(c));
			}
		}
	}
}
