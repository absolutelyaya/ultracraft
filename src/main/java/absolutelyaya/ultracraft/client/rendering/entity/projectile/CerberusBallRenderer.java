package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.entity.projectile.CerberusBallEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class CerberusBallRenderer extends FlyingItemEntityRenderer<CerberusBallEntity>
{
	public CerberusBallRenderer(EntityRendererFactory.Context context)
	{
		super(context);
	}
	
	@Override
	public void render(CerberusBallEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, 15728880);
	}
}
