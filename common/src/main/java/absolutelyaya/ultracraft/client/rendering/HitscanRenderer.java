package absolutelyaya.ultracraft.client.rendering;

import absolutelyaya.ultracraft.client.HitscanHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;

import java.awt.*;

public class HitscanRenderer
{
	public void render(HitscanHandler.Hitscan hitscan, MatrixStack matrixStack, Camera camera)
	{
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		Color col = new Color(hitscan.type.color);
		matrixStack.push();
		Vec3d camPos = camera.getPos();
		Vec3d from = hitscan.from;
		Vec3d to = hitscan.to;
		Vec3d dir = to.subtract(from).normalize();
		float dist = (float)from.distanceTo(to);
		float rot = (float)(-Math.atan2(dir.z, dir.x) - Math.toRadians(90));
		matrixStack.translate(from.x, from.y, from.z);
		matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);
		
		//matrixStack.multiply(Quaternion.fromEulerXyz((float)(Math.atan2(-dir.y, Math.abs(dir.z))), rot, 0f));
		double dx = dir.x;
		double dy = dir.y;
		double dz = dir.z;
		float f = MathHelper.sqrt((float)(dx * dx + dz * dz));
		matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(rot));
		matrixStack.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float)(-Math.atan2(f, dy) - Math.toRadians(90))));
		VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();
		VertexConsumer consumer = immediate.getBuffer(RenderLayer.getLightning());
		
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		for (int i = 1; i <= 3; i++)
		{
			float girth = Math.max(hitscan.getGirth() / i, 0f);
			consumer.vertex(matrix, -girth / 2, -girth / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -girth / 2, girth / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -girth / 2, girth / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -girth / 2, -girth / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			
			consumer.vertex(matrix, girth / 2, -girth / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, girth / 2, -girth / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -girth / 2, -girth / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -girth / 2, -girth / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			
			consumer.vertex(matrix, girth / 2, -girth / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, girth / 2, girth / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, girth / 2, girth / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, girth / 2, -girth / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			
			consumer.vertex(matrix, -girth / 2, girth / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, -girth / 2, girth / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, girth / 2, girth / 2, dist).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
			consumer.vertex(matrix, girth / 2, girth / 2, -0.0f).color(col.getRed(), col.getGreen(), col.getBlue(), 255).next();
		}
		matrixStack.pop();
	}
}
