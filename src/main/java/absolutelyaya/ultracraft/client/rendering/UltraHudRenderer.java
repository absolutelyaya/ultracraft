package absolutelyaya.ultracraft.client.rendering;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.joml.*;
import software.bernie.geckolib3.core.util.Color;

import java.lang.Math;

@SuppressWarnings("SameParameterValue")
public class UltraHudRenderer extends DrawableHelper
{
	final Identifier GUI_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/ultrahud.png");
	final Identifier WEAPONS_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/weapon_icons.png");
	float healthPercent, staminaPercent;
	
	public void render(float tickDelta, Camera cam)
	{
		if(!MinecraftClient.isHudEnabled())
			return;
		if(cam.isThirdPerson())
			return;
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if(player == null)
			return;
		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		
		MatrixStack matrices = new MatrixStack();
		matrices.peek().getPositionMatrix().perspective(90 * 0.0174f,
						(float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight(),
						0.05F, client.gameRenderer.getViewDistance() * 4.0F);
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrices.peek().getPositionMatrix());
		
		matrices.push();
		matrices.scale(0.8f, -0.5f, 0.5f);
		
		float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
		float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
		matrices.multiply(new Quaternionf().rotateX((float)Math.toRadians((player.getPitch(tickDelta) - h) * 0.15f)));
		matrices.multiply(new Quaternionf().rotateY((float)Math.toRadians((player.getYaw(tickDelta) - i) * -0.05f)));
		matrices.multiply(new Quaternionf(new AxisAngle4f(-0.6f, 0f, 1f, 0f)));
		matrices.translate(-15, 0, 150);
		
		RenderSystem.enableBlend();
		matrices.push();
		RenderSystem.enableTexture();
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		matrices.translate(1f, 35f, 0f);
		matrices.scale(1f, -1f, 1f);
		Matrix4f textureMatrix = new Matrix4f(matrices.peek().getPositionMatrix());
		drawTexture(textureMatrix, new Vector4f(-60f, -35f, 67.5f, 67.5f), 0f,
				new Vec2f(80f, 64f), new Vector4f(0f, 0f, 48f, 48f), 0.75f);
		//bars
		//health
		healthPercent = MathHelper.lerp(tickDelta, healthPercent, player.getHealth() / player.getMaxHealth());
		drawTexture(textureMatrix, new Vector4f(-60f + 2.8125f, -35f + 11.250f, 61.875f * healthPercent, 5.625f), 0f,
				new Vec2f(80f, 64f), new Vector4f(2f, 48f, 44f * healthPercent, 4f), 1f);
		//stamina
		staminaPercent = MathHelper.lerp(tickDelta, staminaPercent, ((WingedPlayerEntity)player).getStamina() / 90f);
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
		
		//UltraHotbar
		matrices.push();
		matrices.scale(30, 30, -30);
		matrices.translate(-0.025, 0.425, 0);
		matrices.multiply(new Quaternionf(new AxisAngle4f(0.12f, -1f, 1f, 0f)));
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		Matrix3f normal = matrices.peek().getNormalMatrix();
		normal.set(new Quaternionf().rotateXYZ((float)Math.toRadians(cam.getPitch()), (float)Math.toRadians(cam.getYaw()), 0f));
		matrices.push();
		matrices.scale(0.5f, 0.5f, 0.5f);
		matrices.multiply(new Quaternionf(new AxisAngle4f(-0.18f, 0f, 1f, 0f)));
		matrices.translate(1.25, -0.25, 0);
		
		ItemStack mainHand = player.getMainHandStack();
		ItemStack stack = player.getInventory().getStack((player.getInventory().selectedSlot + 1) % 9);
		if(!(mainHand.getItem() instanceof AbstractWeaponItem))
			drawItem(matrices, textureMatrix, client, immediate, stack, false);
		int lastSlot = (player.getInventory().selectedSlot - 1) % 9;
		stack = player.getInventory().getStack(lastSlot == -1 ? 8 : lastSlot);
		matrices.translate(-2.5, 0, 0);
		matrices.multiply(new Quaternionf(new AxisAngle4f(0.18f, 0f, 1f, 0f)));
		if(!(mainHand.getItem() instanceof AbstractWeaponItem))
			drawItem(matrices, textureMatrix, client, immediate, stack, false);
		matrices.pop();
		drawItem(matrices, textureMatrix, client, immediate, mainHand, true);
		matrices.pop();
		matrices.pop();
		
		int hdt = ((WingedPlayerEntity)player).getWingHintDisplayTicks();
		if(hdt > 0)
		{
			matrices.translate(5, 0, 150);
			drawText(matrices, ((WingedPlayerEntity)player).isWingsVisible() ? "message.ultracraft.hi-vel.enable" : "message.ultracraft.hi-vel.disable",
					-80, -10, Math.min(hdt / 20f, 1f));
		}
		matrices.pop();
		RenderSystem.restoreProjectionMatrix();
	}
	
	void drawItem(MatrixStack matrices, Matrix4f textureMatrix, MinecraftClient client, VertexConsumerProvider immediate, ItemStack stack, boolean hand)
	{
		if(stack.getItem() instanceof AbstractWeaponItem weapon)
		{
			if(!hand)
				return;
			Vector2i uv = weapon.getHUDTexture();
			RenderSystem.setShaderTexture(0, WEAPONS_TEXTURE);
			drawTexture(textureMatrix, new Vector4f(-60f, -35f + 22.5f, 67.5f, 45f), 0f,
					new Vec2f(96f, 96f), new Vector4f(uv.x * 48f, uv.y * 32f, 48f, 32f), 0.75f);
		}
		else
			client.getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI,
					15728880, OverlayTexture.DEFAULT_UV, matrices, immediate, 1);
	}
	
	void drawText(MatrixStack matrices, String text, float x, float y, float alpha)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		client.textRenderer.draw(Text.translatable(text), x, y, Color.ofRGBA(1f, 1f, 1f, alpha).getColor(), false,
				matrix, immediate, false, Color.ofRGBA(0f, 0f, 0f, 0.5f * alpha).getColor(), 15728880);
		matrix.translate(0f, 0f, -0.1f);
		client.textRenderer.draw(Text.translatable(text), x, y, Color.ofRGBA(1f, 1f, 1f, alpha).getColor(), false,
				matrix, immediate, false, 0, 15728880);
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
}
