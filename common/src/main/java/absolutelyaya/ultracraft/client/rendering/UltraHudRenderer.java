package absolutelyaya.ultracraft.client.rendering;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import software.bernie.geckolib3.core.util.Color;

@SuppressWarnings("SameParameterValue")
public class UltraHudRenderer extends DrawableHelper
{
	final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/ultrahud.png");
	float healthPercent, staminaPercent;
	
	public void render(float tickDelta, Camera cam)
	{
		if(cam.isThirdPerson())
			return;
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if(player == null)
			return;
		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		
		MatrixStack matrices = new MatrixStack();
		matrices.peek().getPositionMatrix().multiply(
				Matrix4f.viewboxMatrix(client.options.getFov().getValue(),
						(float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight(),
						0.05F, client.gameRenderer.getViewDistance() * 4.0F));
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrices.peek().getPositionMatrix());
		
		matrices.push();
		matrices.scale(0.8f, -0.5f, 0.5f);
		
		float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
		float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
		matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion((player.getPitch(tickDelta) - h) * 0.15f));
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((player.getYaw(tickDelta) - i) * -0.05f));
		matrices.multiply(Quaternion.fromEulerXyz(0f, -0.6f, 0f));
		matrices.translate(-15, 0, 150);
		
		RenderSystem.enableBlend();
		matrices.push();
		RenderSystem.enableTexture();
		RenderSystem.setShaderTexture(0, TEXTURE);
		matrices.translate(1f, 35f, 0f);
		matrices.scale(1f, -1f, 1f);
		drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(-60f, -35f, 67.5f, 67.5f), 0f,
				new Vec2f(80f, 64f), new Vector4f(0f, 0f, 48f, 48f), 0.75f);
		//bars
		//health
		healthPercent = MathHelper.lerp(tickDelta, healthPercent, player.getHealth() / player.getMaxHealth());
		drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(-60f + 2.8125f, -35f + 11.250f, 61.875f * healthPercent, 5.625f), 0f,
				new Vec2f(80f, 64f), new Vector4f(2f, 48f, 44f * healthPercent, 4f), 1f);
		//stamina
		staminaPercent = MathHelper.lerp(tickDelta, staminaPercent, ((WingedPlayerEntity)player).getStamina() / 90f);
		drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(-60f + 2.8125f, -35f + 2.8125f, 61.875f * staminaPercent, 5.625f), 0f,
				new Vec2f(80f, 64f), new Vector4f(2f, 52f, 44f * staminaPercent, 4f), 1f);
		//Railgun
		if(false) //if hasRailgun
		{
			drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(-60f + 68.90625f, -35 + 21.09f, 21.09f, 46.4062f), 0f,
					new Vec2f(80f, 64f), new Vector4f(49f, 0f, 15f, 33f), 0.75f);
		}
		//Fist
		if(false) //if hasFist
		{
			drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(-60f + 68.90625f, -35f, 21.09f, 19.68f), 0f,
					new Vec2f(80f, 64f), new Vector4f(49f, 34f, 15f, 14f), 0.75f);
		}
		
		//TODO: figure out why some items render red ???
		//UltraHotbar
		matrices.push();
		matrices.scale(30, 30, -30);
		matrices.translate(-0.025, 0.425, 0);
		matrices.multiply(Quaternion.fromEulerXyz(-0.12f, 0.12f, 0f));
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		//client.getItemRenderer().renderGuiItemIcon(client.player.getMainHandStack(), 0, 0);
		Matrix3f normal = matrices.peek().getNormalMatrix();
		normal.load(new Matrix3f(Quaternion.fromEulerXyzDegrees(new Vec3f(cam.getPitch(), cam.getYaw(), 0))));
		//System.out.println(cam.getYaw());
		matrices.push();
		matrices.scale(0.5f, 0.5f, 0.5f);
		matrices.multiply(Quaternion.fromEulerXyz(0f, -0.18f, 0f));
		matrices.translate(1.25, -0.25, 0);
		client.getItemRenderer().renderItem(player.getInventory().getStack(
						(player.getInventory().selectedSlot + 1) % 9), ModelTransformation.Mode.GUI,
				15728880, 0, matrices, immediate, 1);
		int lastSlot = (player.getInventory().selectedSlot - 1) % 9;
		matrices.translate(-2.5, 0, 0);
		matrices.multiply(Quaternion.fromEulerXyz(0f, 0.18f, 0f));
		client.getItemRenderer().renderItem(player.getInventory().getStack(lastSlot == -1 ? 8 : lastSlot), ModelTransformation.Mode.GUI,
				15728880, 0, matrices, immediate, 1);
		matrices.pop();
		client.getItemRenderer().renderItem(player.getMainHandStack(), ModelTransformation.Mode.GUI,
				15728880, 0, matrices, immediate, 1);
		matrices.pop();
		matrices.pop();
		int hdt = ((WingedPlayerEntity)player).getWingHintDisplayTicks();
		if(hdt > 0)
		{
			matrices.translate(5, 0, 150);
			drawText(matrices, ((WingedPlayerEntity)player).isWingsVisible() ? "High Velocity Wings activated!" : "High Velocity Wings deactivated!",
					-80, -10, Math.min(hdt / 20f, 1f));
		}
		matrices.pop();
		RenderSystem.restoreProjectionMatrix();
	}
	
	void drawText(MatrixStack matrices, String text, float x, float y, float alpha)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		client.textRenderer.draw(Text.of(text), x, y, Color.ofRGBA(1f, 1f, 1f, alpha).getColor(), false,
				matrix, immediate, false, Color.ofRGBA(0f, 0f, 0f, 0.5f * alpha).getColor(), 15728880);
		matrix.addToLastColumn(new Vec3f(0f, 0f, 0.06f));
		client.textRenderer.draw(Text.of(text), x, y, Color.ofRGBA(1f, 1f, 1f, alpha).getColor(), false,
				matrix, immediate, false, 0, 15728880);
	}
	
	void drawTexture(Matrix4f matrix, Vector4f transform, float z, Vec2f textureSize, Vector4f uv, float alpha)
	{
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		uv = new Vector4f(uv.getX() / textureSize.x, uv.getY() / textureSize.y,
				uv.getZ() / textureSize.x, uv.getW() / textureSize.y);
		bufferBuilder.vertex(matrix, transform.getX(), transform.getY() + transform.getW(), z)
				.texture(uv.getX(), uv.getY()).next();
		bufferBuilder.vertex(matrix, transform.getX() + transform.getZ(), transform.getY() + transform.getW(), z)
				.texture(uv.getX() + uv.getZ(), uv.getY()).next();
		bufferBuilder.vertex(matrix, transform.getX() + transform.getZ(), transform.getY(), z)
				.texture(uv.getX() + uv.getZ(), uv.getY() + uv.getW()).next();
		bufferBuilder.vertex(matrix, transform.getX(), transform.getY(), z)
				.texture(uv.getX(), uv.getY() + uv.getW()).next();
		BufferRenderer.drawWithShader(bufferBuilder.end());
		BufferRenderer.resetCurrentVertexBuffer();
	}
}
