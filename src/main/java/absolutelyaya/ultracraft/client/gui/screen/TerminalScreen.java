package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class TerminalScreen extends Screen
{
	
	public TerminalScreen(Text title)
	{
		super(title);
	}
	
	@Override
	public void close()
	{
		super.close();
		if(MinecraftClient.getInstance().player instanceof WingedPlayerEntity winged)
			winged.setFocusedTerminal(null);
	}
}
