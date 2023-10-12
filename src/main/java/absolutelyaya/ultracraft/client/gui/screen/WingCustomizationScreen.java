package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WidgetAccessor;
import absolutelyaya.ultracraft.client.Ultraconfig;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.widget.WingColorSelectionWidget;
import absolutelyaya.ultracraft.components.player.IWingDataComponent;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.registry.WingColorPresetManager;
import absolutelyaya.ultracraft.registry.WingPatterns;
import absolutelyaya.ultracraft.util.RenderingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WingCustomizationScreen extends Screen
{
	public static WingCustomizationScreen Instance;
	public static boolean MenuOpen;
	
	final Text scrollHint = Text.translatable("screen.ultracraft.wing-settings.presets.scroll-hint");
	static final Vec3d[] viewTranslations = new Vec3d[] { new Vec3d(-1.2, -0.4, 0.5f), new Vec3d(-2, -0.3, 0f), new Vec3d(-2.5, -0.3, 0f) };
	static int viewMode;
	List<Drawable> mainWidgets = new ArrayList<>();
	List<PreviewButton> previewButtons = new ArrayList<>();
	ButtonWidget refreshPresetsButton, supportButton, refreshSupportButton, patternTabButton;
	
	Text subTitle = Text.empty();
	Perspective oldPerspective;
	boolean wasHudHidden, safeVFX;
	Random rand;
	Screen parent;
	double fovScale, mainOffsetX, offsetY;
	float noise, prevPitch, patternsAnim, presetsAnim, curpreviewButtonscroll, targetScroll, showScrollingHint;
	Tab tab = Tab.MAIN;
	ButtonWidget closeButton;
	WingColorSelectionWidget top, bottom;
	Vector3f startWingColor, startMetalColor;
	
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
		safeVFX = UltracraftClient.getConfig().safeVFX;
		WingColorPresetManager.restoreDefaults();
		startWingColor = UltracraftClient.getWingColors()[0];
		startMetalColor = UltracraftClient.getWingColors()[1];
	}
	
	@Override
	protected void init()
	{
		super.init();
		int y = 32;
		mainWidgets.add(top = addDrawableChild(new WingColorSelectionWidget(textRenderer, new Vector3i(width - 160, y, 155), false, this::getStartColor)));
		y += 103;
		if(height > 135 + 102 + 115)
		{
			mainWidgets.add(bottom = addDrawableChild(new WingColorSelectionWidget(textRenderer, new Vector3i(width - 160, y, 155), true, this::getStartColor)));
			y += 107;
		}
		else
		{
			top.setShowTypeSwitch(true);
			bottom = null;
			y += 4 + (height - (y + 105));
		}
		mainWidgets.add(patternTabButton = addDrawableChild(new OtherButton(width - 160, y, 150, 20, Text.translatable("screen.ultracraft.wing-settings.patterns.title"),
				(button) -> openPatterns())));
		if(safeVFX)
		{
			patternTabButton.setTooltip(Tooltip.of(Text.translatable("screen.ultracraft.wing-settings.patterns.safe-vfx")));
			patternTabButton.active = false;
		}
		y += 25;
		mainWidgets.add(addDrawableChild(new OtherButton(width - 160, y, 150, 20, Text.translatable("screen.ultracraft.wing-settings.presets.title"),
				(button) -> openPresets())));
		refreshPresetsButton = ButtonWidget.builder(Text.translatable("screen.ultracraft.wing-settings.presets.refresh"), this::refreshPresets)
								 .dimensions(width - 160, y, 150, 20).build();
		refreshPresetsButton.active = false;
		refreshPresetsButton.setAlpha(0f);
		supportButton = ButtonWidget.builder(Text.translatable("screen.ultracraft.wing-settings.patterns.support"),
						(b) -> client.setScreen(new SupporterPopupScreen(this)))
									   .dimensions(width - 160, y, 130, 20).build();
		supportButton.active = false;
		supportButton.setAlpha(0f);
		refreshSupportButton = new RefreshButton(width - 30, y, 20, 20, Text.translatable(""), (b) -> UltracraftClient.refreshSupporter());
		refreshSupportButton.active = false;
		refreshSupportButton.setAlpha(0f);
		refreshSupportButton.setTooltip(Tooltip.of(Text.translatable("screen.ultracraft.wing-settings.patterns.refresh-supporter")));
		y += 25;
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.ultracraft.wing-settings.cycle-pov"), (button) -> viewMode = (viewMode + 1) % 3)
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
		
		if(tab == Tab.PRESETS)
			openPresets();
		if(tab == Tab.PATTERNS)
			openPatterns();
		
		children().forEach(c -> ((WidgetAccessor)c).setOffset(new Vector2i(0, (int)offsetY)));
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		animateTabChanges(delta);
		
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
		
		if(tab == Tab.PRESETS || tab == Tab.PATTERNS)
		{
			previewButtons.forEach(pb -> {
				WidgetAccessor widget = ((WidgetAccessor)pb);
				curpreviewButtonscroll = MathHelper.lerp(delta / 10, curpreviewButtonscroll, targetScroll);
				widget.setOffset(new Vector2i(0, Math.round(WingCustomizationScreen.this.curpreviewButtonscroll)));
				int y = pb.getY();
				pb.setAlphaCap(Math.min((y - 22) / 20f, 1f) - MathHelper.clamp(Math.max(y - height + 105, 0) / 20f, 0f, 1f));
			});
		}
		
		renderBackground(context);
		context.drawCenteredTextWithShadow(textRenderer, title, width - 80, 20, 16777215);
		context.drawCenteredTextWithShadow(textRenderer, subTitle, width - 80, 30, 16777215);
		Text viewName = Text.translatable("screen.ultracraft.wing-settings.pov" + (viewMode + 1));
		int w = textRenderer.getWidth(viewName) + 4;
		context.fill((width) / 2 - w / 2, height - 22, (width) / 2 + w / 2, height - 10, 0x88000000);
		context.drawCenteredTextWithShadow(textRenderer, viewName, (width) / 2, height - 20, 16777215);
		super.render(context, mouseX, mouseY, delta);
		MatrixStack matrices = context.getMatrices();
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
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
		matrices.push();
		matrices.translate(0, 0, 5);
		refreshPresetsButton.render(context, mouseX, mouseY, delta);
		supportButton.render(context, mouseX, mouseY, delta);
		refreshSupportButton.render(context, mouseX, mouseY, delta);
		if(showScrollingHint > 0f)
		{
			RenderSystem.setShaderColor(1f, 1f, 1f, Math.min(showScrollingHint, 1f));
			w = textRenderer.getWidth(scrollHint) + 4;
			context.fill((width - 80) - w / 2, height - 92, (width - 80) + w / 2, height - 80, 0x88000000);
			context.drawCenteredTextWithShadow(textRenderer, scrollHint, (width - 80), height - 90, 16777215);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			showScrollingHint += delta / 20;
		}
		matrices.pop();
		
		//client.gameRenderer.loadPostProcessor(new Identifier(Ultracraft.MOD_ID, "shaders/post/blurbg.json")); //disfunctional; Depth map don't work ;-;
	}
	
	void animateTabChanges(float delta)
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
		
		if(tab == Tab.PRESETS)
		{
			if(presetsAnim < 1f)
				presetsAnim += delta / 10f;
		}
		else if(presetsAnim > 0f && tab != Tab.PATTERNS)
		{
			presetsAnim -= delta / 5f;
			for (PreviewButton pb : previewButtons)
				pb.setAlpha(presetsAnim);
		}
		if(tab == Tab.PATTERNS)
		{
			if(patternsAnim < 1f)
				patternsAnim += delta / 10f;
		}
		else if(patternsAnim > 0f && tab != Tab.PRESETS)
		{
			patternsAnim -= delta / 5f;
			for (PreviewButton pb : previewButtons)
				pb.setAlpha(patternsAnim);
		}
		
		if(tab == Tab.MAIN && presetsAnim <= 0f && patternsAnim <= 0f && previewButtons.size() > 0)
		{
			for (PreviewButton pb : previewButtons)
				remove(pb);
			previewButtons.clear();
			presetsAnim = -0.5f;
			patternsAnim = -0.5f;
		}
		refreshPresetsButton.setAlpha(MathHelper.clamp(presetsAnim, 0.02f, 1f));
		supportButton.setAlpha(MathHelper.clamp(patternsAnim, 0.02f, 1f));
		refreshSupportButton.setAlpha(MathHelper.clamp(patternsAnim, 0.02f, 1f));
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
	public void renderBackground(DrawContext context)
	{
		MatrixStack matrices = context.getMatrices();
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
	
	Vec3d getStartColor(Boolean type)
	{
		return new Vec3d(type ? startMetalColor : startWingColor);
	}
	
	@Override
	public void close()
	{
		if(!UltracraftClient.isSupporter())
			UltracraftClient.setWingPattern("");
		client.setScreen(parent);
		client.options.hudHidden = wasHudHidden;
		client.options.setPerspective(oldPerspective);
		if(client.player != null)
			client.player.setPitch(prevPitch);
		MenuOpen = false;
		Ultraconfig config = UltracraftClient.getConfig();
		config.wingColors[0] = new Vec3d(UltracraftClient.getWingColors()[0]);
		config.wingColors[1] = new Vec3d(UltracraftClient.getWingColors()[1]);
		config.wingPreset = UltracraftClient.wingPreset;
		config.wingPattern = UltracraftClient.wingPattern;
		UltracraftClient.saveConfig();
		if(tab == Tab.PRESETS)
			WingColorPresetManager.unloadPresets();
		
		IWingDataComponent wings = UltraComponents.WING_DATA.get(client.player);
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBoolean(wings.isActive());
		buf.writeVector3f(wings.getColors()[0]);
		buf.writeVector3f(wings.getColors()[1]);
		buf.writeString(wings.getPattern());
		ClientPlayNetworking.send(PacketRegistry.SEND_WING_DATA_C2S_PACKET_ID, buf);
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		if(tab == Tab.PRESETS || tab == Tab.PATTERNS)
		{
			targetScroll += amount * -10;
			targetScroll = MathHelper.clamp(targetScroll, (previewButtons.size() / 2f - 1) * -24, 0);
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(refreshPresetsButton.isMouseOver(mouseX, mouseY))
			return refreshPresetsButton.mouseClicked(mouseX, mouseY, button);
		else if(supportButton.isMouseOver(mouseX, mouseY))
			return supportButton.mouseClicked(mouseX, mouseY, button);
		else if(refreshSupportButton.isMouseOver(mouseX, mouseY))
			return refreshSupportButton.mouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
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
		if(tab != Tab.PRESETS)
			WingColorPresetManager.loadPresets();
		curpreviewButtonscroll = targetScroll = 0f;
		tab = Tab.PRESETS;
		subTitle = Text.translatable("screen.ultracraft.wing-settings.presets.title");
		setMainTabActive(false);
		closeButton.setMessage(Text.translatable("screen.ultracraft.wing-settings.back"));
		populatePresetList();
		refreshPresetsButton.active = true;
		if((previewButtons.size() / 2) * 24 > height - 105)
			showScrollingHint = 6f;
	}
	
	void applyPreset(ButtonWidget button)
	{
		PreviewButton pb = ((PreviewButton)button);
		UltracraftClient.wingPreset = pb.id;
		UltracraftClient.setWingColor(pb.preset.wings().multiply(255).toVector3f(), 0);
		UltracraftClient.setWingColor(pb.preset.metal().multiply(255).toVector3f(), 1);
		top.setType(top.getPickerType());
		if(bottom != null)
			bottom.setType(bottom.getPickerType());
	}
	
	void refreshPresets(ButtonWidget button)
	{
		curpreviewButtonscroll = targetScroll = 0f;
		WingColorPresetManager.loadPresets();
		populatePresetList();
	}
	
	void populatePresetList()
	{
		if(previewButtons.size() > 0)
		{
			previewButtons.forEach(this::remove);
			previewButtons.clear();
		}
		List<String> ids = WingColorPresetManager.getAllIDs();
		for (int i = 0; i < ids.size(); i++)
		{
			boolean b = i % 2 == 0;
			WingColorPresetManager.WingColorPreset preset = WingColorPresetManager.getPreset(ids.get(i));
			PreviewButton pb = addDrawableChild(new PreviewButton(width - ((b ? 160 : 80) + 4), 42 + 24 * (i / 2), 76, 20,
					this::applyPreset, ids.get(i), preset, 0.5f + 0.1f * i));
			previewButtons.add(pb);
		}
	}
	
	void openPatterns()
	{
		tab = Tab.PATTERNS;
		subTitle = Text.translatable("screen.ultracraft.wing-settings.patterns.title");
		setMainTabActive(false);
		closeButton.setMessage(Text.translatable("screen.ultracraft.wing-settings.back"));
		populatePatternList();
		boolean supporter = UltracraftClient.isSupporter();
		supportButton.active = refreshSupportButton.active = !supporter;
		if(supporter)
		{
			supportButton.setMessage(Text.translatable("screen.ultracraft.wing-settings.patterns.supported"));
			refreshSupportButton.setTooltip(null);
		}
		if((previewButtons.size() / 2) * 24 > height - 105)
			showScrollingHint = 6f;
	}
	
	void populatePatternList()
	{
		if(previewButtons.size() > 0)
		{
			previewButtons.forEach(this::remove);
			previewButtons.clear();
		}
		List<String> ids = WingPatterns.getAllIDs();
		for (int i = 0; i < ids.size(); i++)
		{
			boolean b = i % 2 == 0;
			WingPatterns.WingPattern pattern = WingPatterns.getPattern(ids.get(i));
			PreviewButton pb = addDrawableChild(new PreviewButton(width - ((b ? 160 : 80) + 4), 42 + 24 * (i / 2), 76, 20,
					this::applyPattern, ids.get(i), pattern, 0.5f + 0.1f * i));
			previewButtons.add(pb);
		}
	}
	
	void applyPattern(ButtonWidget button)
	{
		PreviewButton pb = ((PreviewButton)button);
		boolean none = pb.id.equals("none");
		UltracraftClient.setWingPattern(none ? "" : pb.id);
		if(!UltracraftClient.isSupporter())
		{
			closeButton.active = none;
			closeButton.setTooltip(Tooltip.of(none ? Text.of("") : Text.translatable("screen.ultracraft.wing-settings.patterns.non-supporter-hint")));
		}
	}
	
	void backToMain()
	{
		if(tab == Tab.PRESETS)
		{
			WingColorPresetManager.unloadPresets();
			refreshPresetsButton.active = false;
		}
		if(tab == Tab.PATTERNS)
		{
			refreshSupportButton.active = false;
			supportButton.active = false;
		}
		tab = Tab.MAIN;
		subTitle = Text.empty();
		setMainTabActive(true);
		closeButton.setMessage(ScreenTexts.DONE);
		if(safeVFX)
			patternTabButton.active = false;
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
		public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
		{
			MinecraftClient client = MinecraftClient.getInstance();
			RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			int i = 1;
			if (!active)
				i = 0;
			else if (isSelected())
				i = 2;
			context.drawNineSlicedTexture(new Identifier(Ultracraft.MOD_ID, "textures/gui/widgets.png"), getX(), getY(), getWidth(), getHeight(), 20, 4, 200, 20, 0, i * 20);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			i = active ? 16777215 : 10526880;
			drawMessage(context, client.textRenderer, i | MathHelper.ceil(alpha * 255f) << 24);
		}
	}
	
	static class RefreshButton extends ButtonWidget
	{
		protected RefreshButton(int x, int y, int width, int height, Text message, PressAction onPress)
		{
			super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
		}
		
		@Override
		public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
		{
			MinecraftClient client = MinecraftClient.getInstance();
			RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			int i = 1;
			if (!active)
				i = 0;
			else if (isSelected())
				i = 2;
			context.drawNineSlicedTexture(new Identifier(Ultracraft.MOD_ID, "textures/gui/widgets.png"), getX(), getY(), getWidth(), getHeight(), 20, 20, 20, 20, 0, (3 + i) * 20);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			i = active ? 16777215 : 10526880;
			drawMessage(context, client.textRenderer, i | MathHelper.ceil(alpha * 255f) << 24);
		}
	}
	
	static class PreviewButton extends ButtonWidget
	{
		public final WingColorPresetManager.WingColorPreset preset;
		public final WingPatterns.WingPattern pattern;
		Vec3d wingColor, metalColor, textColor;
		String id;
		float appearTime, alphaCap, alphaUncapped;
		
		protected PreviewButton(int x, int y, int width, int height, PressAction onPress, String id, WingColorPresetManager.WingColorPreset preset, float appearTime)
		{
			super(x, y, width, height, Text.translatable(preset.name()), onPress, DEFAULT_NARRATION_SUPPLIER);
			this.preset = preset;
			this.pattern = null;
			wingColor = preset.wings();
			metalColor = preset.metal();
			textColor = preset.text();
			this.id = id;
			this.appearTime = appearTime;
			alpha = 0;
			MutableText tooltip = Text.empty();
			boolean author = preset.author() != null && preset.author().length() > 0, source = preset.source() != null && preset.source().length() > 0;
			if(author)
				tooltip.append(Text.translatable("screen.ultracraft.wing-settings.presets.author", preset.author()).append(source ? "\n" : ""));
			if(source)
				tooltip.append(Text.translatable("screen.ultracraft.wing-settings.presets.source", preset.source()));
			if(!tooltip.equals(Text.empty()))
				setTooltip(Tooltip.of(tooltip));
		}
		
		protected PreviewButton(int x, int y, int width, int height, PressAction onPress, String id, WingPatterns.WingPattern pattern, float appearTime)
		{
			super(x, y, width, height, Text.translatable("pattern.ultracraft." + id), onPress, DEFAULT_NARRATION_SUPPLIER);
			this.pattern = pattern;
			this.preset = null;
			wingColor = new Vec3d(new Vector3f(UltracraftClient.getWingColors()[0]).mul(id.equals("none") ? 1f / 255f : 1f));
			metalColor = new Vec3d(new Vector3f(UltracraftClient.getWingColors()[1]).mul(id.equals("none") ? 1f / 255f : 1f));
			textColor = pattern.textColor();
			this.id = id;
			this.appearTime = appearTime;
			alpha = 0;
			MutableText tooltip = Text.empty();
			if(pattern.hasFlavor())
				tooltip.append(Text.translatable("pattern.ultracraft." + id + ".flavor"));
			if(!tooltip.equals(Text.empty()))
				setTooltip(Tooltip.of(tooltip));
		}
		
		@Override
		public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
		{
			if(alpha < 1f && appearTime >= 0f)
			{
				appearTime -= delta / 20;
				setAlpha((0.1f - appearTime) * 10);
			}
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
			if((preset != null && id.equals(UltracraftClient.wingPreset)) ||
					   (pattern != null && (id.equals(UltracraftClient.wingPattern) || (id.equals("none") && UltracraftClient.wingPattern.equals("")))))
				i = 3;
			ShaderProgram wingShader = pattern == null ? UltracraftClient.getWingsColoredUIShaderProgram() : pattern.previewProgram().get();
			wingShader.getUniform("WingColor").set((float)wingColor.x, (float)wingColor.y, (float)wingColor.z);
			wingShader.getUniform("MetalColor").set((float)metalColor.x, (float)metalColor.y, (float)metalColor.z);
			RenderSystem.setShader(pattern == null ? UltracraftClient::getWingsColoredUIShaderProgram : pattern.previewProgram());
			RenderSystem.setShaderTexture(0, new Identifier(Ultracraft.MOD_ID, "textures/gui/preset_preview.png"));
			RenderingUtil.drawTexture(context.getMatrices().peek().getPositionMatrix(), new Vector4f(getX() + 1, getY() + 1, width, height),
					new Vec2f(76, 40), new Vector4f(0, 20, 76, -20));
			int c = i <= 1 ? 0xff000000 : i == 3 ? 0xfff4b41b : 0xffffffff;
			context.drawBorder(getX(), getY(), width + 2, height + 2, c);
			c = (0xff << 8) + (int)(textColor.x * 255);
			c = (c << 8) + (int)(textColor.y * 255);
			c = (c << 8) + (int)(textColor.z * 255);
			drawMessage(context, client.textRenderer, c | MathHelper.ceil(alpha * 255f) << 24);
		}
		
		public void setAlphaCap(float cap)
		{
			alphaCap = cap;
			alpha = MathHelper.clamp(alphaUncapped, 0f, alphaCap);
			active = this.alpha > 0.5f;
		}
		
		@Override
		public void setAlpha(float alpha)
		{
			alphaUncapped = alpha;
			super.setAlpha(MathHelper.clamp(alpha, 0f, alphaCap));
			active = this.alpha > 0.5f;
		}
	}
	
	enum Tab
	{
		MAIN,
		PRESETS,
		PATTERNS
	}
}
