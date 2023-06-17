package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.client.UltracraftClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.*;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.joml.Matrix4f;

import java.time.Duration;
import java.util.Random;

public class WingCustomizationScreen extends Screen
{
	public static WingCustomizationScreen Instance;
	public static boolean MenuOpen;
	
	final Text[] viewNames = new Text[] { Text.of("Close-up Back View"), Text.of("Full Back View"), Text.of("Full Front View") };
	static final Vec3d[] viewTranslations = new Vec3d[] { new Vec3d(2.9, -0.4, 0.5f), new Vec3d(1, -0.3, 0f), new Vec3d(6.0, -0.3, 0f) };
	static int viewMode;
	
	Perspective oldPerspective;
	boolean wasHudHidden;
	Random rand;
	Screen parent;
	double fovScale;
	float noise, prevPitch;
	Entity fakePlayer;
	
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
		else
		{
			ClientPlayNetworkHandler handler = new ClientPlayNetworkHandler(client, client.currentScreen, new ClientConnection(NetworkSide.CLIENTBOUND),
					new ServerInfo("mainMenu", "127.0.0.1", true), client.getSession().getProfile(),
					client.getTelemetryManager().createWorldSession(false, Duration.ZERO));
			fakePlayer = new OtherClientPlayerEntity(new ClientWorld(
					handler, new ClientWorld.Properties(Difficulty.EASY, false, true), World.END,
					ClientDynamicRegistryType.createCombinedDynamicRegistries().getCombinedRegistryManager().get(RegistryKeys.DIMENSION_TYPE).entryOf(DimensionTypes.THE_END),
			0, 0, () -> null, client.worldRenderer, false, 0L), client.getSession().getProfile());
			
			//this shit don't work /\
		}
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
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> close())
								 .dimensions(width - 160, height - 40, 150, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.of("Cycle View"), (button) -> viewMode = (viewMode + 1) % 3)
								 .dimensions(width - 160, height - 65, 150, 20).build());
		noise = rand.nextFloat();
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		if(client.player != null)
		{
			client.player.setBodyYaw(client.player.getHeadYaw());
			client.player.setPitch(0f);
		}
		else
		{
			EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
			
			entityRenderDispatcher.setRenderShadows(false);
			VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
			RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(fakePlayer, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrices, immediate, 15728880));
			immediate.draw();
			entityRenderDispatcher.setRenderShadows(true);
		}
		
		renderBackground(matrices);
		drawCenteredTextWithShadow(matrices, textRenderer, title, width - 80, 20, 16777215);
		int w = textRenderer.getWidth(viewNames[viewMode]) + 4;
		fill(matrices, (width) / 2 - w / 2, height - 22, (width) / 2 + w / 2, height - 10, 0x88000000);
		drawCenteredTextWithShadow(matrices, textRenderer, viewNames[viewMode], (width) / 2, height - 20, 16777215);
		super.render(matrices, mouseX, mouseY, delta);
		
		//client.gameRenderer.loadPostProcessor(new Identifier(Ultracraft.MOD_ID, "shaders/post/blurbg.json")); //disfunctional; Depth map don't work ;-;
	}
	
	public Vec3d getCameraOffset()
	{
		Vec3d[] viewTranslations = new Vec3d[] { new Vec3d(-1.2, -0.4, 0.5f), new Vec3d(-2, -0.3, 0f), new Vec3d(-2.5, -0.3, 0f) };
		return viewTranslations[viewMode].multiply(1f, 1f, 0.275 / (165f / width));
	}
	
	public float getCameraRotation()
	{
		return viewMode == 2 ? 180 : 0;
	}
	
	@Override
	public void renderBackground(MatrixStack matrices)
	{
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
}
