package absolutelyaya.ultracraft.client.gui.terminal;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Button;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Tab;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;
import org.joml.Vector2i;

public class WeaponsTab extends Tab
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/weapon_icons.png");
	
	Button returnButton = new Button(Button.RETURN_LABEL,
			new Vector2i(-5 - textRenderer.getWidth(Text.translatable(Button.RETURN_LABEL).getString()), 96 - textRenderer.fontHeight),
			"mainmenu", 0, false, false);
	Button craftButton = new Button("terminal.craft", new Vector2i(102, 96 - textRenderer.fontHeight), "craft", 0, false, false);
	final String[] weaponCategories = new String[] { "revolver", "shotgun", "nailgun", "railcannon", "rocket_launcher" };
	
	public WeaponsTab()
	{
		super(WEAPONS_ID);
	}
	
	@Override
	public void init(TerminalBlockEntity terminal)
	{
		super.init(terminal);
		for (int i = 0; i < weaponCategories.length; i++)
		{
			String t = "terminal.weapon." + weaponCategories[i];
			buttons.add(new Button(t, new Vector2i(-5 - textRenderer.getWidth(Text.translatable("terminal.weapon." + weaponCategories[i]).getString()),
					2 + (i * (textRenderer.fontHeight + 5))), "select", i, false, false));
		}
		buttons.add(craftButton);
	}
	
	@Override
	public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "terminal.weapons", returnButton);
		GUI.drawBox(buffers, matrices, 0, 0, 1, 100, 0xffffffff, 0.0005f);
		GUI.drawBox(buffers, matrices, 99, 0, 1, 100, 0xffffffff, 0.0005f);
		drawButtons(matrices, terminal, buffers);
	}
	
	@Override
	public Vector2f getSizeOverride()
	{
		return new Vector2f(300, 100);
	}
}
