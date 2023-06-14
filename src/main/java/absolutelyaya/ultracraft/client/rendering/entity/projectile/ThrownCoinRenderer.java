package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.projectile.ThrownCoinEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class ThrownCoinRenderer extends EntityRenderer<ThrownCoinEntity>
{
	final Identifier FLASH_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/particle/flash.png");
	
	public ThrownCoinRenderer(EntityRendererFactory.Context context)
	{
		super(context);
	}
	
	@Override
	public void render(ThrownCoinEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
		RenderSystem.setShaderTexture(0, getTexture(entity));
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(getTexture(entity)));
		matrices.translate(0f, 0.125f, 0f);
		Quaternionf camRot = new Quaternionf(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
		matrices.multiply(camRot.rotateY((float)Math.toRadians(180)));
		Matrix4f matrix = new Matrix4f(matrices.peek().getPositionMatrix());
		Matrix3f normalMatrix = new Matrix3f(matrices.peek().getNormalMatrix());
		int c = entity.isDeadCoined() ? 0 : 255;
		consumer.vertex(matrix, -0.1f, -0.1f, 0f).color(255, c, c, 255).texture(0f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, 0.1f, -0.1f, 0f).color(255, c, c, 255).texture(1f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, 0.1f, 0.1f, 0f).color(255, c, c, 255).texture(1f, 1f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, -0.1f, 0.1f, 0f).color(255, c, c, 255).texture(0f, 1f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		if(entity.getVelocity().length() > 0f && entity.isSplittable())
		{
			matrices.push();
			consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(FLASH_TEXTURE));
			float scale = (float)Math.max(1f - Math.abs(entity.getVelocity().y * 6.5f), 0f) * 2f;
			matrices.scale(scale, scale, scale);
			matrices.multiply(new Quaternionf(new AxisAngle4f((entity.age + tickDelta) * entity.getFlashRotSpeed(), 0f, 0f, 1f)));
			matrix = matrices.peek().getPositionMatrix();
			consumer.vertex(matrix, -0.25f, -0.25f, 0f).color(255, c, c, 255).texture(0f, 0f)
					.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
			consumer.vertex(matrix, 0.25f, -0.25f, 0f).color(255, c, c, 255).texture(1f, 0f)
					.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
			consumer.vertex(matrix, 0.25f, 0.25f, 0f).color(255, c, c, 255).texture(1f, 1f)
					.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
			consumer.vertex(matrix, -0.25f, 0.25f, 0f).color(255, c, c, 255).texture(0f, 1f)
					.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
			matrices.pop();
		}
		matrices.pop();
	}
	
	@Override
	public Identifier getTexture(ThrownCoinEntity entity)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/thrown_coin.png");
	}
}
