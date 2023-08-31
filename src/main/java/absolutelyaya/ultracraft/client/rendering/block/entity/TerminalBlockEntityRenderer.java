package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.*;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.lang.Math;
import java.text.MessageFormat;
import java.util.List;

public class TerminalBlockEntityRenderer extends GeoBlockRenderer<TerminalBlockEntity>
{
	static final TextRenderer textRenderer;
	static final int LIGHT = 15728880;
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/terminal.png");
	
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
	public void postRender(MatrixStack poseStack, TerminalBlockEntity animatable, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		//Draw interface
		float playerDist = (float) MinecraftClient.getInstance().player.getPos().distanceTo(animatable.getPos().toCenterPos());
		float displayVisibility = animatable.getDisplayVisibility();
		if(playerDist < 4f && displayVisibility < 1f)
			displayVisibility += partialTick / 5f;
		else if(playerDist > 4f && displayVisibility > 0f)
			displayVisibility -= partialTick / 3f;
		animatable.setDisplayVisibility(displayVisibility);
		if(displayVisibility > 0f)
		{
			animatable.setInactivity(animatable.getInactivity() + partialTick / 20f);
			if(animatable.getSizeOverride() != null)
				animatable.setCurWindowSize(animatable.getCurWindowSize().lerp(animatable.getSizeOverride(), partialTick / 5f));
			else
				animatable.setCurWindowSize(animatable.getCurWindowSize().lerp(animatable.getNormalWindowSize(), partialTick / 5f));
		}
		else if(animatable.getInactivity() < 600f)
			animatable.setInactivity(600f);
		if(displayVisibility > 0f)
			renderDisplay(poseStack, animatable, bufferSource);
		
		//TODO: Draw Paintables
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
			ServerPlayerEntity ownerPlayer = MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(terminal.getOwner());
			Text ownerText = Text.translatable("screen.ultracraft.terminal.owner", ownerPlayer != null ? ownerPlayer.getDisplayName() : terminal.getOwner());
			drawText(buffers, matrices, ownerText.getString(), 102 + (int)((animatable.getCurWindowSize().x - 100) / 2f), -textRenderer.fontHeight, 0.005f);
		}
		//ScreenSaver
		if(animatable.getInactivity() > 30f)
			drawScreenSaver(matrices, buffers);
		else
		{
			switch (animatable.getTab())
			{
				case MAIN_MENU -> drawMainMenu(matrices, buffers);
				case COMING_SOON -> drawComingSoon(matrices, buffers);
				case WEAPONS -> drawWeapons(matrices, buffers);
				case BESTIARY -> drawBestiary(matrices, buffers);
				case CUSTOMIZATION -> drawCustomization(matrices, buffers);
			}
		}
		
