package absolutelyaya.ultracraft.client.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class InfoPopupScreen extends Screen
{
	Screen parent;
	Text text;
	
	protected InfoPopupScreen(Text title, Text text, Screen parent)
	{
		super(title);
		this.parent = parent;
		this.text = text;
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		context.fill(width / 2 - 150, height / 2 - 75, width / 2 + 150, height / 2 + 75, 0xff000000);
		context.drawBorder(width / 2 - 150, height / 2 - 75, 300, 150, 0xffffffff);
		super.render(context, mouseX, mouseY, delta);
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.scale(2, 2, 2);
		context.drawCenteredTextWithShadow(textRenderer, Text.of(title.getString()),
				(int)(width / 4f), (int)(height / 4f) - 35, 0xffffffff);
		matrices.pop();
		String[] lines = text.getString().split("\n");
		for (int i = 0; i < lines.length; i++)
		{
			context.drawCenteredTextWithShadow(textRenderer, Text.of(lines[i]),
					(int)(width / 2f), (int)(height / 2f) - 45 + 10 * i, 0xffffffff);
		}
	}
	
	@Override
	public void close()
	{
		client.setScreen(parent);
	}
}
