package absolutelyaya.ultracraft.client.gui.terminal;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Button;
import absolutelyaya.ultracraft.api.terminal.Tab;
import absolutelyaya.ultracraft.client.gui.terminal.elements.TextBox;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4i;

import java.util.ArrayList;
import java.util.List;

public class EditMainMenuTab extends Tab
{
	public static final String ID = "edit-mainmenu";
	static final Vector4i bounds = new Vector4i(0, textRenderer.fontHeight + 5, 100, 100);
	static final int maxButtons = 5;
	
	final Button centeredToggle, hideToggle;
	final TextBox titleBox;
	
	List<Button> customizableButtons = new ArrayList<>();
	int selectedButton = -1;
	
	public EditMainMenuTab()
	{
		super(ID);
		buttons.add(new Button(Button.RETURN_LABEL,
				new Vector2i(103, 100 - textRenderer.fontHeight - 2), "customize", 0, false, false));
		hideToggle = new Button("O", new Vector2i(103, 61), "toggle-hide", 0, false, false);
		centeredToggle = new Button("O", new Vector2i(103, 75), "toggle-center", 0, false, false);
		titleBox = new TextBox(1, 100, true, true);
	}
	
	@Override
	public void init(TerminalBlockEntity terminal)
	{
		super.init(terminal);
		List<Button> mainMenu = terminal.getMainMenuButtons();
		for (int i = 0; i < mainMenu.size(); i++)
		{
			Button b = mainMenu.get(i);
			customizableButtons.add(new Button(b.getLabel(), b.getPos(), "select", i, b.isHide(), b.isCentered()));
		}
		titleBox.getLines().set(0, terminal.getMainMenuTitle());
	}
	
	@Override
	public Vector2f getSizeOverride()
	{
		return new Vector2f(300, 104);
	}
	
	@Override
	public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		super.drawCustomTab(matrices, terminal, buffers);
		GUI.drawBoxOutline(buffers, matrices, bounds.x, bounds.y + 4, bounds.z - bounds.x, bounds.w - bounds.y - 4, 0xffffffff);
		String t = terminal.getMainMenuTitle();
		
		GUI.drawTextBox(buffers, matrices, 0, 4, titleBox,
				titleBox.equals(terminal.getFocusedTextbox()) ? getRainbow(1f / 3f) : 0xffffffff);
		
		for (int i = 0; i < customizableButtons.size(); i++)
		{
			Button b = customizableButtons.get(i);
			GUI.drawButton(buffers, matrices, b, b.getLabel());
			if(i == selectedButton)
			{
				int l = textRenderer.getWidth(Text.translatable(b.getLabel()));
				matrices.push();
				matrices.translate(0f, 0f, -0.001f);
				GUI.drawBoxOutline(buffers, matrices, b.getPos().x - (b.isCentered() ? l / 2 + 1 : 0), b.getPos().y,
						l + 3, textRenderer.fontHeight + 2, getRainbow(1f / 3f));
				matrices.pop();
			}
		}
		
		if(selectedButton > 0)
			drawInspector(matrices, buffers);
	}
	
	void drawInspector(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		Button b = customizableButtons.get(selectedButton);
		GUI.drawButton(buffers, matrices, hideToggle, b.isHide() ? "O" : "X");
		GUI.drawText(buffers, matrices, "Hide", 114, -37, 0.001f);
		GUI.drawButton(buffers, matrices, centeredToggle, b.isCentered() ? "O" : "X");
		GUI.drawText(buffers, matrices, "Center", 114, -23, 0.001f);
		//TODO: Button Label Textbox
		//TODO: Global Button Action list + Scroll input
		//TODO: Redstone output button
	}
	
	@Override
	public boolean drawCustomCursor(MatrixStack matrices, VertexConsumerProvider buffers, Vector2d relativePos)
	{
		if(selectedButton == -1)
			return false;
		Button b = customizableButtons.get(selectedButton);
		Vector2i pos = calculateButtonPos(relativePos, b);
		if(!isInBounds(new Vector2i((int)(relativePos.x * 100), (int)(relativePos.y * 100))))
			return false;
		int w = textRenderer.getWidth(Text.translatable(b.getLabel()).getString());
		int h = textRenderer.fontHeight;
		matrices.push();
		matrices.translate(0f, 0f, -0.001f);
		GUI.drawBoxOutline(buffers, matrices, pos.x - (b.isCentered() ? w / 2 + 1 : 0), pos.y, w + 3, h + 2, 0x88ffffff);
		matrices.pop();
		return true;
	}
	
	@Override
	public boolean onButtonClicked(String action, int value)
	{
		switch(action)
		{
			case "select" -> selectedButton = value;
			case "toggle-hide" -> customizableButtons.get(selectedButton).toggleHide();
			case "toggle-center" -> customizableButtons.get(selectedButton).toggleCentered();
			default -> {
				return super.onButtonClicked(action, value);
			}
		}
		return true;
	}
	
	@Override
	public void onClicked(Vector2d pos, boolean element, int button)
	{
		if(button == 1)
			selectedButton = -1;
		if(!element && selectedButton != -1)
		{
			Button b = customizableButtons.get(selectedButton);
			Vector2i clampedPos = calculateButtonPos(pos, b);
			if(isInBounds(new Vector2i((int)(pos.x * 100), (int)(pos.y * 100))))
				b.getPos().set(clampedPos.x, clampedPos.y);
		}
	}
	
	Vector2i calculateButtonPos(Vector2d pos, Button b)
	{
		String label = b.getLabel();
		int l = textRenderer.getWidth(Text.translatable(label).getString()) / 2 + 1;
		int h = textRenderer.fontHeight / 2 + 1;
		int x = (int)MathHelper.clamp(pos.x * 100 - 1, bounds.x + l, bounds.z - l - 2);
		int y = (int)MathHelper.clamp(pos.y * 100 - 1 - (int)(textRenderer.fontHeight / 2f), bounds.y + h, bounds.w - h - 7);
		return new Vector2i(x, y);
	}
	
	boolean isInBounds(Vector2i pos)
	{
		return !(pos.x < 0 || pos.x >= 100 || pos.y < 19 || pos.y >= 100);
	}
	
	@Override
	public void onClose(TerminalBlockEntity terminal)
	{
		List<Button> mainMenu =  terminal.getMainMenuButtons();
		for (int i = 0; i < mainMenu.size(); i++)
		{
			Button original = mainMenu.get(i);
			Button b = customizableButtons.get(i);
			mainMenu.set(i, new Button(b.getLabel(), b.getPos(), original.getAction(), original.getValue(), b.isHide(), b.isCentered()));
		}
		terminal.setMainMenuTitle(titleBox.getLines().get(0));
	}
}
