package absolutelyaya.ultracraft.client.gui.terminal.elements;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.rendering.block.entity.TerminalBlockEntityRenderer;
import absolutelyaya.ultracraft.util.TerminalGuiRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import software.bernie.geckolib.core.object.Color;

import java.util.ArrayList;
import java.util.List;

public class Tab
{
	public static final TextRenderer textRenderer;
	public static final TerminalGuiRenderer GUI;
	
	public static final String MAIN_MENU_ID = "main-menu";
	public static final String COMING_SOON_ID = "placeholder";
	public static final String WEAPONS_ID = "weapons";
	public static final String BESTIARY_ID = "enemies";
	public static final String CUSTOMIZATION_ID = "customize";
	public static final String BASE_SELECT_ID = "base-select";
	public static final String EDIT_SCREENSAVER_ID = "edit-screensaver";
	public static final String GRAFFITI_ID = "graffiti";
	public static final Button DEFAULT_RETURN_BUTTON;
	
	static final List<String> defaultTabs = new ArrayList<>() {
		{
			add(MAIN_MENU_ID);
			add(COMING_SOON_ID);
			add(WEAPONS_ID);
			add(BESTIARY_ID);
			add(CUSTOMIZATION_ID);
			add(BASE_SELECT_ID);
			add(EDIT_SCREENSAVER_ID);
			add(GRAFFITI_ID);
		}
	};
	
	protected static float time;
	protected final List<Button> buttons = new ArrayList<>();
	public final String id;
	protected final Button returnButton;
	
	public Tab(String id)
	{
		this.id = id;
		returnButton = new Button(Button.RETURN_LABEL, new Vector2i(48, 95 - textRenderer.fontHeight), "mainmenu", 0, true);
	}
	
	public Tab(String id, String returnAction)
	{
		this.id = id;
		returnButton = new Button(Button.RETURN_LABEL, new Vector2i(48, 95 - textRenderer.fontHeight), returnAction, 0, true);
	}
	
	@ApiStatus.Internal
	public static boolean isDefaultTab(String id)
	{
		return defaultTabs.contains(id);
	}
	
	public void init(TerminalBlockEntity terminal)
	{
		time = 0f;
	}
	
	public final void render(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		time += MinecraftClient.getInstance().getTickDelta() / 20f;
		drawCustomTab(matrices, terminal, buffers);
	}
	
	public void drawCustomTab(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		GUI.drawBG(matrices, buffers);
		drawButtons(matrices, terminal, buffers);
	}
	
	public void drawButtons(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		for (Button b : buttons)
		{
			if(!b.isHide() || terminal.isOwner(MinecraftClient.getInstance().player.getUuid()))
				GUI.drawButton(buffers, matrices, b);
		}
	}
	
	public Vector2f getSizeOverride()
	{
		return null;
	}
	
	public int getColorOverride()
	{
		return -1;
	}
	
	public void onClicked(Vector2d pos, boolean element, int button)
	{
	
	}
	
	public boolean onButtonClicked(String action, int value)
	{
		return false;
	}
	
	public void onScroll(double amount)
	{
		
	}
	
	public boolean drawCustomCursor(MatrixStack matrices, VertexConsumerProvider buffers, Vector2d pos)
	{
		return false;
	}
	
	protected int getRainbow(float speed)
	{
		return Color.HSBtoARGB(time * speed % 1f, 1, 1);
	}
	
	public void onClose(TerminalBlockEntity terminal)
	{
	
	}
	
	static {
		textRenderer = MinecraftClient.getInstance().textRenderer;
		GUI = TerminalBlockEntityRenderer.GUI;
		DEFAULT_RETURN_BUTTON = new Button(Button.RETURN_LABEL, new Vector2i(48, 95 - textRenderer.fontHeight), "mainmenu", 0, true);
	}
}