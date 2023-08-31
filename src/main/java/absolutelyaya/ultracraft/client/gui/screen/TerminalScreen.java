package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.joml.Vector2d;
import org.joml.Vector2f;

public class TerminalScreen extends Screen
{
	TerminalBlockEntity terminal;
	
	public TerminalScreen(TerminalBlockEntity terminal)
	{
		super(Text.of("Terminal"));
		this.terminal = terminal;
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY)
	{
		float f = (100f / width) / 0.23419207f * 0.75f;
		Vector2d v2 = new Vector2d((mouseX - width / 2f) * f + 50f, (mouseY - height / 2f) * f + 50f);
		terminal.setCursor(v2.div(100f));
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		terminal.onHit();
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public void close()
	{
		super.close();
		if(MinecraftClient.getInstance().player instanceof WingedPlayerEntity winged)
			winged.setFocusedTerminal(null);
	}
}
