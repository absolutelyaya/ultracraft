package absolutelyaya.ultracraft.client.gui.terminal;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Button;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Sprite;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Tab;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Vector2i;

public class CybergrindTab extends Tab
{
	public static String ID = "CYBERGRIND";
	public static Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/terminal/cybergrind.png");
	
	Button startButton = new Button(new Sprite(TEXTURE, new Vector2i(), 0.0005f, new Vector2i(52, 16), new Vector2i(24, 74), new Vector2i(100, 100)),
										new Vector2i(24, 74), "redstone", 15, false);
	
	public CybergrindTab()
	{
		super(ID);
	}
	
	@Override
	public void init(TerminalBlockEntity terminal)
	{
		super.init(terminal);
		startButton.setDrawBorder(false);
	}
	
	@Override
	public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		GUI.drawBG(matrices, buffers);
		GUI.drawSprite(buffers, matrices, TEXTURE, new Vector2i(), 0.002f, new Vector2i(), new Vector2i(100, 100), new Vector2i(100, 100));
		GUI.drawButton(buffers, matrices, startButton);
	}
}
