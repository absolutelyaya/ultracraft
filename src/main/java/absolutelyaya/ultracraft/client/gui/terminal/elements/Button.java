package absolutelyaya.ultracraft.client.gui.terminal.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.joml.Vector2i;

public class Button implements Element
{
	public static final String RETURN_LABEL = "terminal.return";
	
	protected String label, action;
	protected TextMode labelMode = TextMode.LEFTBOUND;
	protected Vector2i position, size;
	protected int value, color = -1;
	protected boolean hide, centered, drawBorder = true, clickable = true;
	protected Sprite sprite;
	
	public Button(String label, Vector2i position, String action, int value, boolean centered)
	{
		this.label = label;
		this.action = action;
		this.position = position;
		this.value = value;
		this.centered = centered;
	}
	
	public Button(Sprite sprite, Vector2i position, String action, int value, boolean centered)
	{
		this.sprite = sprite;
		this.action = action;
		this.position = position;
		this.value = value;
		this.centered = centered;
	}
	
	public Button(Vector2i position, Vector2i size, String action, int value)
	{
		this.position = position;
		this.action = action;
		this.value = value;
	}
	
	public TextMode getLabelMode()
	{
		return labelMode;
	}
	
	public Button setLabelMode(TextMode labelMode)
	{
		this.labelMode = labelMode;
		return this;
	}
	
	public boolean isDrawBorder()
	{
		return drawBorder;
	}
	
	public Button setDrawBorder(boolean drawBorder)
	{
		this.drawBorder = drawBorder;
		return this;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public Button setLabel(String s)
	{
		label = s;
		return this;
	}
	
	public Button setSprite(Sprite s)
	{
		sprite = s;
		return this;
	}
	
	public String getAction()
	{
		return action;
	}
	
	public Vector2i getPos()
	{
		return position;
	}
	
	public boolean isHide()
	{
		return hide;
	}
	
	public Button setHide(boolean hide)
	{
		this.hide = hide;
		return this;
	}
	
	public void toggleHide()
	{
		hide = !hide;
	}
	
	public boolean isCentered()
	{
		return centered;
	}
	
	public Button setCentered(boolean centered)
	{
		this.centered = centered;
		return this;
	}
	
	public void toggleCentered()
	{
		centered = !centered;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public Button setValue(int v)
	{
		value = v;
		return this;
	}
	
	public Vector2i getSize()
	{
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		
		if(size == null)
		{
			if(label != null && label.length() > 0)
				return new Vector2i(textRenderer.getWidth(Text.translatable(label).getString()) + 3, textRenderer.fontHeight + 2);
			else if(sprite != null)
				return sprite.getSize();
			else
				return new Vector2i(10, 10);
		}
		return size;
	}
	
	public Sprite getSprite()
	{
		return sprite;
	}
	
	public boolean isClickable()
	{
		return clickable;
	}
	
	public Button setClickable(boolean clickable)
	{
		this.clickable = clickable;
		return this;
	}
	
	public int getColor()
	{
		return color;
	}
	
	public Button setColor(int color)
	{
		this.color = color;
		return this;
	}
	
	public NbtCompound serialize()
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putString("label", label);
		nbt.putInt("x", position.x);
		nbt.putInt("y", position.y);
		nbt.putString("action", action);
		nbt.putInt("value", value);
		nbt.putBoolean("hide", hide);
		nbt.putBoolean("centered", centered);
		return nbt;
	}
	
	public static Button deserialize(NbtCompound nbt)
	{
		String label = nbt.getString("label");
		Vector2i pos = new Vector2i(nbt.getInt("x"), nbt.getInt("y"));
		String action = nbt.getString("action");
		int value = nbt.getInt("value");
		boolean hide = nbt.getBoolean("hide");
		boolean centered = nbt.getBoolean("centered");
		return new Button(label, pos, action, value, centered).setHide(hide);
	}
	
	public enum TextMode
	{
		LEFTBOUND,
		RIGHTBOUND,
		CENTERED
	}
}
