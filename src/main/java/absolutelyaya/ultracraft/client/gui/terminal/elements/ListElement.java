package absolutelyaya.ultracraft.client.gui.terminal.elements;

import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListElement implements Element
{
	final List<String> entries = new ArrayList<>();
	final int width, lines;
	
	double scroll;
	int selected = -1, lastHovered;
	Consumer<Integer> onSelect;
	boolean selectable = true;
	
	public ListElement(int width, int lines)
	{
		this.width = width;
		this.lines = lines;
	}
	
	public List<String> getEntries()
	{
		return entries;
	}
	
	public void onScroll(double amount)
	{
		scroll = MathHelper.clamp(scroll - amount, 0, Math.max(entries.size() - lines, 0));
	}
	
	public double getScroll()
	{
		return scroll;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getLines()
	{
		return lines;
	}
	
	public void select(int idx)
	{
		if(!selectable)
			return;
		if(idx < entries.size())
			selected = idx;
		if(onSelect != null)
			onSelect.accept(idx);
	}
	
	public int getSelected()
	{
		return selected;
	}
	
	public void setSelectable(boolean b)
	{
		selectable = b;
	}
	
	public boolean isSelectable()
	{
		return selectable;
	}
	
	public void setLastHovered(int i)
	{
		lastHovered = i;
	}
	
	public int getLastHovered()
	{
		return lastHovered;
	}
	
	public void setOnSelect(Consumer<Integer> onSelect)
	{
		this.onSelect = onSelect;
	}
}
