package absolutelyaya.ultracraft.client.rendering.entity.other;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.entity.other.ShockwaveEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class ShockwaveRenderer extends EntityRenderer<ShockwaveEntity>
{
	public ShockwaveRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx);
	}
	
	@Override
	public Identifier getTexture(ShockwaveEntity entity)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/particle/generic_stripe.png");
	}
	
	@Override
	public void render(ShockwaveEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
		float f = Math.min(1f - (float)entity.age / entity.getDuration() + 0.3f, 1f);
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayers.getShockWave(getTexture(entity)));
		float radius = entity.getRadius();
		float innerRadius = radius - radius * 0.8f;
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		//cardinal faces
		//top
		//north
		consumer.vertex(matrix, -radius / 2.5f, 0f, -radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, -radius / 3f, 0.5f, -radius + innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, radius / 3f, 0.5f, -radius + innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, radius / 2.5f, 0f, -radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//south
		consumer.vertex(matrix, -radius / 2.5f, 0f, radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, -radius / 3f, 0.5f, radius - innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, radius / 3f, 0.5f, radius - innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, radius / 2.5f, 0f, radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//east
		consumer.vertex(matrix, radius, 0f, -radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, radius - innerRadius, 0.5f, -radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, radius - innerRadius, 0.5f, radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, radius, 0f, radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//west
		consumer.vertex(matrix, -radius, 0f, -radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, -radius + innerRadius, 0.5f, -radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, -radius + innerRadius, 0.5f, radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, -radius, 0f, radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//bottom
		//north
		consumer.vertex(matrix, -radius / 2.5f, 0f, -radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, -radius / 3f, -0.5f, -radius + innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, radius / 3f, -0.5f, -radius + innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, radius / 2.5f, 0f, -radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//south
		consumer.vertex(matrix, -radius / 2.5f, 0f, radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, -radius / 3f, -0.5f, radius - innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, radius / 3f, -0.5f, radius - innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, radius / 2.5f, 0f, radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//east
		consumer.vertex(matrix, radius, 0f, -radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, radius - innerRadius, -0.5f, -radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, radius - innerRadius, -0.5f, radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, radius, 0f, radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//west
		consumer.vertex(matrix, -radius, 0f, -radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, -radius + innerRadius, -0.5f, -radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, -radius + innerRadius, -0.5f, radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, -radius, 0f, radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		
		//diagonal faces
		//top
		//north - east
		consumer.vertex(matrix, radius, 0f, -radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, radius - innerRadius, 0.5f, -radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, radius / 3f, 0.5f, -radius + innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, radius / 2.5f, 0f, -radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//east - south
		consumer.vertex(matrix, radius / 2.5f, 0f, radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, radius / 3f, 0.5f, radius - innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, radius - innerRadius, 0.5f, radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, radius, 0f, radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//south - west
		consumer.vertex(matrix, -radius / 2.5f, 0f, radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, -radius / 3f, 0.5f, radius - innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, -radius + innerRadius, 0.5f, radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, -radius, 0f, radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//west - north
		consumer.vertex(matrix, -radius, 0f, -radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, -radius + innerRadius, 0.5f, -radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, -radius / 3f, 0.5f, -radius + innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, -radius / 2.5f, 0f, -radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//bottom
		//north - east
		consumer.vertex(matrix, radius, 0f, -radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, radius - innerRadius, -0.5f, -radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, radius / 3f, -0.5f, -radius + innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, radius / 2.5f, 0f, -radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//east - south
		consumer.vertex(matrix, radius / 2.5f, 0f, radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, radius / 3f, -0.5f, radius - innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, radius - innerRadius, -0.5f, radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, radius, 0f, radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//south - west
		consumer.vertex(matrix, -radius / 2.5f, 0f, radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, -radius / 3f, -0.5f, radius - innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, -radius + innerRadius, -0.5f, radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, -radius, 0f, radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		//west - north
		consumer.vertex(matrix, -radius, 0f, -radius / 2.5f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 1f).next();
		consumer.vertex(matrix, -radius + innerRadius, -0.5f, -radius / 3f).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 1f).next();
		consumer.vertex(matrix, -radius / 3f, -0.5f, -radius + innerRadius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(1f, 0f).next();
		consumer.vertex(matrix, -radius / 2.5f, 0f, -radius).color(1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness()).texture(0.5f, 0f).next();
		
		matrices.pop();
	}
}
