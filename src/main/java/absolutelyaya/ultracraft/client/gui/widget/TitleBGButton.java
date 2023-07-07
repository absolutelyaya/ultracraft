package absolutelyaya.ultracraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TitleBGButton extends TexturedButtonWidget
{
	static TitleBGButton lastPressed;
	final float originalX;
	float hoverTime, actualXPos, desiredXPos;
	boolean wasLastPressed, wasHovered;
	
	public TitleBGButton(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, PressAction pressAction, Text message)
	{
		super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, message);
		originalX = actualXPos = desiredXPos = x;
	}
	
	@Override
	public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
	{
		drawTexture(context, texture, getX(), getY(), u, v, hoveredVOffset, width, height, textureWidth, textureHeight);
		if(wasLastPressed && !this.equals(lastPressed))
		{
			desiredXPos = originalX;
			wasLastPressed = false;
		}
		if((isHovered() || isFocused()) && !wasHovered && !wasLastPressed)
		{
			desiredXPos = originalX + 16;
			wasHovered = true;
		}
		if(!(isHovered() || isFocused()) && wasHovered && !wasLastPressed)
		{
			desiredXPos = originalX;
			wasHovered = false;
		}
		if(MinecraftClient.getInstance().currentScreen == null)
			return;
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		int textX = getX() + 34 + renderer.getWidth(getMessage()) / 2;
		context.drawCenteredTextWithShadow(renderer, getMessage(),
				textX, getY() + 12, Color.ofRGBA(1f, 1f, 1f, hoverTime).getColor());
		hoverTime = MathHelper.clamp(hoverTime + ((isHovered() || isFocused()) ? delta : -delta) / 2f, 0.05f, 1f);
		setX((int)(actualXPos = MathHelper.lerp(delta / 4f, actualXPos, desiredXPos)));
	}
	
	@Override
	public void drawTexture(DrawContext context, Identifier texture, int x, int y, int u, int v, int hoveredVOffset, int width, int height, int textureWidth, int textureHeight)
	{
		int i = v;
		if (isSelected())
			i += hoveredVOffset * 2;
		if(isFocused())
			i += hoveredVOffset;
		
		RenderSystem.enableDepthTest();
		context.drawTexture(texture, x, y, (float)u, (float)i, width, height, textureWidth, textureHeight);
	}
	
	@Override
	public void onPress()
	{
		super.onPress();
		if(!this.equals(lastPressed))
			desiredXPos = originalX + 32;
		lastPressed = this;
		wasLastPressed = true;
	}
	
	@Override
	public void onRelease(double mouseX, double mouseY)
	{
		super.onRelease(mouseX, mouseY);
		setFocused(false);
	}
	
	@Override
	public boolean isSelected()
	{
		return this.isHovered() || this.equals(lastPressed);
	}
}
