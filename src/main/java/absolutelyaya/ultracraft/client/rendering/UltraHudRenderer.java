package absolutelyaya.ultracraft.client.rendering;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.Ultraconfig;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.components.player.IArmComponent;
import absolutelyaya.ultracraft.components.player.IWingDataComponent;
import absolutelyaya.ultracraft.item.*;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.util.RenderingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import me.shedaniel.math.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import org.joml.*;

import java.lang.Math;
import java.util.Random;

@SuppressWarnings("SameParameterValue")
public class UltraHudRenderer
{
	private static final Ultraconfig config = UltracraftClient.getConfig();
	final Identifier GUI_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/ultrahud.png");
	final Identifier WEAPONS_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/weapon_icons.png");
	final Identifier CROSSHAIR_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/crosshair_stats.png");
	float healthPercent, staminaPercent, absorptionPercent, yOffset;
	static float fishTimer, coinTimer, coinRot = 0, coinRotDest = 0, wingHintDisplayTimer, whitelistHintDisplayTimer;
	static ItemStack lastCatch;
	static int coinCombo;
	final String[] fishMania = new String[] {"message.ultracraft.fish.mania1", "message.ultracraft.fish.mania2", "message.ultracraft.fish.mania3", "message.ultracraft.fish.mania4"};
	final Random rand = new Random();
	
	public UltraHudRenderer()
	{
		super();
	}
	
