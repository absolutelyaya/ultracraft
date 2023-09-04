package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.util.TerminalGuiRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.*;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.lang.Math;
import java.util.List;

public class TerminalBlockEntityRenderer extends GeoBlockRenderer<TerminalBlockEntity>
{
	public static final TerminalGuiRenderer GUI;
	static final TextRenderer textRenderer;
	static final int LIGHT = 15728880;
	
	public TerminalBlockEntityRenderer()
	{
		super(new TerminalBlockEntityModel());
	}
	
	@Override
	public void actuallyRender(MatrixStack poseStack, TerminalBlockEntity animatable, BakedGeoModel model, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		poseStack.push();
		poseStack.translate(0, -1, 0);
		super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.pop();
	}
	
	@Override
	public Identifier getTextureLocation(TerminalBlockEntity animatable)
	{
		return animatable.getBase().getTexture();
	}
	
	@Override
	public void postRender(MatrixStack matrices, TerminalBlockEntity animatable, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float tickDelta, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		animatable.setCaretTimer((animatable.getCaretTimer() + tickDelta / 20f) % 2f);
		//Draw interface
		GUI.setCurrentTerminal(animatable);
		float playerDist = (float) MinecraftClient.getInstance().player.getPos().distanceTo(animatable.getPos().toCenterPos());
		float displayVisibility = animatable.getDisplayVisibility();
		if(playerDist < 4f && displayVisibility < 1f)
			displayVisibility += tickDelta / 5f;
		else if(playerDist > 4f && displayVisibility > 0f)
			displayVisibility -= tickDelta / 3f;
		animatable.setDisplayVisibility(displayVisibility);
		if(displayVisibility > 0f)
		{
			animatable.setInactivity(animatable.getInactivity() + tickDelta / 20f);
			if(animatable.getSizeOverride() != null)
				animatable.setCurWindowSize(animatable.getCurWindowSize().lerp(animatable.getSizeOverride(), tickDelta / 5f));
			else
				animatable.setCurWindowSize(animatable.getCurWindowSize().lerp(animatable.getNormalWindowSize(), tickDelta / 5f));
		}
		else if(animatable.getInactivity() < 600f)
			animatable.setInactivity(600f);
		if(displayVisibility > 0f)
			renderDisplay(matrices, animatable, bufferSource);
		
		if(animatable.getGraffitiTexture() != null)
			renderGraffiti(matrices, animatable, bufferSource);
	}
	
	//Display Rendering
	void renderDisplay(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		PlayerEntity localPlayer = MinecraftClient.getInstance().player;
		if(!(localPlayer instanceof WingedPlayerEntity winged))
			return;
		//Transformation
		matrices.push();
		matrices.translate(0.5f, 0.5f, 0.5f);
		matrices.multiply(new Quaternionf(new AxisAngle4f(animatable.getRotation() * -MathHelper.RADIANS_PER_DEGREE,
				0f, 1f, 0f)));
		matrices.translate(0f, 0f, -0.1f);
		float displayVisibility = terminal.getDisplayVisibility();
		matrices.scale(Math.min(displayVisibility / 0.5f, 1f), displayVisibility, Math.min(displayVisibility / 0.5f, 1f));
		//Owner
		if(terminal.isFocused(winged) && animatable.getOwner() != null)
		{
			MinecraftServer server = MinecraftClient.getInstance().getServer();
			if(server != null)
			{
				ServerPlayerEntity ownerPlayer = server.getPlayerManager().getPlayer(terminal.getOwner());
				Text ownerText = Text.translatable("screen.ultracraft.terminal.owner", ownerPlayer != null ? ownerPlayer.getDisplayName() : terminal.getOwner());
				GUI.drawText(buffers, matrices, ownerText.getString(), 102 + (int)((animatable.getCurWindowSize().x - 100) / 2f), -textRenderer.fontHeight, 0.005f);
			}
		}
		//ScreenSaver
		if(animatable.getInactivity() > 30f)
			drawScreenSaver(matrices, buffers);
		else
		{
			switch (animatable.getTab().id)
			{
				case TerminalBlockEntity.Tab.MAIN_MENU_ID -> drawMainMenu(matrices, buffers);
				case TerminalBlockEntity.Tab.COMING_SOON_ID -> drawComingSoon(matrices, buffers);
				case TerminalBlockEntity.Tab.WEAPONS_ID -> drawWeapons(matrices, buffers);
				case TerminalBlockEntity.Tab.BESTIARY_ID -> drawBestiary(matrices, buffers);
				case TerminalBlockEntity.Tab.CUSTOMIZATION_ID -> drawCustomization(matrices, buffers);
				case TerminalBlockEntity.Tab.BASE_SELECT_ID -> drawBaseSelection(matrices, buffers);
				case TerminalBlockEntity.Tab.EDIT_SCREENSAVER_ID -> drawEditScreensaver(matrices, buffers);
				case TerminalBlockEntity.Tab.GRAFFITI_ID -> drawGraffitiTab(matrices, buffers);
				default -> animatable.getTab().renderCustomTab(matrices, terminal, buffers);
			}
		}
		
		if(!animatable.getTab().equals(TerminalBlockEntity.Tab.GRAFFITI))
		{
			matrices.translate(0f, 0f, -0.005f);
			Vector2d cursor = terminal.getCursor();
			GUI.drawBoxOutline(buffers, matrices, (int)(cursor.x * 100) - 1, (int)(cursor.y * 100) - 1, 1, 1, 0xffffffff);
		}
		//End Transform
		matrices.pop();
	}
	
