package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.widget.SimpleColorSelectionWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.List;

public class TerminalScreen extends Screen
{
	SimpleColorSelectionWidget textColorPicker;
	TerminalBlockEntity terminal;
	
	public TerminalScreen(TerminalBlockEntity terminal)
	{
		super(Text.of("Terminal"));
		this.terminal = terminal;
	}
	
	@Override
	protected void init()
	{
		textColorPicker = new SimpleColorSelectionWidget(textRenderer, Text.translatable("screen.ultracraft.terminal.customize.text-clr"),
				new Vector3i(width - 160, 32, 155), () -> terminal.getTextColor(),
				i -> terminal.setTextColor((0xff << 24) + i));
		textColorPicker.setAlpha(1f);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		switch (terminal.getTab())
		{
			case CUSTOMIZATION -> {
				textColorPicker.setActive(true);
				textColorPicker.render(context, mouseX, mouseY, delta);
			}
			case GRAFFITI -> {
				for (int i = 0; i < 15; i++)
					context.fill(16, 16 + 16 * i, 32, 32 + 16 * i, terminal.getPaletteColor(i));
			}
			default -> {
				textColorPicker.setActive(false);
			}
		}
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY)
	{
		float f = (100f / width) / 0.23419207f * 0.75f;
		Vector2d v2 = new Vector2d((mouseX - width / 2f) * f + 50f, (mouseY - height / 2f) * f + 50f);
		terminal.setCursor(v2.div(100f));
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	{
		if (textColorPicker.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
			return true;
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		terminal.onHit();
		if (textColorPicker.mouseClicked(mouseX, mouseY, button))
			return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		if (textColorPicker.mouseReleased(mouseX, mouseY, button))
			return true;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(terminal.getTab().equals(TerminalBlockEntity.Tab.EDIT_SCREENSAVER))
		{
			switch(keyCode)
			{
				case 265 -> terminal.setCaret(terminal.getCaret().sub(0, 1)); //up
				case 264 -> terminal.setCaret(terminal.getCaret().add(0, 1)); //down
				case 262 -> terminal.setCaret(terminal.getCaret().add(1, 0)); //right
				case 263 -> terminal.setCaret(terminal.getCaret().sub(1, 0)); //left
				case 259 -> { //backspace
					Vector2i v = terminal.getCaret();
					if(v.x > 0)
					{
						String line = terminal.getLines().get(v.y);
						line = line.substring(0, v.x - 1) + (v.x < line.length() ? line.substring(v.x) : "");
						terminal.getLines().set(v.y, line);
						terminal.setCaret(v.sub(1, 0));
					}
				}
				case 261 -> { //delete
					Vector2i v = terminal.getCaret();
					String line = terminal.getLines().get(v.y);
					if(v.x < line.length())
					{
						line = line.substring(0, v.x) + (v.x < line.length() ? line.substring(v.x + 1) : "");
						terminal.getLines().set(v.y, line);
					}
				}
				case 256, 257 -> terminal.setTab(TerminalBlockEntity.Tab.CUSTOMIZATION); //ESC or Enter
			}
			return true;
		}
		if(terminal.getTab().equals(TerminalBlockEntity.Tab.GRAFFITI) && (keyCode == 256)) //ESC
		{
			terminal.setTab(TerminalBlockEntity.Tab.CUSTOMIZATION);
			return true;
		}
		if(textColorPicker.keyPressed(keyCode, scanCode, modifiers))
			return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers)
	{
		if(terminal.getTab().equals(TerminalBlockEntity.Tab.EDIT_SCREENSAVER))
		{
			Vector2i v = terminal.getCaret();
			List<String> text = terminal.getLines();
			String line = text.get(v.y);
			if(v.x == line.length())
				line += chr;
			else if(v.x == 0)
				line = chr + line;
			else
				line = line.substring(0, v.x) + chr + line.substring(v.x);
			if(textRenderer.getWidth(line) <= 100)
			{
				text.set(v.y, line);
				terminal.setCaret(terminal.getCaret().add(1, 0));
			}
			return true;
		}
		if(textColorPicker.charTyped(chr, modifiers))
			return true;
		return super.charTyped(chr, modifiers);
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	@Override
	public void close()
	{
		super.close();
		if(MinecraftClient.getInstance().player instanceof WingedPlayerEntity winged)
			winged.setFocusedTerminal(null);
	}
}