	public void render(float delta, Camera cam)
	{
		if(!MinecraftClient.isHudEnabled() ||cam.isThirdPerson() || config.ultraHudVisibility.equals(UltraHudVisibility.NEVER))
			return;
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if(player == null || player.isSpectator())
			return;
		if(player instanceof WingedPlayerEntity winged && winged.getFocusedTerminal() != null)
			return;
		renderExtras(delta);
		IWingDataComponent wings = UltraComponents.WING_DATA.get(player);
		boolean wingsActive = wings.isActive();
		if(config.ultraHudVisibility.equals(UltraHudVisibility.LIMITED) && !wingsActive)
			return;
		
		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		MatrixStack matrices = new MatrixStack();
		int width = client.getWindow().getFramebufferWidth();
		int height = client.getWindow().getFramebufferHeight();
		float aspect = (float)height / (float)width;
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(client.gameRenderer.getBasicProjectionMatrix(90), VertexSorter.BY_DISTANCE);
		RenderSystem.enableBlend();
		
		healthPercent = MathHelper.lerp(delta, healthPercent, player.getHealth() / player.getMaxHealth());
		staminaPercent = MathHelper.lerp(delta, staminaPercent, UltraComponents.WINGED_ENTITY.get(player).getStamina() / 90f);
		absorptionPercent = MathHelper.lerp(delta, absorptionPercent, Math.min(player.getAbsorptionAmount() / 20f, 1f));
		//Crosshair
		if(config.ultraHudCrosshair)
		{
			matrices.push();
			matrices.scale(aspect, 1f, 1f);
			matrices.translate(-0.75, -0.5, 0);
			RenderSystem.setShaderTexture(0, CROSSHAIR_TEXTURE);
			RenderingUtil.drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(-6f, -5f, 5f, 11f * healthPercent), 100f,
					new Vec2f(32f, 32f), new Vector4f(0f, 11 - 11f * healthPercent, 5f, 11f * healthPercent), 0.75f);
			RenderingUtil.drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(2f, -5f, 5f, 11f * staminaPercent), 100f,
					new Vec2f(32f, 32f), new Vector4f(8f, 11 - 11f * staminaPercent, 5f, 11f * staminaPercent), 0.75f);
			matrices.pop();
		}
		
		matrices.push();
		boolean flip = player.getMainArm().equals(Arm.LEFT) ^ config.switchSides;
		if(!UltracraftClient.getConfig().ultraHudFixed)
		{
			float h = MathHelper.lerp(delta, player.lastRenderPitch, player.renderPitch);
			float i = MathHelper.lerp(delta, player.lastRenderYaw, player.renderYaw);
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((player.getPitch(delta) - h) * 0.15f));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((player.getYaw(delta) - i) * 0.05f));
		}
		matrices.translate(flip ? 60 : -48, 0, -50);
		if(config.switchSides)
			matrices.translate(0, -30, 0);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip ? -10 : 10));
		if(config.moveUltrahud)
		{
			Hand leftHand = player.getMainArm().equals(Arm.LEFT) ? Hand.MAIN_HAND : Hand.OFF_HAND;
			Hand rightHand = leftHand.equals(Hand.OFF_HAND) ? Hand.MAIN_HAND : Hand.OFF_HAND;
			boolean covered = (!(player.getStackInHand(leftHand).isEmpty()) && !flip) || (!player.getStackInHand(rightHand).isEmpty() && flip);
			matrices.translate(0f, yOffset = MathHelper.lerp(delta, yOffset, covered ? 30f + (config.switchSides ? 30 : 0) : 0f), 0f);
		}
		
		matrices.push();
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		matrices.translate(-24, -36, 0f);
		int guiScale = client.options.getGuiScale().getValue();
		if(guiScale == 0)
			guiScale = 3;
		float scale = guiScale * 0.2f;
		matrices.scale(scale, scale, scale);
		//main box
		Matrix4f textureMatrix = new Matrix4f(matrices.peek().getPositionMatrix());
		RenderingUtil.drawTexture(textureMatrix, new Vector4f(0, 0, 48f, 48f), 0f,
				new Vec2f(80f, 64f), new Vector4f(0f, 0f, 48f, 48f), 0.75f);
		//bars
		//health
		RenderingUtil.drawTexture(textureMatrix, new Vector4f(2, 8, 44 * healthPercent, 4), 0f,
				new Vec2f(80f, 64f), new Vector4f(2f, 48f, 44f * healthPercent, 4f), 1f);
		if(absorptionPercent > 0f)
			RenderingUtil.drawTexture(textureMatrix, new Vector4f(2, 8, 44 * absorptionPercent, 4), 0f,
					new Vec2f(80f, 64f), new Vector4f(2f, 56f, 44f * absorptionPercent, 4f), 1f);
		//stamina
		RenderingUtil.drawTexture(textureMatrix, new Vector4f(2, 2, 44 * staminaPercent, 4), 0f,
				new Vec2f(80f, 64f), new Vector4f(2f, 52f, 44f * staminaPercent, 4f), 1f);
		//Railgun
		if(false) //if hasRailgun
		{
			RenderingUtil.drawTexture(textureMatrix, new Vector4f(49, 15, 15, 33), 0f,
					new Vec2f(80f, 64f), new Vector4f(49f, 0f, 15f, 33f), 0.75f);
			RenderingUtil.drawTexture(textureMatrix, new Vector4f(52, 18, 9, 27), 0f,
					new Vec2f(80f, 64f), new Vector4f(68f, 3f, 9f, 27f), 1f);
		}
		//Arm
		IArmComponent arms = UltraComponents.ARMS.get(player);
		if(arms.getUnlockedArmCount() > 1)
		{
			RenderingUtil.drawTexture(textureMatrix, new Vector4f(49, 0, 15, 14), 0f,
					new Vec2f(80f, 64f), new Vector4f(49f, 34f, 15f, 14f), 0.75f);
			RenderingUtil.drawTexture(textureMatrix, new Vector4f(51, 2f, 11, 10), 0f,
					new Vec2f(80f, 64f), new Vector4f(47f + 11 * arms.getActiveArm(), 48f, 11f, 10f), 1f);
		}
		
		matrices.pop();
		ItemStack mainHand = player.getMainHandStack();
		boolean sprite = shouldRenderSpriteInstead(mainHand.getItem());
		if(!sprite)
			RenderSystem.restoreProjectionMatrix();
		if(config.moveUltrahud)
			matrices.translate(0f, yOffset / 3f - (config.switchSides ? 7 : 0), 0f);
		//UltraHotbar
		matrices.push();
		matrices.translate((-21 - (3 - guiScale) * 8) * (flip ? -1.5 : 1), -24 - (3 - guiScale) * 8.5f, -50);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flip ? -10f : 10f));
		matrices.scale(scale, scale, scale);
		matrices.scale(30f, 30f, 5f);
		if(flip)
			matrices.translate(-1.75, 0, 0);
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		Matrix3f normal = matrices.peek().getNormalMatrix();
		normal.set(new Quaternionf().rotateXYZ((float)Math.toRadians(cam.getPitch()), (float)Math.toRadians(cam.getYaw()), 0f));
		matrices.push();
		matrices.scale(0.5f, 0.5f, 0.5f);
		matrices.translate(1.2, -0.25, 0);
		
		RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		ItemStack stack = player.getInventory().getStack((player.getInventory().selectedSlot + 1) % 9);
		if(!sprite)
			drawItem(matrices, textureMatrix, client, immediate, stack, false); // right
		int lastSlot = (player.getInventory().selectedSlot - 1) % 9;
		stack = player.getInventory().getStack(lastSlot == -1 ? 8 : lastSlot);
		matrices.translate(-2.5f, 0f, 0f);
		//matrices.multiply(new Quaternionf(new AxisAngle4f(0.18f, 0f, 1f * (flip ? -1 : 1), 0f)));
		if(!sprite)
			drawItem(matrices, textureMatrix, client, immediate, stack, false); // left
		matrices.pop();
		matrices.translate(0f, 0f, 0.5f);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		drawItem(matrices, textureMatrix, client, immediate, mainHand, true); // middle
		
		matrices.pop();
		
		if(wingHintDisplayTimer > 0.001f)
		{
			matrices.scale(0.25f, -0.25f, -1f);
			if(config.moveUltrahud)
				matrices.translate(0f, yOffset * 1.75f - (config.switchSides ? 7 : 0), 0f);
			matrices.translate(0, 0, 10);
			Text t = Text.translatable(wingsActive ? "message.ultracraft.hi-vel.enable" : "message.ultracraft.hi-vel.disable");
			if(whitelistHintDisplayTimer > 0.001f)
				t = Text.translatable("message.ultracraft.hi-vel.whitelist");
			drawText(matrices, t,
					flip ? -150 : -50, 14, MathHelper.clamp(wingHintDisplayTimer, 0.05f, 1f), false);
			wingHintDisplayTimer -= delta / 20f;
		}
		if(whitelistHintDisplayTimer > 0.001f)
		{
			whitelistHintDisplayTimer -= delta / 20f;
		}
	}
	
	public void renderExtras(float tickDelta)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if(player == null)
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
		RenderSystem.setProjectionMatrix(matrices.peek().getPositionMatrix(), VertexSorter.BY_DISTANCE);
		RenderSystem.enableBlend();
		
		//Fishing Joke
		if(fishTimer > 0f && lastCatch != null && config.fishingJoke)
		{
			int fishCaught = UltraComponents.EASTER.get(player).getFishes();
			matrices.push();
			matrices.scale(aspect, 1f, 1f);
			matrices.push();
			matrices.scale(0.001f, -0.001f, 0.001f);
			drawText(matrices, Text.translatable("message.ultracraft.fish.caught", lastCatch.getName()),
					0, -32f, 1f, true);
			drawText(matrices, Text.translatable("message.ultracraft.fish.size", fishCaught == 69 ? "1.5" : "1"),
					0, 16f, 1f, true);
			float fishManiaLevel = Math.max(0, fishCaught - 10) / 32f;
			float shake = config.safeVFX ? 0f : 1f;
			drawText(matrices, fishCaught == 69 ? Text.translatable("message.ultracraft.fish.mania5") :
										   Text.translatable(fishMania[fishCaught % fishMania.length]),
					(rand.nextFloat() - 0.5f) * fishManiaLevel / 2f * shake,
					32f + (rand.nextFloat() - 0.5f) * fishManiaLevel / 2f * shake,
					MathHelper.clamp(0.5f * fishManiaLevel, 0.05f, 1f), true);
			matrices.scale(0.5f, 0.5f, 0.5f);
			if(fishCaught < 5)
				drawText(matrices, Text.translatable("message.ultracraft.fish.disable"),
						0, 128f, 0.5f, true);
			matrices.pop();
			matrices.push();
			matrices.translate(0f, 0f, 0.1f);
			matrices.scale(0.1f, 0.1f, 0.1f);
			matrices.multiply(new Quaternionf(new AxisAngle4f(-fishTimer / 0.75f, 0f, 1f, 0f)));
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-45.0), 0f, 0f, 1f)));
			VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
			drawItem(matrices, new Matrix4f(matrices.peek().getPositionMatrix()), client, immediate, lastCatch, false);
			immediate.draw();
			matrices.pop();
			fishTimer -= tickDelta / 20;
			matrices.pop();
		}
		
		//Coin Combo
		if(coinTimer > 0f)
		{
			matrices.push();
			matrices.scale(aspect, 1f, 1f);
			matrices.push();
			matrices.scale(0.001f, -0.001f, 0.001f);
			TextRenderer render = client.textRenderer;
			Text t;
			drawOutlinedText(matrices, t = Text.of(String.valueOf(coinCombo)), -render.getWidth(t) / 2f, 50f, 1f);
			matrices.pop();
			matrices.push();
			matrices.translate(0f, -0.725f, 1f);
			RenderSystem.setShaderColor(1f, 1f, 1f, Math.min(coinTimer, 1f));
			coinRot = MathHelper.lerp(tickDelta / 10, coinRot, coinRotDest);
			matrices.translate(0f, 0.14f, 0f);
			matrices.scale(0.25f, 0.25f, 0.25f);
			matrices.multiply(new Quaternionf(new AxisAngle4f(
					(float)(Math.toRadians(15)), 1f, 0f, 0f)));
			matrices.multiply(new Quaternionf(new AxisAngle4f(
					(float)(Math.toRadians(coinRot * 180)), 0f, 1f, 0f)));
			RenderSystem.setShaderLights(new Vector3f(0, 0, -1), new Vector3f(0, 0, -1));
			VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
			drawItem(matrices, new Matrix4f(matrices.peek().getPositionMatrix()), client, immediate, new ItemStack(ItemRegistry.COIN), false);
			immediate.draw();
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			coinTimer -= tickDelta / 20;
			matrices.pop();
			matrices.pop();
		}
		else if(coinCombo > 0)
		{
			coinCombo = 0;
			coinRot = 0;
			coinRotDest = 0;
		}
		matrices.pop();
		RenderSystem.restoreProjectionMatrix();
	}
	
	boolean shouldRenderSpriteInstead(Item item)
	{
		return (item instanceof AbstractWeaponItem weapon && weapon.getHUDTexture() != null) || item instanceof MachineSwordItem || item instanceof PlushieItem || item instanceof FlorpItem;
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
				uv = new Vector2i(MachineSwordItem.getType(stack).ordinal(), 5);
			else if (item instanceof PlushieItem)
				uv = new Vector2i(3, 0);
			else if (item instanceof FlorpItem)
				uv = new Vector2i(3, 3);
			RenderSystem.setShaderTexture(0, WEAPONS_TEXTURE);
			RenderingUtil.drawTexture(textureMatrix, new Vector4f(0, 16, 48, 32f), 0f,
					new Vec2f(192, 192), new Vector4f(uv.x * 48f, uv.y * 32f, 48f, 32f), 0.75f);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
		else
		{
			matrices.push();
			RenderSystem.disableDepthTest();
			//This HUD is *terribly* made, and entity item models don't render correctly. To hide this, I did the following :D
			if(item.equals(Items.SHIELD))
				stack = ItemRegistry.FAKE_SHIELD.getDefaultStack();
			else if(item instanceof BannerItem)
				stack = ItemRegistry.FAKE_BANNER.getDefaultStack();
			else if(item instanceof TerminalItem)
				stack = ItemRegistry.FAKE_TERMINAL.getDefaultStack();
			else if(stack.isOf(Items.CHEST) || stack.isOf(Items.TRAPPED_CHEST))
				stack = ItemRegistry.FAKE_CHEST.getDefaultStack();
			else if(stack.isOf(Items.ENDER_CHEST))
				stack = ItemRegistry.FAKE_ENDER_CHEST.getDefaultStack();
			else if(client.getItemRenderer().getModel(stack, client.world, client.player, 0).isBuiltin())
				stack = ItemRegistry.PLACEHOLDER.getDefaultStack();
			RenderSystem.disableDepthTest();
			client.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI,
					15728880, OverlayTexture.DEFAULT_UV, matrices, immediate, client.world, 1);
			RenderSystem.enableDepthTest();
			
			matrices.pop();
		}
	}
	
	void drawText(MatrixStack matrices, Text text, float x, float y, float alpha, boolean centered)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		String[] lines = text.getString().split("\n");
		for (int i = 0; i < lines.length; i++)
		{
			Matrix4f matrix = matrices.peek().getPositionMatrix();
			float x1 = x - (centered ? client.textRenderer.getWidth(lines[i]) / 2f : 0);
			float y1 = y + (client.textRenderer.fontHeight + 2) * i;
			client.textRenderer.draw(Text.of(lines[i]), x1, y1, Color.ofRGBA(1f, 1f, 1f, alpha).getColor(), false,
					matrix, immediate, TextRenderer.TextLayerType.NORMAL, Color.ofRGBA(0f, 0f, 0f, 0.5f * alpha).getColor(), 15728880);
			matrix.translate(0f, 0f, -0.1f);
			client.textRenderer.draw(Text.of(lines[i]), x1, y1, Color.ofRGBA(1f, 1f, 1f, alpha).getColor(), false,
					matrix, immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
		}
	}
	
	void drawOutlinedText(MatrixStack matrices, Text text, float x, float y, float alpha)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		client.textRenderer.draw(text, x, y, Color.ofRGBA(1f, 1f, 1f, alpha).getColor(), false,
				matrix, immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
		matrix.translate(0f, 0f, 0.5f);
		client.textRenderer.draw(text, x, y - 1, Color.ofRGBA(0f, 0f, 0f, alpha).getColor(), false,
				matrix, immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
		client.textRenderer.draw(text, x, y + 1, Color.ofRGBA(0f, 0f, 0f, alpha).getColor(), false,
				matrix, immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
		client.textRenderer.draw(text, x - 1, y, Color.ofRGBA(0f, 0f, 0f, alpha).getColor(), false,
				matrix, immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
		client.textRenderer.draw(text, x + 1, y, Color.ofRGBA(0f, 0f, 0f, alpha).getColor(), false,
				matrix, immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
	}
 
	public static void onUpdateWingsActive()
	{
		wingHintDisplayTimer = 2.5f;
	}
	
	public static void onWhitelistHint()
	{
		whitelistHintDisplayTimer = 3f;
	}
	
	public static void onCatchFish(ItemStack stack)
	{
		if(UltracraftClient.getConfig().fishingJoke)
		{
			lastCatch = stack;
			fishTimer = 10f;
			UltraComponents.EASTER.get(MinecraftClient.getInstance().player).addFish();
		}
	}
	
	public static void onPunchCoin(int score)
	{
		if(UltracraftClient.getConfig().coinPunchCounter)
		{
			coinTimer = 10f;
			coinCombo = score + 1;
			coinRotDest += 1f + 0.05f * score;
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
