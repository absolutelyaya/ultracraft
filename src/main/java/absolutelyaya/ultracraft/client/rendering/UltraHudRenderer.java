package absolutelyaya.ultracraft.client.rendering;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.Ultraconfig;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.item.MachineSwordItem;
import absolutelyaya.ultracraft.item.PlushieItem;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.joml.*;

import java.lang.Math;
import java.util.Random;

@SuppressWarnings("SameParameterValue")
public class UltraHudRenderer extends DrawableHelper
{
	private static final Ultraconfig config = UltracraftClient.getConfigHolder().get();
	final Identifier GUI_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/ultrahud.png");
	final Identifier WEAPONS_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/weapon_icons.png");
	final Identifier CROSSHAIR_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/crosshair_stats.png");
	float healthPercent, staminaPercent, yOffset;
	static float fishTimer;
	static ItemStack lastCatch;
	static int fishCaught;
	final String[] fishMania = new String[] {"message.ultracraft.fish.mania1", "message.ultracraft.fish.mania2", "message.ultracraft.fish.mania3", "message.ultracraft.fish.mania4"};
	final Random rand = new Random();
	
	public void render(float tickDelta, Camera cam)
	{
		if(!MinecraftClient.isHudEnabled() ||cam.isThirdPerson() || config.ultraHudVisibility.equals(UltraHudVisibility.NEVER))
			return;
		if(config.ultraHudVisibility.equals(UltraHudVisibility.LIMITED) && !UltracraftClient.isHiVelEnabled())
			return;
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if(player == null)
			return;
		if(player.isSpectator())
			return;
		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		
		MatrixStack matrices = new MatrixStack();
		int width = client.getWindow().getFramebufferWidth();
		int height = client.getWindow().getFramebufferHeight();
		float aspect = (float)width / (float)height;
		matrices.peek().getPositionMatrix().perspective(90 * 0.0174f,
				aspect, 0.05F, client.gameRenderer.getViewDistance() * 4.0F);
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrices.peek().getPositionMatrix());
		RenderSystem.enableBlend();
		
