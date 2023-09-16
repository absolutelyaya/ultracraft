package absolutelyaya.ultracraft.client.gui.terminal.elements;

import org.joml.Vector2i;

public class ColorButton extends Button
{
	final int color;
	
	public ColorButton(Vector2i position, String action, int value, boolean hide, boolean centered, int color)
	{
		super("", position, action, value, centered);
		setHide(hide);
		this.color = color;
	}
	
	public int getColor()
	{
		return color;
	}
}
