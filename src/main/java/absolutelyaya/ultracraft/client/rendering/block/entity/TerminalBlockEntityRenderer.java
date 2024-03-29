package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.gui.terminal.elements.Tab;
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
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.renderer.GeoBlockRenderer;

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
		TerminalBlockEntity.Base base = animatable.getBase();
		if(base.equals(TerminalBlockEntity.Base.RGB))
			return new Identifier(Ultracraft.MOD_ID, "procedural/terminal_base/" + animatable.getTerminalID().toString());
		return base.getTexture();
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
				case Tab.COMING_SOON_ID -> drawComingSoon(matrices, buffers);
				case Tab.BESTIARY_ID -> drawBestiary(matrices, buffers);
				default -> animatable.getTab().render(matrices, terminal, buffers);
			}
		}
		
		if(!animatable.getTab().id.equals(Tab.GRAFFITI_ID))
		{
			matrices.translate(0f, 0f, -0.005f);
			Vector2d cursor = terminal.getCursor();
			if(!animatable.getTab().drawCustomCursor(matrices, buffers, cursor))
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
		consumer.vertex(poseMatrix, 8f, -24f, 8.01f).color(c).texture(0.2f, 1f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 8f, 16f, 8.01f).color(c).texture(0.2f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0f, 16f, 8.01f).color(c).texture(0f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0f, -24f, 8.01f).color(c).texture(0f, 1f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		
		poseMatrix.rotate(rotStep);
		consumer.vertex(poseMatrix, -8f, -24f, -8.01f).color(c).texture(0.6f, 1f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, -8f, 16f, -8.01f).color(c).texture(0.6f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 8f, 16f, -8.01f).color(c).texture(0.2f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 8f, -24f, -8.01f).color(c).texture(0.2f, 1f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		
		poseMatrix.rotate(rotStep);
		consumer.vertex(poseMatrix, -8f, 16f, 8.01f).color(c).texture(0.6f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, -8f, -24f, 8.01f).color(c).texture(0.6f, 1f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0f, -24f, 8.01f).color(c).texture(0.8f, 1f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0f, 16f, 8.01f).color(c).texture(0.8f, 0f).overlay(o).light(LIGHT).normal(normalMatrix, 0f, 0f, -1f).next();
		
		matrices.pop();
	}
	
	void drawScreenSaver(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawBG(matrices, buffers);
		List<String> lines = animatable.getScreensaver();
		for (int i = 0; i < lines.size(); i++)
			GUI.drawText(buffers, matrices, Text.translatable(lines.get(i)).getString(), 2, textRenderer.fontHeight * (i + 1) - 108, 0.005f);
	}
	
	void drawComingSoon(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "terminal.coming-soon", "///", Tab.DEFAULT_RETURN_BUTTON);
	}
	
	void drawWeapons(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "terminal.weapons", Tab.DEFAULT_RETURN_BUTTON);
	}
	
	void drawBestiary(MatrixStack matrices, VertexConsumerProvider buffers)
	{
		GUI.drawTab(matrices, buffers, "terminal.bestiary", Tab.DEFAULT_RETURN_BUTTON);
	}
	
	static {
		textRenderer = MinecraftClient.getInstance().textRenderer;
		GUI = new TerminalGuiRenderer();
	}
}
