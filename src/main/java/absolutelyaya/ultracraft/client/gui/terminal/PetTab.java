package absolutelyaya.ultracraft.client.gui.terminal;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Button;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Sprite;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Tab;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;
import org.joml.Vector2i;

public class PetTab extends Tab
{
	public static final Identifier SPRITE_SHEET = new Identifier(Ultracraft.MOD_ID, "textures/gui/terminal/saiai.png");
	public static final String ID = "PET-GAME";
	boolean mainMenu = true;
	Button startGameButton;
	Sprite petSprite;
	float hunger, energy, hygiene;
	
	public PetTab()
	{
		super(ID);
		startGameButton = new Button("Start Game", new Vector2i(50, 30), "start", 0, true);
		petSprite = new Sprite(SPRITE_SHEET, new Vector2i(50, 50), 0.01f,
				new Vector2i(0, 0), new Vector2i(16, 19), new Vector2i(100, 100)).setCentered(true);
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
			drawPetScreen(matrices, buffers);
	}
	
	void drawMainMenu(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "SAIAI", "☆", DEFAULT_RETURN_BUTTON);
		GUI.drawButton(buffers, matrices, startGameButton);
	}
	
	void drawPetScreen(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawBG(matrices, buffers);
		GUI.drawSprite(buffers, matrices, petSprite);
		drawStatMeters(matrices, buffers);
	}
	
	void drawStatMeters(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawSprite(buffers, matrices, SPRITE_SHEET, new Vector2i(1, 79), 0.0001f,
				new Vector2i(0, 86), new Vector2i(26, 4), new Vector2i(100, 100));
		GUI.drawBox(buffers, matrices, 1, 84, 98, 1, 0xffffffff);
		GUI.drawBox(buffers, matrices, 98, 81, 1, 3, 0xffffffff);
		GUI.drawBox(buffers, matrices, 28, 81, 1, 3, 0xffffffff);
		GUI.drawBox(buffers, matrices, 28, 80, 71, 1, 0xffffffff);
		GUI.drawBox(buffers, matrices, 29, 81, 69 * hunger, 3, 0xffdd0000);
		GUI.drawSprite(buffers, matrices, SPRITE_SHEET, new Vector2i(1, 86), 0.0001f,
				new Vector2i(0, 91), new Vector2i(26, 4), new Vector2i(100, 100));
		GUI.drawBox(buffers, matrices, 1, 91, 98, 1, 0xffffffff);
		GUI.drawBox(buffers, matrices, 98, 88, 1, 3, 0xffffffff);
		GUI.drawBox(buffers, matrices, 28, 88, 1, 3, 0xffffffff);
		GUI.drawBox(buffers, matrices, 28, 87, 71, 1, 0xffffffff);
		GUI.drawBox(buffers, matrices, 29, 88, 69 * energy, 3, 0xffdd0000);
		GUI.drawSprite(buffers, matrices, SPRITE_SHEET, new Vector2i(1, 93), 0.0001f,
				new Vector2i(0, 96), new Vector2i(26, 4), new Vector2i(100, 100));
		GUI.drawBox(buffers, matrices, 1, 98, 98, 1, 0xffffffff);
		GUI.drawBox(buffers, matrices, 98, 95, 1, 3, 0xffffffff);
		GUI.drawBox(buffers, matrices, 28, 95, 1, 3, 0xffffffff);
		GUI.drawBox(buffers, matrices, 28, 94, 71, 1, 0xffffffff);
		GUI.drawBox(buffers, matrices, 29, 95, 69 * hygiene, 3, 0xffdd0000);
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