	void renderGraffiti(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		//POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
		VertexConsumer consumer = buffers.getBuffer(RenderLayer.getEntityCutout(terminal.getGraffitiTexture()));
		matrices.push();
		float f = 1f / 16f;
		int c = 0xffffffff, o = OverlayTexture.DEFAULT_UV;
		matrices.translate(0.5f, 0.5f, 0.5f);
		matrices.scale(f, f, f);
		matrices.multiply(new Quaternionf(new AxisAngle4f(animatable.getRotation() * -MathHelper.RADIANS_PER_DEGREE,
				0f, 1f, 0f)));
		Quaternionf rotStep = new Quaternionf(new AxisAngle4f(90 * -MathHelper.RADIANS_PER_DEGREE, 0f, 1f, 0f));
		Matrix4f poseMatrix = new Matrix4f(matrices.peek().getPositionMatrix());
		Matrix3f normalMatrix = new Matrix3f(matrices.peek().getNormalMatrix());
		
		poseMatrix.rotate(rotStep);
		consumer.vertex(poseMatrix, 8f, -11f, 8.01f).color(c).texture(0.25f, 22f / 32f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 8f, 11f, 8.01f).color(c).texture(0.25f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0f, 11f, 8.01f).color(c).texture(0f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0f, -11f, 8.01f).color(c).texture(0f, 22f / 32f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		
		poseMatrix.rotate(rotStep);
		consumer.vertex(poseMatrix, -8f, -21f, -8.01f).color(c).texture(0.75f, 1f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, -8f, 11f, -8.01f).color(c).texture(0.75f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 8f, 11f, -8.01f).color(c).texture(0.25f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 8f, -21f, -8.01f).color(c).texture(0.25f, 1f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		
		poseMatrix.rotate(rotStep);
		consumer.vertex(poseMatrix, -8f, 11f, 8.01f).color(c).texture(0.75f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, -8f, -11f, 8.01f).color(c).texture(0.75f, 22f / 32f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0f, -11f, 8.01f).color(c).texture(1f, 22f / 32f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0f, 11f, 8.01f).color(c).texture(1f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		
		matrices.pop();
	}
	
	void drawScreenSaver(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawBG(matrices, buffers);
		List<String> lines = animatable.getLines();
		for (int i = 0; i < lines.size(); i++)
			GUI.drawText(buffers, matrices, lines.get(i), 2, textRenderer.fontHeight * (i + 1) - 108, 0.005f);
	}
	
	void drawMainMenu(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		if(animatable.isLocked() && !animatable.isOwner(MinecraftClient.getInstance().player.getUuid()))
		{
			GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.no-access", "!", true, "force-screensaver");
			return;
		}
		GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.main-menu", false);
		int y = 50;
		String t;
		if(animatable.isOwner(MinecraftClient.getInstance().player.getUuid()))
		{
			t = Text.translatable("screen.ultracraft.terminal.customize").getString();
			GUI.drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y,
					textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "customize");
		}
		y -= textRenderer.fontHeight + 5;
		t = Text.translatable("screen.ultracraft.terminal.bestiary").getString();
		GUI.drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y,
				textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "bestiary");
		y -= textRenderer.fontHeight + 5;
		t = Text.translatable("screen.ultracraft.terminal.weapons").getString();
		GUI.drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y,
				textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "weapons");
	}
	
