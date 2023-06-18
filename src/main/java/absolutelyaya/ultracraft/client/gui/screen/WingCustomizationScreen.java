package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WidgetAccessor;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.widget.ColorSelectionWidget;
import absolutelyaya.ultracraft.util.RenderingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WingCustomizationScreen extends Screen
{
	public static WingCustomizationScreen Instance;
	public static boolean MenuOpen;
	
	final Text[] viewNames = new Text[] { Text.of("Close-up Back View"), Text.of("Full Back View"), Text.of("Full Front View") };
	static final Vec3d[] viewTranslations = new Vec3d[] { new Vec3d(-1.2, -0.4, 0.5f), new Vec3d(-2, -0.3, 0f), new Vec3d(-2.5, -0.3, 0f) };
	static int viewMode;
	List<Drawable> mainWidgets = new ArrayList<>();
	
	Text subTitle = Text.empty();
	Perspective oldPerspective;
	boolean wasHudHidden;
	Random rand;
	Screen parent;
	double fovScale, mainOffsetX, offsetY;
	float noise, prevPitch, patternsAnim;
	Tab tab = Tab.MAIN;
	ButtonWidget closeButton;
	ColorSelectionWidget top;
	
	public WingCustomizationScreen(Screen parent)
	{
		super(Text.translatable("screen.ultracraft.wing-settings.title"));
		this.parent = parent;
		rand = new Random();
		noise = rand.nextFloat();
		client = MinecraftClient.getInstance();
		wasHudHidden = client.options.hudHidden;
		oldPerspective = client.options.getPerspective();
		fovScale = client.options.getFovEffectScale().getValue();
		if(client.player != null)
			prevPitch = client.player.getPitch();
		//else
		//{
			//ClientPlayNetworkHandler handler = new ClientPlayNetworkHandler(client, client.currentScreen, new ClientConnection(NetworkSide.CLIENTBOUND),
			//		new ServerInfo("mainMenu", "127.0.0.1", true), client.getSession().getProfile(),
			//		client.getTelemetryManager().createWorldSession(false, Duration.ZERO));
			//fakePlayer = new OtherClientPlayerEntity(new ClientWorld(
			//		handler, new ClientWorld.Properties(Difficulty.EASY, false, true), World.END,
			//		ClientDynamicRegistryType.createCombinedDynamicRegistries().getCombinedRegistryManager().get(RegistryKeys.DIMENSION_TYPE).entryOf(DimensionTypes.THE_END),
			//0, 0, () -> null, client.worldRenderer, false, 0L), client.getSession().getProfile());
			
			//this shit don't work /\
		//}
		client.options.hudHidden = true;
		client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
		client.options.getFovEffectScale().setValue(0.0);
		Instance = this;
		MenuOpen = true;
	}
	
	@Override
	protected void init()
	{
		super.init();
		int y = 32;
		mainWidgets.add(top = addDrawableChild(new ColorSelectionWidget(textRenderer, new Vector3i(width - 160, y, 155), false)));
		y += 103;
		if(height > 135 + 102 + 115)
		{
			mainWidgets.add(addDrawableChild(new ColorSelectionWidget(textRenderer, new Vector3i(width - 160, y, 155), true)));
			y += 107;
		}
		else
			top.setShowTypeSwitch(true);
		mainWidgets.add(addDrawableChild(new OtherButton(width - 160, y, 150, 20, Text.of("§6☆ Patterns ☆"),
				(button) -> openPatterns())));
		y += 25;
		mainWidgets.add(addDrawableChild(new OtherButton(width - 160, y, 150, 20, Text.of("Presets"),
				(button) -> openPresets())));
		y += 25;
		addDrawableChild(ButtonWidget.builder(Text.of("Cycle View"), (button) -> viewMode = (viewMode + 1) % 3)
								 .dimensions(width - 160, y, 150, 20).build());
		y += 25;
		closeButton = addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
			if(tab == Tab.MAIN)
				close();
			else
				backToMain();
		}).dimensions(width - 160, y, 150, 20).build());
		noise = rand.nextFloat();
		
		if(y + 25 > height)
			offsetY -= (height - (y + 25));
		
		children().forEach(c -> ((WidgetAccessor)c).setOffset(new Vector2i(0, (int)offsetY)));
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		animateTabChanges(matrices, delta);
		
		if(client.player != null)
		{
			client.player.setBodyYaw(client.player.getHeadYaw());
			client.player.setPitch(0f);
		}
		//else
		//{
		//	EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
		//
		//	entityRenderDispatcher.setRenderShadows(false);
		//	VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
		//	RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(fakePlayer, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrices, immediate, 15728880));
		//	immediate.draw();
		//	entityRenderDispatcher.setRenderShadows(true);
		//}
		
		renderBackground(matrices);
		drawCenteredTextWithShadow(matrices, textRenderer, title, width - 80, 20, 16777215);
		drawCenteredTextWithShadow(matrices, textRenderer, subTitle, width - 80, 30, 16777215);
		int w = textRenderer.getWidth(viewNames[viewMode]) + 4;
		fill(matrices, (width) / 2 - w / 2, height - 22, (width) / 2 + w / 2, height - 10, 0x88000000);
		drawCenteredTextWithShadow(matrices, textRenderer, viewNames[viewMode], (width) / 2, height - 20, 16777215);
		super.render(matrices, mouseX, mouseY, delta);
		//PatternTab
		if(patternsAnim > 0f)
		{
			RenderSystem.setShaderTexture(0, new Identifier(Ultracraft.MOD_ID, "textures/gui/notyet.png"));
			float scale = 2f * (1f + (1f - patternsAnim));
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f - Math.abs(1 - patternsAnim));
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
			RenderingUtil.drawTexture(matrices.peek().getPositionMatrix(),
					new Vector4f((int)(width - 80 - 32 * scale), (int)(height / 2 - 24 * scale), (int)(32 * scale), (int)(24 * scale)),
					new Vec2f(64f, 48f), new Vector4f(0f, 0f, 1f, 1f));
			drawTexturedQuad(matrices.peek().getPositionMatrix(),
					(int)(width - 80 - 32 * scale), (int)(width - 80 + 32 * scale), (int)(height / 2 - 24 * scale), (int)(height / 2 + 24 * scale), 1,
					0f, 1f, 0f, 1f);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
		
		//client.gameRenderer.loadPostProcessor(new Identifier(Ultracraft.MOD_ID, "shaders/post/blurbg.json")); //disfunctional; Depth map don't work ;-;
	}
	
	void animateTabChanges(MatrixStack matrices, float delta)
	{
		if(tab != Tab.MAIN && mainOffsetX < 200f)
			mainOffsetX += delta * 20;
		else if(tab == Tab.MAIN && mainOffsetX > 0f)
			mainOffsetX -= delta * 20;
		mainOffsetX = MathHelper.clamp(mainOffsetX, 0f, 200f);
		mainWidgets.forEach(w ->
		{
			((WidgetAccessor)w).setOffset(new Vector2i((int)mainOffsetX, 0));
			((WidgetAccessor)w).setAlpha((float) MathHelper.clamp(1 - (mainOffsetX / 150f), 0f, 1f));
		});
		
		if(tab == Tab.PATTERNS && patternsAnim < 1f && mainOffsetX > 100f)
			patternsAnim = MathHelper.clamp(patternsAnim + delta / 10, 0f, 1f);
		else if(tab != Tab.PATTERNS && patternsAnim < 2f && patternsAnim != 0f)
			patternsAnim = MathHelper.clamp(patternsAnim + delta / 5, 1f, 2f);
		else if(tab != Tab.PATTERNS && patternsAnim == 2f)
			patternsAnim = 0f;
	}
	
	public Vec3d getCameraOffset()
	{
		return viewTranslations[viewMode].multiply(1f, 1f, 0.275 / (165f / width));
	}
	
	public float getCameraRotation()
	{
		return viewMode == 2 ? 180 : 0;
	}
	
	@Override
	public void renderBackground(MatrixStack matrices)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND_TEXTURE);
		RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f);
		drawTexture(matrices, width - 165, 0, 0, 0.0f, 0.0f, 165, height, 32, 32);
		RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f);
		RenderSystem.setShader(UltracraftClient::getTexPosFadeProgram);
		RenderSystem.getShader().getUniform("Tiling").set(16f, 16f, noise);
		drawTexture(matrices, width - 165 - 32, 0, 0, 0.0f, 0.0f, 32, height, 32, 32);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	}
	
	@Override
	public void close()
	{
		client.setScreen(parent);
		client.gameRenderer.disablePostProcessor();
		client.options.hudHidden = wasHudHidden;
		client.options.setPerspective(oldPerspective);
		if(client.player != null)
			client.player.setPitch(prevPitch);
		MenuOpen = false;
		UltracraftClient.getConfigHolder().getConfig().wingColors = UltracraftClient.getWingColors();
		UltracraftClient.getConfigHolder().save();
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	public static void drawTexture(MatrixStack matrices, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		drawTexture(matrices, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
	}
	
	private static void drawTexture(MatrixStack matrices, int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
		drawTexturedQuad(matrices.peek().getPositionMatrix(), x0, x1, y0, y1, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
	}
	
	private static void drawTexturedQuad(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1)
	{
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(matrix, (float)x0, (float)y0, (float)z).texture(u0, v0).next();
		bufferBuilder.vertex(matrix, (float)x0, (float)y1, (float)z).texture(u0, v1).next();
		bufferBuilder.vertex(matrix, (float)x1, (float)y1, (float)z).texture(u1, v1).next();
		bufferBuilder.vertex(matrix, (float)x1, (float)y0, (float)z).texture(u1, v0).next();
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}
	
	void openPresets()
	{
		tab = Tab.PRESETS;
		subTitle = Text.of("Presets");
		setMainTabActive(false);
		closeButton.setMessage(Text.of("Back"));
	}
	
	void openPatterns()
	{
		tab = Tab.PATTERNS;
		subTitle = Text.of("§6☆ Patterns ☆");
		setMainTabActive(false);
		closeButton.setMessage(Text.of("Back"));
	}
	
	void backToMain()
	{
		tab = Tab.MAIN;
		subTitle = Text.empty();
		setMainTabActive(true);
		closeButton.setMessage(ScreenTexts.DONE);
	}
	
	void setMainTabActive(boolean b)
	{
		mainWidgets.forEach(w -> ((WidgetAccessor)w).setActive(b));
	}
	
	static class OtherButton extends ButtonWidget
	{
		protected OtherButton(int x, int y, int width, int height, Text message, PressAction onPress)
		{
			super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
		}
		
		@Override
		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta)
		{
			MinecraftClient client = MinecraftClient.getInstance();
			RenderSystem.setShaderTexture(0, new Identifier(Ultracraft.MOD_ID, "textures/gui/widgets.png"));
			RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			int i = 1;
			if (!active)
				i = 0;
			else if (isSelected())
				i = 2;
			drawNineSlicedTexture(matrices, getX(), getY(), getWidth(), getHeight(), 20, 4, 200, 20, 0, i * 20);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			i = active ? 16777215 : 10526880;
			drawMessage(matrices, client.textRenderer, i | MathHelper.ceil(alpha * 255f) << 24);
		}
	}
	
	enum Tab
	{
		MAIN,
		PRESETS,
		PATTERNS
	}
}
