package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class HellBulletRenderer extends EntityRenderer<HellBulletEntity>
{
	protected static final Identifier HELL_BULLET = new Identifier(Ultracraft.MOD_ID, "textures/item/hell_bullet.png");
	protected static final Identifier CERB_BALL = new Identifier(Ultracraft.MOD_ID, "textures/item/cerberus_ball.png");
	
	public HellBulletRenderer(EntityRendererFactory.Context context)
	{
		super(context);
	}
	
	@Override
	public void render(HellBulletEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
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
		consumer.vertex(matrix, -0.25f, -0.25f, 0f).color(255, 255, 255, 255).texture(0f, 1f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, 0.25f, -0.25f, 0f).color(255, 255, 255, 255).texture(1f, 1f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, 0.25f, 0.25f, 0f).color(255, 255, 255, 255).texture(1f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, -0.25f, 0.25f, 0f).color(255, 255, 255, 255).texture(0f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, 15728880);
	}
	
	@Override
	public Identifier getTexture(HellBulletEntity entity)
	{
		return HELL_BULLET;
	}
}
