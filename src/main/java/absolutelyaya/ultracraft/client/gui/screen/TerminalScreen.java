package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.api.terminal.TerminalCodeRegistry;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.ClientGraffitiManager;
import absolutelyaya.ultracraft.client.gui.terminal.DefaultTabs;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Tab;
import absolutelyaya.ultracraft.client.gui.terminal.elements.TextBox;
import absolutelyaya.ultracraft.client.gui.widget.SimpleColorSelectionWidget;
import absolutelyaya.ultracraft.client.gui.widget.TerminalPaletteWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.List;

public class TerminalScreen extends Screen
{
	public static final Identifier CHECKERS = new Identifier(Ultracraft.MOD_ID, "textures/gui/graffiti_checkers.png");
	
	SimpleColorSelectionWidget textColorPicker, baseColorPicker, paletteColorPicker;
	TerminalBlockEntity terminal;
	TerminalPaletteWidget paletteWidget;
	Tab lastTab = new DefaultTabs.MainMenu();
	CheckboxWidget editPalleteCheckbox;
	Vector2i graffitiTexturePos = new Vector2i();
	TextFieldWidget exportName;
	ButtonWidget exportButton;
	int lastSelected;
	String secretInput;
	float secretTimer;
	
	public TerminalScreen(TerminalBlockEntity terminal)
	{
		super(Text.of("Terminal"));
		this.terminal = terminal;
	}
	
