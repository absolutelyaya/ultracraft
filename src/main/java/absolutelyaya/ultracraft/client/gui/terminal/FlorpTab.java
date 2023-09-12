package absolutelyaya.ultracraft.client.gui.terminal;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector2f;

public class FlorpTab extends TerminalBlockEntity.Tab
{
	public static final String ID = "FLORP";
	boolean mainMenu = true;
	
	public FlorpTab()
	{
		super(ID);
	}
	
	@Override
	public Vector2f getSizeOverride()
	{
		return super.getSizeOverride();
	}
	
	@Override
	public void renderCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		if(mainMenu)
			drawMainMenu(matrices, terminal, buffers);
		else
			GUI.drawTab(matrices, buffers, "placeholder", ":D", false, "mainmenu");
	}
	
	void drawMainMenu(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "FLORP", true);
		String t = "Start Game";
		int tWidth = textRenderer.getWidth(t);
		GUI.drawButton(buffers, matrices, t, 50 - tWidth / 2, 30, tWidth + 2, textRenderer.fontHeight + 2, "start");
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
