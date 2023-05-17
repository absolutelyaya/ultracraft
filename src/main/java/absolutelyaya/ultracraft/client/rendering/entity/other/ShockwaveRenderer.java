package absolutelyaya.ultracraft.client.rendering.entity.other;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.entity.other.ShockwaveEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ShockwaveRenderer extends EntityRenderer<ShockwaveEntity>
{
	final ShockwaveModel model;
	
	public ShockwaveRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx);
		model = new ShockwaveModel(ctx.getModelLoader().getModelPart(UltracraftClient.SHOCKWAVE_LAYER));
	}
	
	@Override
	public Identifier getTexture(ShockwaveEntity entity)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/shockwave.png");
	}
	
	@Override
	public void render(ShockwaveEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
		matrices.translate(0f, -0.5f, 0f);
		matrices.scale(entity.getRadius() * 2, 1f, entity.getRadius() * 2);
		float f = Math.min(1f - (float)entity.age / entity.getDuration() + 0.3f, 1f);
		model.render(matrices, vertexConsumers.getBuffer(RenderLayers.getShockWave(getTexture(entity))), light,
				OverlayTexture.DEFAULT_UV, 1f, 0.8f * f, 0.6f * f, 0.4f * entity.getOpagueness());
		matrices.pop();
	}
}
