package absolutelyaya.ultracraft.client.gui.widget;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import org.joml.Vector2i;

import java.util.List;
import java.util.function.Consumer;

public class TerminalPaletteWidget implements Element, Drawable
{
	TerminalBlockEntity terminal;
	List<Integer> colors;
	Vector2i pos;
	int optionSize, selectedColor = 0;
	boolean active;
	Consumer<Integer> onSetColor;
	
	public TerminalPaletteWidget(Vector2i pos, int optionSize, TerminalBlockEntity terminal, Consumer<Integer> onSetColor)
	{
		this.terminal = terminal;
		colors = terminal.getPalette();
		this.pos = pos;
		this.optionSize = optionSize;
		this.onSetColor = onSetColor;
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		context.fill(pos.x - 2, pos.y - 2, pos.x + optionSize + 2, pos.y + ((colors.size() + 1) * optionSize) + 2, 0x88000000);
		context.drawBorder(pos.x - 2, pos.y - 2, optionSize + 4, ((colors.size() + 1) * optionSize) + 4, 0xff000000);
		for (int i = 0; i < 16; i++)
		{
			boolean selected = selectedColor == i;
			int x = pos.x + (selected ? optionSize / 2 : 0), y = pos.y + optionSize * i;
			if(!selected && mouseX > x && mouseX < x + optionSize && mouseY > y && mouseY < y + optionSize)
				x += pos.x / 3;
			context.fill(x, y, x + optionSize, y + optionSize, terminal.getPaletteColor(i));
			if(selected)
			{
				context.getMatrices().push();
				context.getMatrices().translate(0f, 0f, 0.1f);
				context.drawBorder(x - 1, y - 1, optionSize + 2, optionSize + 2, 0xffffffff);
				context.getMatrices().pop();
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(!active)
			return false;
		for (int i = 0; i < 16; i++)
		{
			boolean selected = selectedColor == i;
			int x = pos.x + (selected ? optionSize / 2 : 0), y = pos.y + optionSize * i;
			if(!selected && mouseX > x && mouseX < x + optionSize && mouseY > y && mouseY < y + optionSize)
			{
				selectedColor = i;
				if(i >= 0)
					onSetColor.accept(i);
				return true;
			}
		}
		return false;
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
	
	public int getSelectedColor()
	{
		return selectedColor;
	}
	
	public void setActive(boolean b)
	{
		active = b;
	}
	
	public void setSelectedColor(int idx)
	{
		selectedColor = idx;
	}
}
