package absolutelyaya.ultracraft.client.rendering;

import absolutelyaya.ultracraft.Ultracraft;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class TitleLimboBGRenderer extends RotatingCubeMapRenderer
{
	final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/misc/limbo.png");
	
	private final MinecraftClient client;
	float time;
	
	public TitleLimboBGRenderer(CubeMapRenderer cubeMap)
	{
		super(cubeMap);
		this.client = MinecraftClient.getInstance();
	}
	
	@Override
	public void render(float delta, float alpha)
	{
		time += delta;
		RenderSystem.clearDepth(1);
		drawBG(this.client, time / - (5 * 6));
	}
	
	void drawBG(MinecraftClient client, float v)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		Matrix4f matrix4f = new Matrix4f().perspective(85f * 0.0174f, (float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight(), 0.05F, 10.0F);
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrix4f, RenderSystem.getVertexSorting());
		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		matrixStack.loadIdentity();
		matrixStack.translate(0f, 0.05f, -1f);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, TEXTURE);
		
		//Black / Clear Screen
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(10f, 10f, -1f).color(0, 0, 0, 255).light(0xffffffff).next();
		bufferBuilder.vertex(10f, -10f, -1f).color(0, 0, 0, 255).light(0xffffffff).next();
		bufferBuilder.vertex(-10f, -10f, -1f).color(0, 0, 0, 255).light(0xffffffff).next();
		bufferBuilder.vertex(-10f, 10f, -1f).color(0, 0, 0, 255).light(0xffffffff).next();
		tessellator.draw();
		//Lava
		RenderSystem.setShaderTexture(0, new Identifier("textures/block/lava_still.png"));
		for (int i = 0; i < 16; i++)
		{
			matrixStack.push();
			float z = -2 - v % 1f - (i);
			matrixStack.translate(0, 0, z - 0.5);
			RenderSystem.applyModelViewMatrix();
			int l = Math.round(255.0F * MathHelper.clamp(2f + z / 4f, 0f, 1f));
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(10f, -4f, 0.5f).texture(0f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-10f, -4f, 0.5f).texture(20f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-10f, -4f, -0.5f).texture(20f, 1f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(10f, -4f, -0.5f).texture(0f, 1f).color(l, l, l, 255).light(0xffffffff).next();
			tessellator.draw();
			matrixStack.pop();
		}
		//Ceiling
		RenderSystem.setShaderTexture(0, new Identifier("textures/block/stone.png"));
		for (int i = 0; i < 16; i++)
		{
			matrixStack.push();
			float z = -2 - v % 1f - (i);
			matrixStack.translate(0, 0, z - 0.5);
			RenderSystem.applyModelViewMatrix();
			int l = Math.round(255.0F * MathHelper.clamp(2f + z / 4f, 0f, 1f) / 4f);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(10f, 4f, 0.5f).texture(0f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-10f, 4f, 0.5f).texture(20f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-10f, 4f, -0.5f).texture(20f, 1f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(10f, 4f, -0.5f).texture(0f, 1f).color(l, l, l, 255).light(0xffffffff).next();
			tessellator.draw();
			matrixStack.pop();
		}
		//Walls
		RenderSystem.setShaderTexture(0, TEXTURE);
		for (int i = 0; i < 12; i++)
		{
			matrixStack.push();
			float z = -2 - v % 1f - (i);
			matrixStack.translate(0, 0, z - 0.5);
			RenderSystem.applyModelViewMatrix();
			int l = Math.round(255.0F * MathHelper.clamp(2f + z / 4f, 0f, 1f));
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(6.001f, 4f, 0.5f).texture(0.5f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(6.001f, -4f, 0.5f).texture(0.5f, 1f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(6.001f, -4f, -0.5f).texture(0.625f, 1f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(6.001f, 4f, -0.5f).texture(0.625f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			
			bufferBuilder.vertex(-6.001f, 4f, 0.5f).texture(0.625f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-6.001f, -4f, 0.5f).texture(0.625f, 1f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-6.001f, -4f, -0.5f).texture(0.5f, 1f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-6.001f, 4f, -0.5f).texture(0.5f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			tessellator.draw();
			matrixStack.pop();
		}
		//Windows
		for (int i = 0; i < 2; i++)
		{
			matrixStack.push();
			float z = -2 - v % 6f - (i * 6);
			matrixStack.translate(0, 0.5, z);
			RenderSystem.applyModelViewMatrix();
			int l = Math.round(255.0F * MathHelper.clamp(4f + z / 2f, 0f, 1f));
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(6f, 1.5f, 1f).texture(0.5f, 0.125f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(6f, -1.5f, 1f).texture(0.5f, 0.5f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(6f, -1.5f, -1f).texture(0.25f, 0.5f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(6f, 1.5f, -1f).texture(0.25f, 0.125f).color(l, l, l, 255).light(0xffffffff).next();
			
			bufferBuilder.vertex(-6f, 1.5f, 1f).texture(0.5f, 0.125f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-6f, -1.5f, 1f).texture(0.5f, 0.5f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-6f, -1.5f, -1f).texture(0.25f, 0.5f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-6f, 1.5f, -1f).texture(0.25f, 0.125f).color(l, l, l, 255).light(0xffffffff).next();
			tessellator.draw();
			matrixStack.pop();
		}
		//Smoke
		for (int i = 0; i < 12; i++)
		{
			matrixStack.push();
			float z = - v % 1f - (i);
			matrixStack.translate(0, 0, z - 0.5);
			RenderSystem.applyModelViewMatrix();
			int l = Math.round(80f * MathHelper.clamp(1.5f + z / 8f, 0f, 1f));
			for (int ii = 0; ii < 3; ii++)
			{
				float f = ii * 0.66f;
				bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
				bufferBuilder.vertex(2f + f, 4f, 0.5f).texture(0.625f, 0f + v * -0.1f + f).color(255, 255, 255, l).light(0xffffffff).next();
				bufferBuilder.vertex(2f + f, -4f, 0.5f).texture(0.625f, 1f + v * -0.1f + f).color(255, 255, 255, l).light(0xffffffff).next();
				bufferBuilder.vertex(2f + f, -4f, -0.5f).texture(0.75f, 1f + v * -0.1f + f).color(255, 255, 255, l).light(0xffffffff).next();
				bufferBuilder.vertex(2f + f, 4f, -0.5f).texture(0.75f, 0f + v * -0.1f + f).color(255, 255, 255, l).light(0xffffffff).next();
				
				bufferBuilder.vertex(-2f - f, 4f, 0.5f).texture(0.75f, 0f + v * -0.1f + f).color(255, 255, 255, l).light(0xffffffff).next();
				bufferBuilder.vertex(-2f - f, -4f, 0.5f).texture(0.75f, 1f + v * -0.1f + f).color(255, 255, 255, l).light(0xffffffff).next();
				bufferBuilder.vertex(-2f - f, -4f, -0.5f).texture(0.625f, 1f + v * -0.1f + f).color(255, 255, 255, l).light(0xffffffff).next();
				bufferBuilder.vertex(-2f - f, 4f, -0.5f).texture(0.625f, 0f + v * -0.1f + f).color(255, 255, 255, l).light(0xffffffff).next();
				tessellator.draw();
			}
			matrixStack.pop();
		}
		//Embers
		for (int i = 0; i < 3; i++)
		{
			matrixStack.push();
			float z = - v % 5f - (i * 5f);
			matrixStack.translate(0, 0, z - 0.5);
			for (int ii = 0; ii < 4; ii++)
			{
				boolean alt = ii % 2 == 0;
				float b = alt ? 0.33f + (float)Math.sin(v * 3f) * 0.05f : 0f + (float)Math.cos(v * 3f) * 0.05f;
				float uvOffset = v * (alt ? -0.23f : -0.146f) + ii * 0.25f;
				RenderSystem.applyModelViewMatrix();
				bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
				bufferBuilder.vertex(2.5f + b, 4f, 0.5f).texture(0.75f, 0f + uvOffset).color(255, 255, 255, 255).light(0xffffffff).next();
				bufferBuilder.vertex(2.5f + b, -4f, 0.5f).texture(0.75f, 1f + uvOffset).color(255, 255, 255, 255).light(0xffffffff).next();
				bufferBuilder.vertex(2.5f + b, -4f, -0.5f).texture(0.875f, 1f + uvOffset).color(255, 255, 255, 255).light(0xffffffff).next();
				bufferBuilder.vertex(2.5f + b, 4f, -0.5f).texture(0.875f, 0f + uvOffset).color(255, 255, 255, 255).light(0xffffffff).next();
				
				bufferBuilder.vertex(-2.5f - b, 4f, 0.5f).texture(0.875f, 0f + uvOffset).color(255, 255, 255, 255).light(0xffffffff).next();
				bufferBuilder.vertex(-2.5f - b, -4f, 0.5f).texture(0.875f, 1f + uvOffset).color(255, 255, 255, 255).light(0xffffffff).next();
				bufferBuilder.vertex(-2.5f - b, -4f, -0.5f).texture(0.75f, 1f + uvOffset).color(255, 255, 255, 255).light(0xffffffff).next();
				bufferBuilder.vertex(-2.5f - b, 4f, -0.5f).texture(0.75f, 0f + uvOffset).color(255, 255, 255, 255).light(0xffffffff).next();
				tessellator.draw();
				matrixStack.translate(0, 0, -1f);
			}
			matrixStack.pop();
		}
		//Walkway
		for (int i = 0; i < 8; i++)
		{
			matrixStack.push();
			float z = -v % 2f - (i * 2);
			matrixStack.translate(0, 0, z);
			RenderSystem.applyModelViewMatrix();
			int l = Math.round(255.0F * MathHelper.clamp(2f + z / 4f, 0f, 1f));
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(1f, -1.75f, 1f).texture(0f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-1f, -1.75f, 1f).texture(0.25f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-1f, -1.75f, -1f).texture(0.25f, 0.25f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(1f, -1.75f, -1f).texture(0f, 0.25f).color(l, l, l, 255).light(0xffffffff).next();
			
			bufferBuilder.vertex(1f, -0.75f, 1f).texture(0.25f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(1f, -1.75f, 1f).texture(0.25f, 0.125f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(1f, -1.75f, -1f).texture(0.5f, 0.125f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(1f, -0.75f, -1f).texture(0.5f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			
			bufferBuilder.vertex(-1f, -0.75f, 1f).texture(0.25f, 0f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-1f, -1.75f, 1f).texture(0.25f, 0.125f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-1f, -1.75f, -1f).texture(0.5f, 0.125f).color(l, l, l, 255).light(0xffffffff).next();
			bufferBuilder.vertex(-1f, -0.75f, -1f).texture(0.5f, 0f).color(l, l, l, 255).light(0xffffffff).next();
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
