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

public class UltraHudRenderer extends DrawableHelper
{
	final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/item/hell_bullet.png");
	
	public void render(MatrixStack matrices, float tickDelta, Camera cam)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		RenderSystem.disableDepthTest();
		//RenderSystem.depthFunc(0);
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		
		matrices.push();
		matrices.multiply(cam.getRotation());
		matrices.scale(-0.02f, -0.02f, 0.02f);
		matrices.multiply(Quaternion.fromEulerXyz(0f, -0.6f, 0f));
		matrices.translate(-5, 0, 150);
		
		
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		matrices.push();
		RenderSystem.enableTexture();
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		matrices.translate(1f, 0f, 0f);
		this.drawTexture(matrices.peek().getPositionMatrix(), -30f, 30f, -30f, 30f, 0f);
		
		matrices.push();
		matrices.scale(-30, -30, 30);
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		client.getItemRenderer().renderItem(client.player.getMainHandStack(), ModelTransformation.Mode.GUI,
				0xffffff, 0, matrices, immediate, 1);
		matrices.pop();
		matrices.pop();
		
		matrices.translate(5, 0, 150);
		drawText(matrix4f, "High Velocity Wings activated!", -25, 100);
		matrices.pop();
	}
	
	void drawText(Matrix4f matrix4f, String text, float x, float y)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
		//client.textRenderer.draw(Text.of(text), x + 0.525f, y + 1.05f, 0x1f1f1f, false,
		//		matrix4f, immediate, false, 0, 15728880);
		//matrix4f.addToLastColumn(new Vec3f(0f, 0f, 0.01f));
		client.textRenderer.draw(Text.of(text), x, y, 0xffffff, false,
				matrix4f, immediate, false, 0x8f000000, 15728880);
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
	}
}
