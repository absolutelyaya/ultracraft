package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.client.gui.widget.GameRuleWidget;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.joml.Vector2i;

public class ServerConfigScreen extends Screen
{
	final GameRules gameRules;
	
	public ServerConfigScreen(World world)
	{
		super(Text.translatable("screen.ultracraft.server.config-menu.title"));
		gameRules = world.getGameRules();
	}
	
	@Override
	protected void init()
	{
		super.init();
		//TODO: Query Server Gamerules to set initial values
		//TODO: Add Scrolling
		//TODO: Add all other Mod Gamerules
		//TODO: Add Widget Body Texture
		addDrawableChild(new GameRuleWidget<>(gameRules, new Vector2i(width / 2 - 100, height / 2 - 38), GameruleRegistry.DISABLE_HANDSWAP, GameRuleWidget.ValueType.BOOL, 1));
		addDrawableChild(new GameRuleWidget<>(gameRules, new Vector2i(width / 2 - 100, height / 2), GameruleRegistry.HIVEL_SPEED, GameRuleWidget.ValueType.INT, 2));
		addDrawableChild(new GameRuleWidget<>(gameRules, new Vector2i(width / 2 - 100, height / 2 + 38), GameruleRegistry.TIME_STOP, new String[] { GameruleRegistry.Option.FORCE_ON.toString(), GameruleRegistry.Option.FORCE_OFF.toString() }, 3));
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public void renderBackground(MatrixStack matrices)
	{
		super.renderBackground(matrices);
		fill(matrices, 0, 0, width, height, 0x44000000);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND_TEXTURE);
		RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f);
		drawTexture(matrices, width /2 - 125, 0, 0, 0.0f, 0.0f, 250, height, 32, 32);
		drawBorder(matrices, width / 2 - 125, -1, 250, height + 2, 0xffdddddd);
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
}
