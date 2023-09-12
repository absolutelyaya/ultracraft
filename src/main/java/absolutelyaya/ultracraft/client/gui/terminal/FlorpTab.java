package absolutelyaya.ultracraft.client.gui.terminal;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Button;
import absolutelyaya.ultracraft.api.terminal.Tab;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector2f;
import org.joml.Vector2i;

public class FlorpTab extends Tab
{
	public static final String ID = "FLORP";
	boolean mainMenu = true;
	Button startGameButton;
	
	public FlorpTab()
	{
		super(ID);
		startGameButton = new Button("Start Game", new Vector2i(50, 30), "start", 0, false, true);
	}
	
	@Override
	public Vector2f getSizeOverride()
	{
		return super.getSizeOverride();
	}
	
	@Override
	public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		if(mainMenu)
			drawMainMenu(matrices, buffers);
		else
			GUI.drawTab(matrices, buffers, "placeholder", ":D", DEFAULT_RETURN_BUTTON);
	}
	
	void drawMainMenu(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "FLORP", DEFAULT_RETURN_BUTTON);
		GUI.drawButton(buffers, matrices, startGameButton);
	}
	
	@Override
	public boolean onButtonClicked(String action, int value)
	{
		if(action.equals("start"))
		{
			mainMenu = false;
			return true;
		}
		return super.onButtonClicked(action, value);
	}
}
