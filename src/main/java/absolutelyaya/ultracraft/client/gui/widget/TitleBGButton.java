package absolutelyaya.ultracraft.client.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib3.core.util.Color;

public class TitleBGButton extends TexturedButtonWidget
{
	static TitleBGButton lastPressed;
	float hoverTime, originalX, actualXPos, desiredXPos;
	boolean wasLastPressed, wasHovered;
	
	public TitleBGButton(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, PressAction pressAction, Text message)
	{
		super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, message);
		originalX = actualXPos = desiredXPos = x;
	}
	
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		super.renderButton(matrices, mouseX, mouseY, delta);
		if(wasLastPressed && !this.equals(lastPressed))
		{
			desiredXPos = originalX;
			wasLastPressed = false;
		}
		if(isHovered() && !wasHovered && !wasLastPressed)
		{
			desiredXPos = originalX + 16;
			wasHovered = true;
		}
		if(!isHovered() && wasHovered && !wasLastPressed)
		{
			desiredXPos = originalX;
			wasHovered = false;
		}
		if(MinecraftClient.getInstance().currentScreen == null)
			return;
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		int textX = getX() + 34 + renderer.getWidth(getMessage()) / 2;
		drawCenteredText(matrices, renderer, getMessage(),
				textX, getY() + 12,
				Color.ofRGBA(1f, 1f, 1f, hoverTime).getColor());
		hoverTime = MathHelper.clamp(hoverTime + (isHovered() ? delta : -delta) / 2f, 0.05f, 1f);
		setX((int)(actualXPos = MathHelper.lerp(delta / 4f, actualXPos, desiredXPos)));
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
}
