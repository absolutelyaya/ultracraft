package absolutelyaya.ultracraft.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec2f;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class RenderingUtil
{
	public static void drawTexture(Matrix4f matrix, Vector4f transform, Vec2f textureSize, Vector4f uv)
	{
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		uv = new Vector4f(uv.x() / textureSize.x, uv.y() / textureSize.y,
				uv.z() / textureSize.x, uv.w() / textureSize.y);
		bufferBuilder.vertex(matrix, transform.x(), transform.y() + transform.w(), 0)
				.texture(uv.x(), uv.y()).next();
		bufferBuilder.vertex(matrix, transform.x() + transform.z(), transform.y() + transform.w(), 0)
				.texture(uv.x() + uv.z(), uv.y()).next();
		bufferBuilder.vertex(matrix, transform.x() + transform.z(), transform.y(), 0)
				.texture(uv.x() + uv.z(), uv.y() + uv.w()).next();
		bufferBuilder.vertex(matrix, transform.x(), transform.y(), 0)
				.texture(uv.x(), uv.y() + uv.w()).next();
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		BufferRenderer.resetCurrentVertexBuffer();
	}
	
	public static void drawTexture(Matrix4f matrix, Vector4f transform, float z, Vec2f textureSize, Vector4f uv, float alpha)
	{
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
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
