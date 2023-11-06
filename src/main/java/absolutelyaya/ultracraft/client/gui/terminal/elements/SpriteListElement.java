package absolutelyaya.ultracraft.client.gui.terminal.elements;

import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class SpriteListElement extends ListElement
{
	final List<Pair<Sprite, String>> elements = new ArrayList<>();
	final int spriteSize;
	public SpriteListElement(int width, int lines, int spriteSize)
	{
		super(width, lines);
		this.spriteSize = spriteSize;
	}
	
	public Pair<Sprite, String> getElement(int idx)
	{
		return elements.get(idx);
	}
	
	public int getSpriteSize()
	{
		return spriteSize;
	}
	
	public List<Pair<Sprite, String>> getElements()
	{
		return elements;
	}
}