	void drawComingSoon(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.coming-soon", "///", true, "mainmenu");
	}
	
	void drawWeapons(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.weapons", true);
	}
	
	void drawBestiary(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.bestiary", true);
	}
	
	void drawCustomization(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.customize", true);
		int y = 77;
		String t;
		if(MinecraftClient.getInstance().player instanceof WingedPlayerEntity winged && !animatable.equals(winged.getFocusedTerminal()))
		{
			t = Text.translatable("screen.ultracraft.terminal.focus-pls").getString();
			GUI.drawText(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y, 0.02f);
		}
		y -= textRenderer.fontHeight + 5;
		t = Text.translatable("screen.ultracraft.terminal.customize." + (animatable.isLocked() ? "locked" : "lock")).getString();
		GUI.drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y,
				textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "toggle-lock");
		y -= textRenderer.fontHeight + 5;
		t = Text.translatable("screen.ultracraft.terminal.customize.graffiti").getString();
		GUI.drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y,
				textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "graffiti");
		y -= textRenderer.fontHeight + 5;
		t = Text.translatable("screen.ultracraft.terminal.customize.base-clr").getString();
		GUI.drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y,
				textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "edit-base");
		y -= textRenderer.fontHeight + 5;
		t = Text.translatable("screen.ultracraft.terminal.customize.screensaver").getString();
		GUI.drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y,
				textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "edit-screensaver");
	}
	
	void drawBaseSelection(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.customize.base-clr", true, "customize");
		int[] colors = new int[] {
				0xffd7f6fa, 0xffcfc6b8, 0xff7d7071, 0xff282235,
				0xff994f51, 0xffbc2f27, 0xfff47e1b, 0xfffcc330,
				0xffb6d53c, 0xff397b44, 0xff4ae6bf, 0xff28ccdf,
				0xff3978a8, 0xff6e3696, 0xffaf3ca7, 0xffe98cd1
		};
		int size = 14;
		for (int y = 0; y < 4; y++)
			for (int x = 0; x < 4; x++)
				GUI.drawColorButton(buffers, matrices, colors[y * 4 + x], 16 + x * (size + 3), 18 + y * (size + 3), size, size, "set-base@" + (y * 4 + x));
	}
	
	void drawEditScreensaver(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawBG(matrices, buffers);
		GUI.drawBoxOutline(buffers, matrices, 0, 0, 100, 100, 0xffffffff);
		List<String> lines = animatable.getLines();
		for (int i = 0; i < lines.size(); i++)
			GUI.drawText(buffers, matrices, lines.get(i), 2, textRenderer.fontHeight * (i + 1) - 108, 0.005f);
		if(animatable.getCaretTimer() <= 1f)
		{
			Vector2i caret = animatable.getCaret();
			String before = (caret.x == 0 || lines.get(caret.y).length() == 0 ? "" : lines.get(caret.y).substring(0, caret.x));
			matrices.push();
			matrices.translate(0f, 0f, -0.005f);
			GUI.drawBox(buffers, matrices, textRenderer.getWidth(before) + 1, textRenderer.fontHeight * caret.y, 1, textRenderer.fontHeight,
					animatable.getTextColor());
			matrices.pop();
		}
		String t = Text.translatable("screen.ultracraft.terminal.button.back").getString();
		GUI.drawButton(buffers, matrices, t,  103, 100 - textRenderer.fontHeight - 2,
				textRenderer.getWidth(t) + 4, textRenderer.fontHeight + 2, "customize");
	}
	
	void drawGraffitiTab(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "screen.ultracraft.terminal.customize.graffiti", false, "customize");
		
		String t = Text.translatable("screen.ultracraft.terminal.focus-pls").getString();
		GUI.drawText(buffers, matrices, t, 50 - textRenderer.getWidth(t) / 2, -22 + textRenderer.fontHeight - 2, 0.005f, animatable.getTextColor());
	}
	
	static {
		textRenderer = MinecraftClient.getInstance().textRenderer;
		GUI = new TerminalGuiRenderer();
	}
}
