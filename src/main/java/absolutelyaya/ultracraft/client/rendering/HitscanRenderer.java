package absolutelyaya.ultracraft.client.rendering;

import absolutelyaya.ultracraft.client.ClientHitscanHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.*;

public class HitscanRenderer
{
	public static void render(ClientHitscanHandler.Hitscan hitscan, MatrixStack matrices, Camera camera, float delta)
	{
		Color col = hitscan.getColor();
		Vec3d camPos = camera.getPos();
		Vec3d from = hitscan.getFrom(delta);
		Vec3d to = hitscan.getTo(delta);
		float girth = Math.max(hitscan.getGirth(), 0f);
		
		renderRay(matrices, from, to, camPos, girth, col,
				(hitscan instanceof ClientHitscanHandler.MovingHitscan ? RenderLayer.getGui() : RenderLayer.getLightning()), hitscan.getLayers());
	}
	
	public static void renderRay(MatrixStack matrices, Vec3d from, Vec3d to, Vec3d camPos, float girth, Color col, RenderLayer layer, int steps)
	{
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		Vec3d dir = to.subtract(from).normalize();
		float dist = (float)from.distanceTo(to);
		float rot = (float)(-Math.atan2(dir.z, dir.x) - Math.toRadians(90));
		matrices.push();
		matrices.translate(from.x, from.y, from.z);
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);
		
		//matrixStack.multiply(Quaternion.fromEulerXyz((float)(Math.atan2(-dir.y, Math.abs(dir.z))), rot, 0f));
		double dx = dir.x;
		double dy = dir.y;
		double dz = dir.z;
		float f = MathHelper.sqrt((float)(dx * dx + dz * dz));
		matrices.multiply(new Quaternionf(new AxisAngle4f(rot, 0f, 1f, 0f)));
		matrices.multiply(new Quaternionf(new AxisAngle4f((float)(-Math.atan2(f, dy) - Math.toRadians(90)), 1f, 0f, 0f)));
		VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();
		VertexConsumer consumer = immediate.getBuffer(layer);
		
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		for (int i = 1; i <= steps; i++)
		{
			float g = girth / i;
			consumer.vertex(matrix, -g / 2, -g / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -g / 2, g / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -g / 2, g / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -g / 2, -g / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			
			consumer.vertex(matrix, g / 2, -g / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, g / 2, -g / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -g / 2, -g / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -g / 2, -g / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			
			consumer.vertex(matrix, g / 2, -g / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, g / 2, g / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, g / 2, g / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, g / 2, -g / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			
			consumer.vertex(matrix, -g / 2, g / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -g / 2, g / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, g / 2, g / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, g / 2, g / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
		}
		matrices.pop();
	}
}
