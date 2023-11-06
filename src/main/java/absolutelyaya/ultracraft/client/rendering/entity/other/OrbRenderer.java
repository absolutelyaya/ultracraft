package absolutelyaya.ultracraft.client.rendering.entity.other;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.other.AbstractOrbEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class OrbRenderer extends EntityRenderer<AbstractOrbEntity>
{
	public OrbRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx);
	}
	
	@Override
	public void render(AbstractOrbEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		if(entity.isInvisible())
			return;
		matrices.push();
		RenderSystem.setShaderTexture(0, getTexture(entity));
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		Matrix3f normalMatrix = new Matrix3f(matrices.peek().getNormalMatrix());
		VertexConsumer consumer = vertexConsumers.getBuffer(
				RenderLayer.getEntityTranslucentEmissive(new Identifier(Ultracraft.MOD_ID, "textures/entity/biglight.png")));
		matrices.translate(0f, 0.5f, 0f);
		Quaternionf camRot = new Quaternionf(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
		matrices.multiply(camRot.rotateY((float)Math.toRadians(180)));
		Matrix4f matrix = new Matrix4f(matrices.peek().getPositionMatrix());
		Vec3i c = entity.getGlowColor();
		consumer.vertex(matrix, -1.5f, -1.5f, 0f).color(c.getX(), c.getY(), c.getZ(), 255).texture(0f, 1f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, 1.5f, -1.5f, 0f).color(c.getX(), c.getY(), c.getZ(), 255).texture(1f, 1f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, 1.5f, 1.5f, 0f).color(c.getX(), c.getY(), c.getZ(), 255).texture(1f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, -1.5f, 1.5f, 0f).color(c.getX(), c.getY(), c.getZ(), 255).texture(0f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(getTexture(entity)));
		matrices.translate(0, 0, 0);
		consumer.vertex(matrix, -0.5f, -0.5f, 0.01f).color(255, 255, 255, 255).texture(0f, 1f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, 0.5f, -0.5f, 0.01f).color(255, 255, 255, 255).texture(1f, 1f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, 0.5f, 0.5f, 0.01f).color(255, 255, 255, 255).texture(1f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, -0.5f, 0.5f, 0.01f).color(255, 255, 255, 255).texture(0f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		matrices.pop();
	}
	
	@Override
	public Identifier getTexture(AbstractOrbEntity entity)
	{
		return entity.getTexture();
	}
}