	@Override
	protected void init()
	{
		textColorPicker = new SimpleColorSelectionWidget(textRenderer, Text.translatable("terminal.customize.text-clr"),
				new Vector3i(width - 160, 32, 155), () -> terminal.getTextColor(),
				i -> terminal.setTextColor((0xff << 24) + i));
		textColorPicker.setAlpha(1f);
		baseColorPicker = new SimpleColorSelectionWidget(textRenderer, Text.translatable("terminal.customize.base-clr"),
				new Vector3i(width - 160, 124, 155), () -> terminal.getBaseColor(),
				i -> terminal.setBaseColor(i));
		baseColorPicker.setAlpha(1f);
		paletteWidget = new TerminalPaletteWidget(new Vector2i(16, height / 2 - 16 * 16 / 2), 16, terminal, i -> {
			paletteColorPicker.forceUpdate();
			lastSelected = i;
		});
		paletteWidget.setSelectedColor(lastSelected);
		int scale = client.options.getGuiScale().getValue();
		editPalleteCheckbox = addDrawableChild(new CheckboxWidget(16 + (scale == 0 || scale == 4 ? 24 : 0), height - 32, 16, 20,
				Text.translatable("screen.ultracraft.terminal.graffiti.edit-palette"), false));
		paletteColorPicker = new SimpleColorSelectionWidget(textRenderer,
				Text.translatable("screen.ultracraft.terminal.graffiti.palette-clr", paletteWidget.getSelectedColor()),
				new Vector3i(64, height / 2 - 41, 155), () -> terminal.getPaletteColor(paletteWidget.getSelectedColor()),
				i -> terminal.setPaletteColor(paletteWidget.getSelectedColor() - 1, (0xff << 24) + i));
		paletteColorPicker.setAlpha(1f);
		int yOffset = (scale == 0 || scale == 4 ? 32 : 0);
		exportName = addDrawableChild(new TextFieldWidget(textRenderer, 64, height / 2 + 67 - yOffset, 128, 20,
				Text.translatable("screen.ultracraft.terminal.graffiti.default_name")));
		exportName.setText(Text.translatable("screen.ultracraft.terminal.graffiti.default_name").getString());
		exportName.setPlaceholder(Text.translatable("screen.ultracraft.terminal.graffiti.name_placeholder"));
		exportName.setChangedListener(s -> exportButton.setMessage(Text.translatable("screen.ultracraft.terminal.graffiti.export", s)));
		exportButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.ultracraft.terminal.graffiti.export", exportName.getText()),
				b -> ClientGraffitiManager.exportGraffitiPng(terminal, exportName.getText()))
												.dimensions(63, height / 2 + 67 + 22 - yOffset, 130, 20).build());
		if(!terminal.getTab().id.equals(Tab.GRAFFITI_ID))
		{
			editPalleteCheckbox.active = false;
			editPalleteCheckbox.visible = false;
			exportButton.active = false;
			exportButton.visible = false;
			exportName.active = false;
			exportName.visible = false;
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		switch (terminal.getTab().id)
		{
			case Tab.CUSTOMIZATION_ID -> {
				if(!lastTab.id.equals(Tab.CUSTOMIZATION_ID))
				{
					textColorPicker.setActive(true);
					baseColorPicker.setActive(true);
					editPalleteCheckbox.active = false;
					editPalleteCheckbox.visible = false;
					exportButton.active = false;
					exportButton.visible = false;
					exportName.active = false;
					exportName.visible = false;
					paletteColorPicker.setActive(false);
				}
				textColorPicker.render(context, mouseX, mouseY, delta);
				baseColorPicker.render(context, mouseX, mouseY, delta);
			}
			case Tab.GRAFFITI_ID -> {
				renderGraffitiMenu(context, mouseX, mouseY, delta);
				if(!lastTab.id.equals(Tab.GRAFFITI_ID))
					terminal.setLastHovered(null);
			}
			default -> {
				if(!lastTab.equals(terminal.getTab()))
				{
					textColorPicker.setActive(false);
					baseColorPicker.setActive(false);
					paletteWidget.setActive(false);
					editPalleteCheckbox.active = false;
					editPalleteCheckbox.visible = false;
					exportButton.active = false;
					exportButton.visible = false;
					exportName.active = false;
					exportName.visible = false;
					paletteColorPicker.setActive(false);
				}
			}
		}
		lastTab = terminal.getTab();
		if(secretTimer > 0)
			secretTimer -= delta / 20f;
		else if(secretInput != null)
		{
			TerminalCodeRegistry.trigger(secretInput, terminal);
			secretInput = null;
		}
	}
	
	void renderGraffitiMenu(DrawContext context, int mouseX, int mouseY, float delta)
	{
		editPalleteCheckbox.active = true;
		editPalleteCheckbox.visible = true;
		paletteWidget.setActive(true);
		boolean showColorPicker = editPalleteCheckbox.isChecked() && paletteWidget.getSelectedColor() != 0;
		paletteColorPicker.setActive(showColorPicker);
		if(showColorPicker)
		{
			paletteColorPicker.setTitle(Text.translatable("screen.ultracraft.terminal.graffiti.palette-clr", paletteWidget.getSelectedColor()));
			paletteColorPicker.render(context, mouseX, mouseY, delta);
			exportButton.active = false;
			exportButton.visible = false;
			exportName.active = false;
			exportName.visible = false;
		}
		else
		{
			exportButton.active = true;
			exportButton.visible = true;
			exportName.active = true;
			exportName.visible = true;
			if(terminal.getGraffitiTexture() != null)
			{
				int scale = client.options.getGuiScale().getValue();
				int x = 48, y = height / 2 - (scale == 0 || scale == 4 ? 128 : 96);
				graffitiTexturePos = new Vector2i(x, y);
				context.getMatrices().push();
				context.getMatrices().translate(x, y, 0f);
				context.getMatrices().scale(4f, 4f, 4f);
				context.drawTexture(CHECKERS, 0, 0, 0, 0, 40, 40, 40, 40);
				context.drawTexture(terminal.getGraffitiTexture(), 0, 0, 0, 0, 40, 40, 40, 40);
				context.getMatrices().pop();
				context.drawBorder(x - 1, y - 1, 34, 162, 0xffffffff); //left
				context.drawBorder(x + 96 - 1, y - 1, 34, 162, 0xffffffff); //right
				context.drawBorder(x + 32, y - 1, 64, 162, 0xffffffff); //back
				context.fill(x + 96 + 33, y, x + 160, y + 160, 0x88000000);
				context.drawBorder(x - 1, y - 1, 162, 162, 0xffffffff);
				if(mouseX >= graffitiTexturePos.x && mouseX < graffitiTexturePos.x + 160 && mouseY >= graffitiTexturePos.y && mouseY < graffitiTexturePos.y + 160)
				{
					int mx = Math.round((mouseX - 2) / 4f) * 4;
					int my = Math.round((mouseY - 2) / 4f) * 4;
					int c = paletteWidget.getSelectedColor();
					if(c != 0)
						context.fill(mx, my, mx + 4, my + 4, terminal.getPaletteColor(c));
					else
						context.drawBorder(mx - 1, my - 1, 6, 6, 0xffffffff);
				}
			}
			else
				ClientGraffitiManager.refreshGraffitiTexture(terminal);
		}
		paletteWidget.render(context, mouseX, mouseY, delta);
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY)
	{
		float f = (100f / width) / 0.23419207f * 0.75f;
		Vector2d v2 = new Vector2d((mouseX - width / 2f) * f + 50f, (mouseY - height / 2f) * f + 50f);
		terminal.setCursor(v2.div(100f));
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	{
		if (textColorPicker.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
			return true;
		if(baseColorPicker.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
			return true;
		if(paletteColorPicker.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
			return true;
		if(!editPalleteCheckbox.isChecked() && terminal.getTab().id.equals(Tab.GRAFFITI_ID) &&
				   mouseX >= graffitiTexturePos.x && mouseX < graffitiTexturePos.x + 160 && mouseY >= graffitiTexturePos.y && mouseY < graffitiTexturePos.y + 160)
		{
			int x = (int)Math.round((mouseX - graffitiTexturePos.x - 2) / 160f * 40f);
			int y = (int)Math.round((mouseY - graffitiTexturePos.y - 2) / 160f * 40f);
			terminal.setPixel(x, y, (byte)paletteWidget.getSelectedColor());
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		terminal.onHit(button);
		if(!exportName.isMouseOver(mouseX, mouseY))
			exportName.setFocused(false);
		if(!editPalleteCheckbox.isChecked() && terminal.getTab().id.equals(Tab.GRAFFITI_ID) &&
				   mouseX >= graffitiTexturePos.x && mouseX < graffitiTexturePos.x + 160 && mouseY >= graffitiTexturePos.y && mouseY < graffitiTexturePos.y + 160)
		{
			int x = (int)Math.round((mouseX - graffitiTexturePos.x - 2) / 160f * 40f);
			int y = (int)Math.round((mouseY - graffitiTexturePos.y - 2) / 160f * 40f);
			terminal.setPixel(x, y, (byte)paletteWidget.getSelectedColor());
			return true;
		}
		if (textColorPicker.mouseClicked(mouseX, mouseY, button))
			return true;
		if (baseColorPicker.mouseClicked(mouseX, mouseY, button))
			return true;
		if (paletteColorPicker.mouseClicked(mouseX, mouseY, button))
			return true;
		if (paletteWidget.mouseClicked(mouseX, mouseY, button))
			return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		if (textColorPicker.mouseReleased(mouseX, mouseY, button))
			return true;
		if (baseColorPicker.mouseReleased(mouseX, mouseY, button))
			return true;
		if (paletteColorPicker.mouseReleased(mouseX, mouseY, button))
			return true;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		terminal.scroll(amount);
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		TextBox box = terminal.getFocusedTextbox();
		if(box != null)
		{
			switch(keyCode)
			{
				case 265 -> terminal.setCaret(terminal.getCaret().sub(0, 1)); //up
				case 264 -> terminal.setCaret(terminal.getCaret().add(0, 1)); //down
				case 262 -> terminal.setCaret(terminal.getCaret().add(1, 0)); //right
				case 263 -> terminal.setCaret(terminal.getCaret().sub(1, 0)); //left
				case 259 -> { //backspace
					Vector2i v = terminal.getCaret();
					if(v.x > 0)
					{
						String line = box.getLines().get(v.y);
						line = line.substring(0, v.x - 1) + (v.x < line.length() ? line.substring(v.x) : "");
						box.getLines().set(v.y, line);
						box.onChange(v.y);
						terminal.setCaret(v.sub(1, 0));
					}
				}
				case 261 -> { //delete
					Vector2i v = terminal.getCaret();
					String line = box.getLines().get(v.y);
					if(v.x < line.length())
					{
						line = line.substring(0, v.x) + (v.x < line.length() ? line.substring(v.x + 1) : "");
						box.getLines().set(v.y, line);
						box.onChange(v.y);
					}
				}
				case 256, 257 -> terminal.setFocusedTextbox(null); //ESC or Enter
			}
			return true;
		}
		if(terminal.getTab().id.equals(Tab.GRAFFITI_ID) && !exportName.isFocused())
		{
			switch (keyCode)
			{
				case 262, 68 -> terminal.rotateGrafittiCam(modifiers == 2 ? -15 : -7.5f); //right
				case 263, 65 -> terminal.rotateGrafittiCam(modifiers == 2 ? 15 : 7.5f); //left
				case 256 -> terminal.setTab(new DefaultTabs.Customization()); // ESC
			}
			return true;
		}
		if(textColorPicker.keyPressed(keyCode, scanCode, modifiers))
			return true;
		if(baseColorPicker.keyPressed(keyCode, scanCode, modifiers))
			return true;
		if(paletteColorPicker.keyPressed(keyCode, scanCode, modifiers))
			return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers)
	{
		TextBox box = terminal.getFocusedTextbox();
		if(box != null && (!box.isNumbersOnly() || Character.isDigit(chr)))
		{
			Vector2i v = terminal.getCaret();
			List<String> text = box.getLines();
			String line = text.get(v.y);
			if(v.x == line.length())
				line += chr;
			else if(v.x == 0)
				line = chr + line;
			else
				line = line.substring(0, v.x) + chr + line.substring(v.x);
			if(textRenderer.getWidth(line) < box.getMaxLength())
			{
				text.set(v.y, line);
				box.onChange(v.y);
				terminal.setCaret(terminal.getCaret().add(1, 0));
			}
			return true;
		}
		if(terminal.isOwner(client.player.getUuid()) && terminal.getTab().id.equals(Tab.MAIN_MENU_ID))
		{
			if(secretInput == null)
				secretInput = "";
			secretInput += chr;
			secretTimer = 2f;
		}
		if(textColorPicker.charTyped(chr, modifiers))
			return true;
		if(baseColorPicker.charTyped(chr, modifiers))
			return true;
		if(paletteColorPicker.charTyped(chr, modifiers))
			return true;
		return super.charTyped(chr, modifiers);
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	@Override
	public void close()
	{
		super.close();
		if(MinecraftClient.getInstance().player instanceof WingedPlayerEntity winged)
			winged.setFocusedTerminal(null);
	}
}