		//if(winged.getFocusedTerminal() == null)
		//{
			matrices.translate(0f, 0f, -0.005f);
			Vector2d cursor = terminal.getCursor();
			drawBoxOutline(buffers, matrices, (int)(cursor.x * 100) - 1, (int)(cursor.y * 100) - 1, 1, 1, 0xffffffff);
		//}
		//End Transform
		matrices.pop();
	}
	
	void drawScreenSaver(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		drawBG(matrices, buffers);
		List<String> lines = animatable.getLines();
		for (int i = 0; i < lines.size(); i++)
			drawText(buffers, matrices, lines.get(i), 2, textRenderer.fontHeight * (i + 1) - 108, 0.005f);
	}
	
	void drawMainMenu(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		drawTab(matrices, buffers, "screen.ultracraft.terminal.main-menu", false);
		int y = 50;
		String t = Text.translatable("screen.ultracraft.terminal.customize").getString();
		drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y,
				textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "customize");
		y -= textRenderer.fontHeight + 5;
		t = Text.translatable("screen.ultracraft.terminal.bestiary").getString();
		drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y,
				textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "bestiary");
		y -= textRenderer.fontHeight + 5;
		t = Text.translatable("screen.ultracraft.terminal.weapons").getString();
		drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, y,
				textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "weapons");
	}
	
	void drawComingSoon(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		drawTab(matrices, buffers, "screen.ultracraft.terminal.coming-soon", "///", true);
	}
	
	void drawWeapons(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		drawTab(matrices, buffers, "screen.ultracraft.terminal.weapons", true);
	}
	
	void drawBestiary(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		drawTab(matrices, buffers, "screen.ultracraft.terminal.bestiary", true);
	}
	
	void drawCustomization(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		drawTab(matrices, buffers, "screen.ultracraft.terminal.customization", true);
	}
	
	void drawTab(MatrixStack matrices, VertexConsumerProvider buffers, String title, boolean returnButton)
	{
		drawTab(matrices, buffers, title, "-", returnButton);
	}
	
	void drawTab(MatrixStack matrices, VertexConsumerProvider buffers, String title, String titleBorder, boolean returnButton)
	{
		String t = MessageFormat.format("{0} {1} {0}", titleBorder, Text.translatable(title).getString());
		drawBG(matrices, buffers);
		drawText(buffers, matrices, t, 50 - textRenderer.getWidth(t) / 2, - 100 + textRenderer.fontHeight, 0.01f);
		if(returnButton)
		{
			t = Text.translatable("screen.ultracraft.terminal.button.back").getString();
			drawButton(buffers, matrices, t, 48 - textRenderer.getWidth(t) / 2, 95 - textRenderer.fontHeight,
					textRenderer.getWidth(t) + 2, textRenderer.fontHeight + 2, "mainmenu");
		}
	}
	
	void drawBG(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		int BGcolor = 0xaa000000;
		Vector2f size = animatable.getCurWindowSize();
		drawBox(buffers, matrices, (int)(-(size.x - 100) / 2f), 0, size.x, size.y, BGcolor);
	}
	
	void drawMultiLine(VertexConsumerProvider buffers, MatrixStack matrices, String text, int x, int y, float z)
	{
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++)
			drawText(buffers, matrices, lines[i], x, y - textRenderer.fontHeight * i, z);
	}
	
	void drawText(VertexConsumerProvider buffers, MatrixStack matrices, String text, int x, int y, float z)
	{
		drawText(buffers, matrices, text, x, y, z, animatable.getTextColor());
	}
	
	void drawText(VertexConsumerProvider buffers, MatrixStack matrices, String text, int x, int y, float z, int color)
	{
		matrices.push();
		matrices.translate(0.5f, -0.5f, -z);
		matrices.scale(-0.01f, -0.01f, 0.01f);
		textRenderer.draw(text, x, y, color, false,
				new Matrix4f(matrices.peek().getPositionMatrix()), buffers, TextRenderer.TextLayerType.NORMAL, 0x00000000, LIGHT, false);
		matrices.pop();
	}
	
	void drawButton(VertexConsumerProvider buffers, MatrixStack matrices, String text, int x, int y, int sizeX, int sizeY, String action)
	{
		Vector2d cursor = new Vector2d(animatable.getCursor()).mul(100f);
		boolean hovered = cursor.x > x && cursor.x < x + sizeX && cursor.y > y && cursor.y < y + sizeY;
		matrices.push();
		drawBoxOutline(buffers, matrices, x, y, sizeX, sizeY);
		if(hovered)
		{
			matrices.translate(0f, 0f, -0.005f);
			drawBox(buffers, matrices, x, y, sizeX, sizeY, animatable.getTextColor());
			animatable.setLastHovered(action);
		}
		else if(animatable.getLastHovered() != null && animatable.getLastHovered().equals(action))
			animatable.setLastHovered(null);
		matrices.translate(0f, 1f, -0.01f);
		drawText(buffers, matrices, text, x + 2, y + 2, 0f, hovered ? 0xff000000 : animatable.getTextColor());
		matrices.pop();
	}
	
	void drawBoxOutline(VertexConsumerProvider buffers, MatrixStack matrices, int x, int y, int sizeX, int sizeY)
	{
		drawBoxOutline(buffers, matrices, x, y, sizeX, sizeY, animatable.getTextColor());
	}
	
	void drawBoxOutline(VertexConsumerProvider buffers, MatrixStack matrices, int x, int y, int sizeX, int sizeY, int color)
	{
		matrices.push();
		matrices.translate(0f, 0f, -0.005f);
		drawBox(buffers, matrices, x, y + sizeY, sizeX, 1, color);
		drawBox(buffers, matrices, x, y - 1, sizeX, 1, color);
		drawBox(buffers, matrices, x - 1, y, 1, sizeY, color);
		drawBox(buffers, matrices, x + sizeX, y, 1, sizeY, color);
		matrices.pop();
	}
	
	void drawBox(VertexConsumerProvider buffers, MatrixStack matrices, int x, int y, float sizeX, float sizeY, int color)
	{
		matrices.push();
		VertexConsumer consumer = buffers.getBuffer(RenderLayer.getGui());
		matrices.translate(0.5f, 0.5f, 0f);
		matrices.scale(-0.01f, -0.01f, -1f);
		Matrix4f poseMatrix = new Matrix4f(matrices.peek().getPositionMatrix());
		Matrix3f normalMatrix = new Matrix3f(matrices.peek().getNormalMatrix());
		consumer.vertex(poseMatrix, x, y, 0f).color(color).texture(0, 0).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, x, y + sizeY, 0f).color(color).texture(0, 1).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, x + sizeX, y + sizeY, 0f).color(color).texture(1, 1).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, x + sizeX, y, 0f).color(color).texture(1, 0).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		matrices.pop();
	}
	
	static {
		textRenderer = MinecraftClient.getInstance().textRenderer;
	}
}
