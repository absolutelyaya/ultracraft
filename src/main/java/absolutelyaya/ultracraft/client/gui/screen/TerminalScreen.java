package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.api.TerminalCodeRegistry;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.widget.TerminalPaletteWidget;
import absolutelyaya.ultracraft.client.gui.widget.SimpleColorSelectionWidget;
import absolutelyaya.ultracraft.client.ClientGraffitiManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.List;

public class TerminalScreen extends Screen
{
	SimpleColorSelectionWidget textColorPicker, paletteColorPicker;
	TerminalBlockEntity terminal;
	TerminalPaletteWidget paletteWidget;
	TerminalBlockEntity.Tab lastTab = TerminalBlockEntity.Tab.MAIN_MENU;
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
		textColorPicker = new SimpleColorSelectionWidget(textRenderer, Text.translatable("screen.ultracraft.terminal.customize.text-clr"),
				new Vector3i(width - 160, 32, 155), () -> terminal.getTextColor(),
				i -> terminal.setTextColor((0xff << 24) + i));
		textColorPicker.setAlpha(1f);
		paletteWidget = new TerminalPaletteWidget(new Vector2i(16, height / 2 - 16 * 16 / 2), 16, terminal, i -> {
			paletteColorPicker.forceUpdate();
			lastSelected = i;
		});
		paletteWidget.setSelectedColor(lastSelected);
		editPalleteCheckbox = addDrawableChild(new CheckboxWidget(16, height - 32, 16, 20,
				Text.translatable("screen.ultracraft.terminal.graffiti.edit-palette"), false));
		paletteColorPicker = new SimpleColorSelectionWidget(textRenderer,
				Text.translatable("screen.ultracraft.terminal.graffiti.palette-clr", paletteWidget.getSelectedColor()),
				new Vector3i(64, height / 2 - 41, 155), () -> terminal.getPaletteColor(paletteWidget.getSelectedColor()),
				i -> terminal.setPaletteColor(paletteWidget.getSelectedColor() - 1, (0xff << 24) + i));
		paletteColorPicker.setAlpha(1f);
		exportName = addDrawableChild(new TextFieldWidget(textRenderer, 64, height / 2 + 67, 128, 20,
				Text.translatable("screen.ultracraft.terminal.graffiti.default_name")));
		exportName.setText(Text.translatable("screen.ultracraft.terminal.graffiti.default_name").getString());
		exportName.setPlaceholder(Text.translatable("screen.ultracraft.terminal.graffiti.name_placeholder"));
		exportName.setChangedListener(s -> exportButton.setMessage(Text.translatable("screen.ultracraft.terminal.graffiti.export", s)));
		exportButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.ultracraft.terminal.graffiti.export", exportName.getText()), b -> {
			ClientGraffitiManager.exportGraffitiPng(terminal, exportName.getText());
		}).dimensions(63, height / 2 + 67 + 22, 130, 20).build());
		if(!terminal.getTab().equals(TerminalBlockEntity.Tab.GRAFFITI))
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
			case TerminalBlockEntity.Tab.CUSTOMIZATION_ID -> {
				if(!lastTab.equals(TerminalBlockEntity.Tab.CUSTOMIZATION))
				{
					textColorPicker.setActive(true);
					editPalleteCheckbox.active = false;
					editPalleteCheckbox.visible = false;
					exportButton.active = false;
					exportButton.visible = false;
					exportName.active = false;
					exportName.visible = false;
					paletteColorPicker.setActive(false);
				}
				textColorPicker.render(context, mouseX, mouseY, delta);
			}
			case TerminalBlockEntity.Tab.GRAFFITI_ID -> renderGraffitiMenu(context, mouseX, mouseY, delta);
			default -> {
				if(!lastTab.equals(terminal.getTab()))
				{
					textColorPicker.setActive(false);
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
				int x = 64, y = height / 2 - 64;
				graffitiTexturePos = new Vector2i(x, y);
				context.getMatrices().push();
				context.getMatrices().translate(x, y, 0f);
				context.getMatrices().scale(0.5f, 0.5f, 0.5f);
				context.drawTexture(terminal.getGraffitiTexture(), 0, 0, 0, 0, 256, 256);
				context.getMatrices().pop();
				context.drawBorder(x - 1, y - 1, 34, 90, 0xffffffff);
				context.drawBorder(x + 96 - 1, y - 1, 34, 90, 0xffffffff);
				context.drawBorder(x + 32, y - 1, 64, 130, 0xffffffff);
				context.fill(x, y + 89, x + 32, y + 128, 0x88000000);
				context.fill(x + 96, y + 89, x + 32 + 96, y + 128, 0x88000000);
				context.drawBorder(x - 1, y - 1, 130, 130, 0xffffffff);
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
		if(paletteColorPicker.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
			return true;
		if(!editPalleteCheckbox.isChecked() && terminal.getTab().equals(TerminalBlockEntity.Tab.GRAFFITI) &&
				   mouseX > graffitiTexturePos.x && mouseX < graffitiTexturePos.x + 128 && mouseY > graffitiTexturePos.y && mouseY < graffitiTexturePos.y + 128)
		{
			int x = (int)Math.round((mouseX - graffitiTexturePos.x) / 128f * 32f);
			int y = (int)Math.round((mouseY - graffitiTexturePos.y) / 128f * 32f);
			terminal.setPixel(x, y, (byte)paletteWidget.getSelectedColor());
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		terminal.onHit();
		if(!exportName.isMouseOver(mouseX, mouseY))
			exportName.setFocused(false);
		if(!editPalleteCheckbox.isChecked() && terminal.getTab().equals(TerminalBlockEntity.Tab.GRAFFITI) &&
				   mouseX > graffitiTexturePos.x && mouseX < graffitiTexturePos.x + 128 && mouseY > graffitiTexturePos.y && mouseY < graffitiTexturePos.y + 128)
		{
			int x = (int)Math.round((mouseX - graffitiTexturePos.x) / 128f * 32f);
			int y = (int)Math.round((mouseY - graffitiTexturePos.y) / 128f * 32f);
			terminal.setPixel(x, y, (byte)paletteWidget.getSelectedColor());
			return true;
		}
		if (textColorPicker.mouseClicked(mouseX, mouseY, button))
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
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(terminal.getTab().equals(TerminalBlockEntity.Tab.EDIT_SCREENSAVER))
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
						String line = terminal.getLines().get(v.y);
						line = line.substring(0, v.x - 1) + (v.x < line.length() ? line.substring(v.x) : "");
						terminal.getLines().set(v.y, line);
						terminal.setCaret(v.sub(1, 0));
					}
				}
				case 261 -> { //delete
					Vector2i v = terminal.getCaret();
					String line = terminal.getLines().get(v.y);
					if(v.x < line.length())
					{
						line = line.substring(0, v.x) + (v.x < line.length() ? line.substring(v.x + 1) : "");
						terminal.getLines().set(v.y, line);
					}
				}
				case 256, 257 -> terminal.setTab(TerminalBlockEntity.Tab.CUSTOMIZATION); //ESC or Enter
			}
			return true;
		}
		if(terminal.getTab().equals(TerminalBlockEntity.Tab.GRAFFITI) && !exportName.isFocused())
		{
			switch (keyCode)
			{
				case 262, 68 -> terminal.rotateGrafittiCam(modifiers == 2 ? -15 : -7.5f); //right
				case 263, 65 -> terminal.rotateGrafittiCam(modifiers == 2 ? 15 : 7.5f); //left
				case 256 -> terminal.setTab(TerminalBlockEntity.Tab.CUSTOMIZATION); // ESC
			}
			return true;
		}
		if(textColorPicker.keyPressed(keyCode, scanCode, modifiers))
			return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers)
	{
		if(terminal.getTab().equals(TerminalBlockEntity.Tab.EDIT_SCREENSAVER))
		{
			Vector2i v = terminal.getCaret();
			List<String> text = terminal.getLines();
			String line = text.get(v.y);
			if(v.x == line.length())
				line += chr;
			else if(v.x == 0)
				line = chr + line;
			else
				line = line.substring(0, v.x) + chr + line.substring(v.x);
			if(textRenderer.getWidth(line) <= 100)
			{
				text.set(v.y, line);
				terminal.setCaret(terminal.getCaret().add(1, 0));
			}
			return true;
		}
		if(terminal.isOwner(client.player.getUuid()) && terminal.getTab().equals(TerminalBlockEntity.Tab.MAIN_MENU))
		{
			if(secretInput == null)
				secretInput = "";
			secretInput += chr;
			secretTimer = 2f;
		}
		if(textColorPicker.charTyped(chr, modifiers))
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
