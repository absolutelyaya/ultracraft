package absolutelyaya.ultracraft.client.gui.terminal.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TextBox implements Element
{
	final List<String> lines = new ArrayList<>();
	final int maxLines, maxLength;
	final boolean allowTranslations, centered;
	
	public TextBox(int maxLines, int maxLength, boolean allowTranslations, boolean centered)
	{
		this.maxLines = maxLines;
		for (int i = 0; i < maxLines; i++)
			lines.add("");
		this.maxLength = maxLength;
		this.allowTranslations = allowTranslations;
		this.centered = centered;
	}
	
	public List<String> getLines()
	{
		return lines;
	}
	
	public int getMaxLines()
	{
		return maxLines;
	}
	
	public int getMaxLength()
	{
		return maxLength;
	}
	
	public boolean isCentered()
	{
		return centered;
	}
	
	public boolean fits(String s)
	{
		TextRenderer t = MinecraftClient.getInstance().textRenderer;
		return (t.getWidth(s) < maxLength && (!allowTranslations || t.getWidth(Text.translatable(s).getString()) < maxLength));
	}
	
	public boolean hasTranslation(String s)
	{
		TextRenderer t = MinecraftClient.getInstance().textRenderer;
		return allowTranslations && t.getWidth(s) != t.getWidth(Text.translatable(s).getString());
	}
	
	public void unfocus()
	{
		for (int i = 0; i < lines.size(); i++)
		{
			if(!fits(lines.get(i)))
				lines.set(i, "");
		}
	}
}
