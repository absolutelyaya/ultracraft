package absolutelyaya.ultracraft.client.gui.terminal;

import absolutelyaya.ultracraft.api.terminal.GlobalButtonActions;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Button;
import absolutelyaya.ultracraft.client.gui.terminal.elements.ListElement;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Tab;
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
	
	final Button centeredToggle, hideToggle, addButton, deleteButton;
	final TextBox titleBox, buttonLabelTextBox, buttonValueTextBox;
	final ListElement globalActionsList;
	
	List<Button> customizableButtons = new ArrayList<>();
	List<String> buttonActions = new ArrayList<>();
	List<Integer> buttonValues = new ArrayList<>();
	int selectedButton = -1;
	
	public EditMainMenuTab()
	{
		super(ID);
		buttons.add(new Button(Button.RETURN_LABEL,
				new Vector2i(103, 100 - textRenderer.fontHeight - 2), "customize", 0,  false));
		hideToggle = new Button("O", new Vector2i(103, 33), "toggle-hide", 0,  false);
		centeredToggle = new Button("O", new Vector2i(103, 47), "toggle-center", 0,  false);
		addButton = new Button("+", new Vector2i(103, 61), "add-button", 0,  false);
		deleteButton = new Button("-", new Vector2i(103, 75), "delete-button", 0,  false);
		titleBox = new TextBox(1, 100, true, true);
		buttonLabelTextBox = new TextBox(1, 95, true, false);
		globalActionsList = new ListElement(95, 3);
		buttonValueTextBox = new TextBox(1, 92 - textRenderer.getWidth(Text.translatable("terminal.value")),
				false, false);
	}
	
	@Override
	public void init(TerminalBlockEntity terminal)
	{
		super.init(terminal);
		List<Button> mainMenu = terminal.getMainMenuButtons();
		for (int i = 0; i < mainMenu.size(); i++)
		{
			Button b = mainMenu.get(i);
			customizableButtons.add(new Button(b.getLabel(), b.getPos(), "select", i, b.isCentered()).setHide(b.isHide()));
			buttonActions.add(b.getAction());
			buttonValues.add(b.getValue());
		}
		titleBox.getLines().set(0, terminal.getMainMenuTitle());
		globalActionsList.getEntries().addAll(GlobalButtonActions.getAllActions());
		globalActionsList.setOnSelect(i -> buttonActions.set(selectedButton, globalActionsList.getEntries().get(i)));
		buttonValueTextBox.setNumbersOnly(true);
		buttonValueTextBox.setChangeConsumer(t -> buttonValues.set(selectedButton, Integer.parseInt(t)));
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
		
		if(selectedButton >= 0)
			drawInspector(matrices, terminal, buffers);
		
		GUI.drawButton(buffers, matrices, addButton, customizableButtons.size() < maxButtons ? terminal.getTextColor() : 0xff888888);
		GUI.drawText(buffers, matrices, "Add Button", 114, -37, 0.001f);
		GUI.drawButton(buffers, matrices, deleteButton, selectedButton > 0 ? terminal.getTextColor() : 0xff888888);
		GUI.drawText(buffers, matrices, "Delete Button", 114, -23, 0.001f);
	}
	
	void drawInspector(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		Button b = customizableButtons.get(selectedButton);
		GUI.drawButton(buffers, matrices, hideToggle, b.isHide() ? "Y" : "N");
		GUI.drawText(buffers, matrices, "Hide", 114, -65, 0.001f);
		GUI.drawButton(buffers, matrices, centeredToggle, b.isCentered() ? "Y" : "N");
		GUI.drawText(buffers, matrices, "Center", 114, -51, 0.001f);
		GUI.drawText(buffers, matrices, "Button Label", -96, -91, 0.001f);
		GUI.drawTextBox(buffers, matrices, -98, 9 + textRenderer.fontHeight, buttonLabelTextBox,
				buttonLabelTextBox.equals(terminal.getFocusedTextbox()) ? getRainbow(1f / 3f) : 0xffffffff);
		if(selectedButton > 0)
		{
			GUI.drawText(buffers, matrices, "Button Action", -96, -69 + 5, 0.001f);
			GUI.drawList(buffers, matrices, -98, 13 + textRenderer.fontHeight * 3 + 5, globalActionsList);
			String s = Text.translatable("terminal.value").getString();
			GUI.drawText(buffers, matrices, s, -96, -textRenderer.fontHeight, 0.001f);
			GUI.drawTextBox(buffers, matrices, -95 + textRenderer.getWidth(s), 100 - textRenderer.fontHeight - 2, buttonValueTextBox,
					buttonValueTextBox.equals(terminal.getFocusedTextbox()) ? getRainbow(1f / 3f) : 0xffffffff);
		}
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
			case "select" -> {
				selectedButton = value;
				if(selectedButton >= 0)
				{
					Button b = customizableButtons.get(value);
					buttonLabelTextBox.setChangeConsumer(t -> {
						b.setLabel(t);
						clampButtonPos(b);
					});
					buttonLabelTextBox.getLines().set(0, b.getLabel());
					deleteButton.setValue(selectedButton);
					String selectedAction = buttonActions.get(selectedButton);
					if(globalActionsList.getEntries().contains(selectedAction))
						globalActionsList.select(globalActionsList.getEntries().indexOf(selectedAction));
					buttonValueTextBox.getLines().set(0, String.valueOf(buttonValues.get(selectedButton)));
				}
				else
					buttonLabelTextBox.setChangeConsumer(null);
			}
			case "toggle-hide" -> customizableButtons.get(selectedButton).toggleHide();
			case "toggle-center" -> {
				customizableButtons.get(selectedButton).toggleCentered();
				clampButtonPos(customizableButtons.get(selectedButton));
			}
			case "add-button" -> {
				if(customizableButtons.size() < maxButtons)
				{
					customizableButtons.add(new Button("terminal.button",
							new Vector2i(50, 50 - textRenderer.fontHeight / 2), "select", customizableButtons.size(), true));
					buttonActions.add("mainmenu");
					buttonValues.add(0);
				}
			}
			case "delete-button" -> {
				if(selectedButton > 0)
				{
					customizableButtons.remove(selectedButton);
					buttonActions.remove(selectedButton);
					buttonValues.remove(selectedButton);
					selectedButton = -1;
					for (int i = 0; i < customizableButtons.size(); i++)
						customizableButtons.get(i).setValue(i);
				}
			}
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
		boolean center = b.isCentered();
		int l = textRenderer.getWidth(Text.translatable(label).getString()) + 1;
		int h = textRenderer.fontHeight / 2 + 1;
		int x = (int)MathHelper.clamp(pos.x * 100 - 1, bounds.x + (center ? l / 2f + 2 : 1), bounds.z - ((center ? l / 2f - 1 : l) + 1) - 2);
		int y = (int)MathHelper.clamp(pos.y * 100 - 1 - (int)(textRenderer.fontHeight / 2f), bounds.y + h, bounds.w - h - 7);
		return new Vector2i(x, y);
	}
	
	void clampButtonPos(Button b)
	{
		Vector2i pos = b.getPos();
		String label = b.getLabel();
		boolean center = b.isCentered();
		int l = textRenderer.getWidth(Text.translatable(label).getString()) + 1;
		int h = textRenderer.fontHeight / 2 + 1;
		int x = MathHelper.clamp(pos.x, bounds.x + (center ? l : 1), bounds.z - ((center ? l / 2 : l) + 1) - 2);
		int y = MathHelper.clamp(pos.y, bounds.y + h, bounds.w - h - 7);
		pos.set(x, y);
	}
	
	boolean isInBounds(Vector2i pos)
	{
		return !(pos.x < 0 || pos.x >= 100 || pos.y < 19 || pos.y >= 100);
	}
	
	@Override
	public void onClose(TerminalBlockEntity terminal)
	{
		List<Button> mainMenu = terminal.getMainMenuButtons();
		mainMenu.clear();
		for (int i = 0; i < customizableButtons.size(); i++)
		{
			Button b = customizableButtons.get(i);
			mainMenu.add(new Button(b.getLabel(), b.getPos(), buttonActions.get(i), buttonValues.get(i), b.isCentered()).setHide(b.isHide()));
		}
		terminal.setMainMenuTitle(titleBox.getLines().get(0));
	}
}
