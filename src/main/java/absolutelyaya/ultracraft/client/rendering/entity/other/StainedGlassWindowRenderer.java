package absolutelyaya.ultracraft.client.rendering.entity.other;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.other.StainedGlassWindow;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class StainedGlassWindowRenderer extends EntityRenderer<StainedGlassWindow>
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/entity/stained_glass/bird.png");
	static final Identifier BACK_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/entity/stained_glass/hologram.png");
	
	public StainedGlassWindowRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx);
	}
	
	@Override
	public Identifier getTexture(StainedGlassWindow entity)
	{
		return TEXTURE;
	}
	
	@Override
	public void render(StainedGlassWindow entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
		matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-yaw), 0, 1, 0)));
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(getTexture(entity)));
		Matrix4f matrix = new Matrix4f(matrices.peek().getPositionMatrix());
		Matrix3f normalMatrix = new Matrix3f(matrices.peek().getNormalMatrix());
		float halfwidth = entity.getWidthPixels() / 16f / 2f, halfheight = entity.getHeightPixels() / 16f / 2f;
		consumer.vertex(matrix, -halfwidth, -halfheight, -0.025f).color(255, 255, 255, 255).texture(0f, 1f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, halfwidth, -halfheight, -0.025f).color(255, 255, 255, 255).texture(1f, 1f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, halfwidth, halfheight, -0.025f).color(255, 255, 255, 255).texture(1f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, -halfwidth, halfheight, -0.025f).color(255, 255, 255, 255).texture(0f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		
		consumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(BACK_TEXTURE));
		consumer.vertex(matrix, halfwidth, -halfheight, -0.03f).color(255, 255, 255, 255).texture(halfwidth * 2f, halfheight * 2f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, -halfwidth, -halfheight, -0.03f).color(255, 255, 255, 255).texture(0f, halfheight * 2f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, -halfwidth, halfheight, -0.03f).color(255, 255, 255, 255).texture(0f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, halfwidth, halfheight, -0.03f).color(255, 255, 255, 255).texture(halfwidth * 2f, 0f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}
}
