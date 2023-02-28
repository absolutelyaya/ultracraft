package absolutelyaya.ultracraft.client.rendering;

import absolutelyaya.ultracraft.Ultracraft;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class TitleBGRenderer extends RotatingCubeMapRenderer
{
	private final MinecraftClient client;
	float time;
	
	public TitleBGRenderer(CubeMapRenderer cubeMap)
	{
		super(cubeMap);
		this.client = MinecraftClient.getInstance();
	}
	
	@Override
	public void render(float delta, float alpha)
	{
		time += delta;
		RenderSystem.clearDepth(1);
		drawBG(this.client, alpha, time / - (5 * 6));
	}
	
	void drawBG(MinecraftClient client, float alpha, float v)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		Matrix4f matrix4f = new Matrix4f().perspective(85f * 0.0174f, (float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight(), 0.05F, 10.0F);
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrix4f);
		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		matrixStack.loadIdentity();
		matrixStack.scale(1f, 1.5f, 1f);
		matrixStack.translate(0f, 0f, -1f);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.setShaderColor(0.8f, 0.8f, 0.8f, 1f);
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, new Identifier(Ultracraft.MOD_ID, "textures/misc/title_bg.png"));
		
		for (int i = 0; i < 3; i++)
		{
			matrixStack.push();
			matrixStack.translate(0, (i - 1) * 2, 0);
			RenderSystem.applyModelViewMatrix();
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.light(0xffffffff);
			int l = Math.round(255.0F * alpha);
			float s = 1f / 8f;
			bufferBuilder.vertex(1.0, -1.0, 1.0).texture(0.0F, 0.0F + v + (i * s)).color(255, 255, 255, l).next();
			bufferBuilder.vertex(1.0, 1.0, 1.0).texture(0.0F, s + v + (i * s)).color(255, 255, 255, l).next();
			bufferBuilder.vertex(1.0, 1.0, -1.0).texture(1.0F, s + v + (i * s)).color(255, 255, 255, l).next();
			bufferBuilder.vertex(1.0, -1.0, -1.0).texture(1.0F, 0.0F + v + (i * s)).color(255, 255, 255, l).next();
			
			bufferBuilder.vertex(1.0, -1.0, -1.0).texture(0.0F, 0.0F + v + (i * s)).color(255, 255, 255, l).next();
			bufferBuilder.vertex(1.0, 1.0, -1.0).texture(0.0F, s + v + (i * s)).color(255, 255, 255, l).next();
			bufferBuilder.vertex(-1.0, 1.0, -1.0).texture(1.0F, s + v + (i * s)).color(255, 255, 255, l).next();
			bufferBuilder.vertex(-1.0, -1.0, -1.0).texture(1.0F, 0.0F + v + (i * s)).color(255, 255, 255, l).next();
			
			bufferBuilder.vertex(-1.0, -1.0, -1.0).texture(0.0F, 0.0F + v + (i * s)).color(255, 255, 255, l).next();
			bufferBuilder.vertex(-1.0, 1.0, -1.0).texture(0.0F, s + v + (i * s)).color(255, 255, 255, l).next();
			bufferBuilder.vertex(-1.0, 1.0, 1.0).texture(1.0F, s + v + (i * s)).color(255, 255, 255, l).next();
			bufferBuilder.vertex(-1.0, -1.0, 1.0).texture(1.0F, 0.0F + v + (i * s)).color(255, 255, 255, l).next();
			tessellator.draw();
			matrixStack.pop();
		}
		matrixStack.pop();
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.restoreProjectionMatrix();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
	}
}