		//Fishing Joke
		if(fishTimer > 0f && lastCatch != null && config.fishingJoke)
		{
			matrices.push();
			matrices.scale(aspect, 1f, 1f);
			matrices.push();
			matrices.scale(0.001f, -0.001f, 0.001f);
			TextRenderer render = client.textRenderer;
			Text t;
			drawText(matrices, t = Text.translatable("message.ultracraft.fish.caught", lastCatch.getName()),
					-render.getWidth(t) / 2f, -32f, 1f);
			drawText(matrices, t = Text.translatable("message.ultracraft.fish.size", fishCaught == 69 ? "1.5" : "1"),
					-render.getWidth(t) / 2f, 16f, 1f);
			float fishManiaLevel = Math.max(0, fishCaught - 10) / 32f;
			drawText(matrices, t = fishCaught == 69 ? Text.translatable("message.ultracraft.fish.mania5") :
										   Text.translatable(fishMania[fishCaught % fishMania.length]),
					-render.getWidth(t) / 2f + (rand.nextFloat() - 0.5f) * fishManiaLevel / 2f, 32f + (rand.nextFloat() - 0.5f) * fishManiaLevel / 2f,
					MathHelper.clamp(0.5f * fishManiaLevel, 0.05f, 1f));
			matrices.scale(0.5f, 0.5f, 0.5f);
			if(fishCaught < 5)
				drawText(matrices, t = Text.translatable("message.ultracraft.fish.disable"),
						-render.getWidth(t) / 2f, 128f, 0.5f);
			matrices.pop();
			matrices.push();
			matrices.translate(0f, 0f, 3f);
			matrices.multiply(new Quaternionf(new AxisAngle4f(-fishTimer / 0.75f, 0f, 1f, 0f)));
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-45.0), 0f, 0f, 1f)));
			VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
			drawItem(matrices, new Matrix4f(matrices.peek().getPositionMatrix()), client, immediate, lastCatch, false);
			immediate.draw();
			matrices.pop();
			fishTimer -= tickDelta / 20;
			matrices.pop();
		}
		RenderSystem.enableBlend();
		
		healthPercent = MathHelper.lerp(tickDelta, healthPercent, player.getHealth() / player.getMaxHealth());
		staminaPercent = MathHelper.lerp(tickDelta, staminaPercent, ((WingedPlayerEntity)player).getStamina() / 90f);
		//Crosshair
		if(config.ultraHudCrosshair)
		{
			matrices.push();
			matrices.scale(aspect, 1f, 1f);
			matrices.translate(-0.75, -0.5, 0);
			RenderSystem.setShaderTexture(0, CROSSHAIR_TEXTURE);
			drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(-6f, -5f, 5f, 11f * healthPercent), 100f,
					new Vec2f(32f, 32f), new Vector4f(0f, 11 - 11f * healthPercent, 5f, 11f * healthPercent), 0.75f);
			drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(2f, -5f, 5f, 11f * staminaPercent), 100f,
					new Vec2f(32f, 32f), new Vector4f(8f, 11 - 11f * staminaPercent, 5f, 11f * staminaPercent), 0.75f);
			matrices.pop();
		}
		
		matrices.push();
		matrices.scale(0.8f, -0.5f, 0.5f);
		
		float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
		float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
		boolean flip = player.getMainArm().equals(Arm.LEFT) ^ config.switchSides;
		matrices.multiply(new Quaternionf().rotateX((float)Math.toRadians((player.getPitch(tickDelta) - h) * 0.15f)));
		matrices.multiply(new Quaternionf().rotateY((float)Math.toRadians((player.getYaw(tickDelta) - i) * -0.05f)));
		matrices.multiply(new Quaternionf(new AxisAngle4f(-0.6f * (flip ? -1 : 1), 0f, 1f, 0f)));
		matrices.translate(-15 * (flip ? -4 : 1), 0, 150);
		if(config.switchSides)
			matrices.translate(0, -30, 0);
		if(config.moveUltrahud)
		{
			Hand leftHand = player.getMainArm().equals(Arm.LEFT) ? Hand.MAIN_HAND : Hand.OFF_HAND;
			Hand rightHand = leftHand.equals(Hand.OFF_HAND) ? Hand.MAIN_HAND : Hand.OFF_HAND;
			boolean covered = (!(player.getStackInHand(leftHand).isEmpty()) && !flip) || (!player.getStackInHand(rightHand).isEmpty() && flip);
			matrices.translate(0f, yOffset = MathHelper.lerp(tickDelta, yOffset, covered ? -64f + (config.switchSides ? 30 : 0) : 0f), 0f);
		}
		
		matrices.push();
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		matrices.translate(1f, 35f, 0f);
		matrices.scale(1f, -1f, 1f);
		Matrix4f textureMatrix = new Matrix4f(matrices.peek().getPositionMatrix());
		drawTexture(textureMatrix, new Vector4f(-60f, -35f, 67.5f, 67.5f), 0f,
				new Vec2f(80f, 64f), new Vector4f(0f, 0f, 48f, 48f), 0.75f);
		//bars
		//health
		drawTexture(textureMatrix, new Vector4f(-60f + 2.8125f, -35f + 11.250f, 61.875f * healthPercent, 5.625f), 0f,
				new Vec2f(80f, 64f), new Vector4f(2f, 48f, 44f * healthPercent, 4f), 1f);
		//stamina
		drawTexture(textureMatrix, new Vector4f(-60f + 2.8125f, -35f + 2.8125f, 61.875f * staminaPercent, 5.625f), 0f,
				new Vec2f(80f, 64f), new Vector4f(2f, 52f, 44f * staminaPercent, 4f), 1f);
		//Railgun
		if(false) //if hasRailgun
		{
			drawTexture(textureMatrix, new Vector4f(-60f + 68.90625f, -35 + 21.09f, 21.09f, 46.4062f), 0f,
					new Vec2f(80f, 64f), new Vector4f(49f, 0f, 15f, 33f), 0.75f);
		}
		//Fist
		if(false) //if hasFist
		{
			drawTexture(textureMatrix, new Vector4f(-60f + 68.90625f, -35f, 21.09f, 19.68f), 0f,
					new Vec2f(80f, 64f), new Vector4f(49f, 34f, 15f, 14f), 0.75f);
		}
		
		if(config.moveUltrahud)
			matrices.translate(0f, yOffset / 7f - (config.switchSides ? 7 : 0), 0f);
		//UltraHotbar
		matrices.push();
		matrices.scale(30, 30, -30);
		matrices.translate(-0.025, 0.425, 0);
		if(flip)
			matrices.translate(-1.75, 0, 0);
		if(cam.getSubmersionType().equals(CameraSubmersionType.WATER))
		{
			matrices.translate(0.45, 0.025, -0.5);
			matrices.scale(1.05f, 1f, 1f);
		}
		matrices.multiply(new Quaternionf(new AxisAngle4f(0.12f, -1f, 1f * (flip ? -1 : 1), 0f)));
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		Matrix3f normal = matrices.peek().getNormalMatrix();
		normal.set(new Quaternionf().rotateXYZ((float)Math.toRadians(cam.getPitch()), (float)Math.toRadians(cam.getYaw()), 0f));
		matrices.push();
		matrices.scale(0.5f, 0.5f, 0.5f);
		matrices.multiply(new Quaternionf(new AxisAngle4f(-0.18f, 0f, 1f * (flip ? -1 : 1), 0f)));
		matrices.translate(1.25, -0.25, 0);
		
		ItemStack mainHand = player.getMainHandStack();
		ItemStack stack = player.getInventory().getStack((player.getInventory().selectedSlot + 1) % 9);
		if(!shouldRenderSpriteInstead(mainHand.getItem()))
			drawItem(matrices, textureMatrix, client, immediate, stack, false); // right
		int lastSlot = (player.getInventory().selectedSlot - 1) % 9;
		stack = player.getInventory().getStack(lastSlot == -1 ? 8 : lastSlot);
		matrices.translate(-2.5, 0, 0);
		matrices.multiply(new Quaternionf(new AxisAngle4f(0.18f, 0f, 1f * (flip ? -1 : 1), 0f)));
		if(!shouldRenderSpriteInstead(mainHand.getItem()))
			drawItem(matrices, textureMatrix, client, immediate, stack, false); // middle
		matrices.pop();
		drawItem(matrices, textureMatrix, client, immediate, mainHand, true); // left
		matrices.pop();
		matrices.pop();
		
		int hdt = ((WingedPlayerEntity)player).getWingHintDisplayTicks();
		if(hdt > 0)
		{
			if(config.moveUltrahud)
				matrices.translate(0f, yOffset * 0.7f - (config.switchSides ? 7 : 0), 0f);
			matrices.translate(5, 0, 150);
			drawText(matrices, Text.translatable(
					UltracraftClient.isHiVelEnabled() ? "message.ultracraft.hi-vel.enable" : "message.ultracraft.hi-vel.disable"),
					-80 + (flip ? -40 : 0), -10, Math.min(hdt / 20f, 1f));
		}
		matrices.pop();
		RenderSystem.restoreProjectionMatrix();
	}
	
	boolean shouldRenderSpriteInstead(Item item)
	{
		return item instanceof AbstractWeaponItem || item instanceof MachineSwordItem || item instanceof PlushieItem;
	}
	
	void drawItem(MatrixStack matrices, Matrix4f textureMatrix, MinecraftClient client, VertexConsumerProvider immediate, ItemStack stack, boolean hand)
	{
		Item item = stack.getItem();
		if(shouldRenderSpriteInstead(item))
		{
			if(!hand)
				return;
			Vector2i uv = new Vector2i(3, 5);
			if(item instanceof AbstractWeaponItem weapon)
				uv = weapon.getHUDTexture();
			else if (item.equals(ItemRegistry.MACHINE_SWORD))
				uv = new Vector2i(0, 5);
			else if (item instanceof PlushieItem)
				uv = new Vector2i(3, 0);
			RenderSystem.setShaderTexture(0, WEAPONS_TEXTURE);
			drawTexture(textureMatrix, new Vector4f(-60f, -35f + 22.5f, 67.5f, 45f), 0f,
					new Vec2f(192, 192), new Vector4f(uv.x * 48f, uv.y * 32f, 48f, 32f), 0.75f);
		}
		else
		{
			//This HUD is *terribly* made, and entity item models don't render correctly. To hide this, I did the following :D
			if(item.equals(Items.SHIELD))
				stack = ItemRegistry.FAKE_SHIELD.getDefaultStack();
			else if(item instanceof BannerItem)
				stack = ItemRegistry.FAKE_BANNER.getDefaultStack();
			client.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI,
					15728880, OverlayTexture.DEFAULT_UV, matrices, immediate, client.world, 1);
		}
	}
	
	void drawText(MatrixStack matrices, Text text, float x, float y, float alpha)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		client.textRenderer.draw(text, x, y, Color.ofRGBA(1f, 1f, 1f, alpha).getColor(), false,
				matrix, immediate, TextRenderer.TextLayerType.NORMAL, Color.ofRGBA(0f, 0f, 0f, 0.5f * alpha).getColor(), 15728880);
		matrix.translate(0f, 0f, -0.1f);
		client.textRenderer.draw(text, x, y, Color.ofRGBA(1f, 1f, 1f, alpha).getColor(), false,
				matrix, immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
	}
	
	void drawTexture(Matrix4f matrix, Vector4f transform, float z, Vec2f textureSize, Vector4f uv, float alpha)
	{
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		uv = new Vector4f(uv.x() / textureSize.x, uv.y() / textureSize.y,
				uv.z() / textureSize.x, uv.w() / textureSize.y);
		bufferBuilder.vertex(matrix, transform.x(), transform.y() + transform.w(), z)
				.texture(uv.x(), uv.y()).next();
		bufferBuilder.vertex(matrix, transform.x() + transform.z(), transform.y() + transform.w(), z)
				.texture(uv.x() + uv.z(), uv.y()).next();
		bufferBuilder.vertex(matrix, transform.x() + transform.z(), transform.y(), z)
				.texture(uv.x() + uv.z(), uv.y() + uv.w()).next();
		bufferBuilder.vertex(matrix, transform.x(), transform.y(), z)
				.texture(uv.x(), uv.y() + uv.w()).next();
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		BufferRenderer.resetCurrentVertexBuffer();
	}
	
	public static void onCatchFish(ItemStack stack)
	{
		if(UltracraftClient.getConfigHolder().get().fishingJoke)
		{
			lastCatch = stack;
			fishTimer = 10f;
			fishCaught++;
		}
	}
	
	@SuppressWarnings("unused")
	public enum UltraHudVisibility
	{
		ALWAYS,
		LIMITED,
		NEVER
	}
}
