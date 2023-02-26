package absolutelyaya.ultracraft.client.rendering;

import absolutelyaya.ultracraft.Ultracraft;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

@SuppressWarnings("SameParameterValue")
public class UltraHudRenderer extends DrawableHelper
{
	final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/ultrahud.png");
	
	public void render(MatrixStack matrices, float tickDelta, Camera cam)
	{
		if(cam.isThirdPerson())
			return;
		MinecraftClient client = MinecraftClient.getInstance();
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		
		matrices = new MatrixStack();
		matrices.peek().getPositionMatrix().multiply(
				Matrix4f.viewboxMatrix(client.options.getFov().getValue(),
						(float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight(),
						0.05F, client.gameRenderer.getViewDistance() * 4.0F));
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrices.peek().getPositionMatrix());
		
		matrices.push();
		//matrices.multiply(cam.getRotation());
		matrices.scale(0.8f, -0.5f, 0.5f);
		matrices.multiply(Quaternion.fromEulerXyz(0f, -0.6f, 0f));
		matrices.translate(-15, 0, 150);
		
		
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		matrices.push();
		RenderSystem.enableTexture();
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 0.6f);
		matrices.translate(1f, 0f, 0f);
		this.drawTexture(matrices.peek().getPositionMatrix(), -60f, 30f, -30f, 60f, 0f);
		RenderSystem.setShaderColor(1f, 1f, 1f, 0.75f);
		
		matrices.push();
		matrices.scale(30, -30, -30);
		matrices.translate(-0.35, 0.175, 0);
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		client.getItemRenderer().renderItem(client.player.getMainHandStack(), ModelTransformation.Mode.GUI,
				0xffffff, 0, matrices, immediate, 1);
		matrices.pop();
		matrices.pop();
		
		matrices.translate(5, 0, 150);
		drawText(matrices, "High Velocity Wings activated!", -80, 70);
		matrices.pop();
		RenderSystem.restoreProjectionMatrix();
	}
	
	void drawText(MatrixStack matrices, String text, float x, float y)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		client.textRenderer.draw(Text.of(text), x, y, 0xffffff, false,
				matrix, immediate, false, 0x8f000000, 15728880);
		matrix.addToLastColumn(new Vec3f(0f, 0f, 0.06f));
		client.textRenderer.draw(Text.of(text), x, y, 0xffffff, false,
				matrix, immediate, false, 0, 15728880);
	}
	
	void drawTexture(Matrix4f matrix, float x0, float x1, float y0, float y1, float z)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(matrix, x0, y1, z).texture(0, 1).next();
		bufferBuilder.vertex(matrix, x1, y1, z).texture(1, 1).next();
		bufferBuilder.vertex(matrix, x1, y0, z).texture(1, 0).next();
		bufferBuilder.vertex(matrix, x0, y0, z).texture(0, 0).next();
		BufferRenderer.drawWithShader(bufferBuilder.end());
		BufferRenderer.resetCurrentVertexBuffer();
	}
}
