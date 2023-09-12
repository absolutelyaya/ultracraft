package absolutelyaya.ultracraft.client.gui.terminal;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Button;
import absolutelyaya.ultracraft.client.gui.terminal.elements.ColorButton;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Tab;
import absolutelyaya.ultracraft.client.gui.terminal.elements.TextBox;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Vector2i;

import java.util.List;

public class DefaultTabs
{
	public static class MainMenu extends Tab
	{
		public MainMenu()
		{
			super(MAIN_MENU_ID);
		}
		
		@Override
		public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
		{
			GUI.drawTab(matrices, buffers, terminal.getMainMenuTitle(), null);
			for (Button b : terminal.getMainMenuButtons())
				GUI.drawButton(buffers, matrices, b);
		}
	}
	
	public static class Customization extends Tab
	{
		public Customization()
		{
			super(CUSTOMIZATION_ID);
			int y = 64;
			buttons.add(new Button("screen.ultracraft.terminal.customize.mainmenu", new Vector2i(48, y),
					"edit-mainmenu", 0, false, true));
			if(UltracraftClient.isCanGraffiti())
			{
				y -= textRenderer.fontHeight + 5;
				buttons.add(new Button("screen.ultracraft.terminal.customize.graffiti", new Vector2i(48, y),
						"graffiti", 0, false, true));
			}
			y -= textRenderer.fontHeight + 5;
			buttons.add(new Button("screen.ultracraft.terminal.customize.base-clr", new Vector2i(48, y),
					"edit-base", 0, false, true));
			y -= textRenderer.fontHeight + 5;
			buttons.add(new Button("screen.ultracraft.terminal.customize.screensaver", new Vector2i(48, y),
					"edit-screensaver", 0, false, true));
		}
		
		@Override
		public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
		{
			GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.customize", DEFAULT_RETURN_BUTTON);
			drawButtons(matrices, terminal, buffers);
		}
		
		@Override
		public void onClose(TerminalBlockEntity terminal)
		{
			terminal.syncCustomization();
		}
	}
	
	public static class BaseSelection extends Tab
	{
		static final int[] colors = new int[] {
				0xffd7f6fa, 0xffcfc6b8, 0xff7d7071, 0xff282235,
				0xff994f51, 0xffbc2f27, 0xfff47e1b, 0xfffcc330,
				0xffb6d53c, 0xff397b44, 0xff4ae6bf, 0xff28ccdf,
				0xff3978a8, 0xff6e3696, 0xffaf3ca7, 0xffe98cd1
		};
		static final int buttonSize = 14;
		
		public BaseSelection()
		{
			super(BASE_SELECT_ID, "customize");
			for (int y = 0; y < 4; y++)
				for (int x = 0; x < 4; x++)
					buttons.add(new ColorButton(new Vector2i(18 + y * (buttonSize + 3), 16 + x * (buttonSize + 3)),
							"set-base", (y * 4 + x), false, false, colors[y * 4 + x]));
		}
		
		@Override
		public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
		{
			GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.customize.base-clr", returnButton);
			for (Button b : buttons)
			{
				if(b instanceof ColorButton cb)
					GUI.drawColorButton(buffers, matrices, buttonSize, buttonSize, cb);
			}
			GUI.drawButton(buffers, matrices, returnButton);
		}
	}
	
	public static class ScreenSaverEditor extends Tab
	{
		Button returnButton;
		TextBox textBox;
		
		public ScreenSaverEditor()
		{
			super(EDIT_SCREENSAVER_ID);
			returnButton = new Button(Button.RETURN_LABEL, new Vector2i(103, 100 - textRenderer.fontHeight - 2),
					"customize", 0, false, false);
		}
		
		@Override
		public void init(TerminalBlockEntity terminal)
		{
			textBox = new TextBox(terminal.getScreensaver().size(), 100, true, false);
			List<String> list = terminal.getScreensaver();
			for (int i = 0; i < list.size(); i++)
				textBox.getLines().set(i, list.get(i));
		}
		
		@Override
		public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
		{
			GUI.drawBG(matrices, buffers);
			GUI.drawTextBox(buffers, matrices, 0, 0, textBox,
					textBox.equals(terminal.getFocusedTextbox()) ? getRainbow(1f / 6f) : 0xffffffff);
			GUI.drawButton(buffers, matrices, returnButton);
		}
		
		@Override
		public void onClose(TerminalBlockEntity terminal)
		{
			List<String> list = terminal.getScreensaver();
			for (int i = 0; i < list.size(); i++)
				list.set(i, textBox.getLines().get(i));
		}
	}
	
	public static class Graffiti extends Tab
	{
		public Graffiti()
		{
			super(GRAFFITI_ID, "customize");
		}
		
		@Override
		public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
		{
			GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.customize.graffiti", null);
			
			String t = Text.translatable("screen.ultracraft.terminal.focus-pls").getString();
			GUI.drawText(buffers, matrices, t, 50 - textRenderer.getWidth(t) / 2, -22 + textRenderer.fontHeight - 2, 0.005f, terminal.getTextColor());
		}
		
		@Override
		public void onClose(TerminalBlockEntity terminal)
		{
			terminal.syncGraffiti();
		}
	}
}
