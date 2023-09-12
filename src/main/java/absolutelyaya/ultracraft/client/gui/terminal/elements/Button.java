package absolutelyaya.ultracraft.client.gui.terminal.elements;

import net.minecraft.nbt.NbtCompound;
import org.joml.Vector2i;

public class Button implements Element
{
	public static final String RETURN_LABEL = "terminal.return";
	
	protected String label, action;
	protected Vector2i position;
	protected int value;
	protected boolean hide, centered;
	
	public Button(String label, Vector2i position, String action, int value, boolean hide, boolean centered)
	{
		this.label = label;
		this.action = action;
		this.position = position;
		this.value = value;
		this.hide = hide;
		this.centered = centered;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public void setLabel(String s)
	{
		label = s;
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
	
	public void toggleHide()
	{
		hide = !hide;
	}
	
	public boolean isCentered()
	{
		return centered;
	}
	
	public void toggleCentered()
	{
		centered = !centered;
	}
	
	public int getValue()
	{
		return value;
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
		return new Button(label, pos, action, value, hide, centered);
	}
}
