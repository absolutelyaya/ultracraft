package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.entity.projectile.EjectedCoreEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class EjectedCoreRenderer extends FlyingItemEntityRenderer<EjectedCoreEntity>
{
	public EjectedCoreRenderer(EntityRendererFactory.Context context)
	{
		super(context);
	}
	
	@Override
	public void render(EjectedCoreEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, 15728880);
	}
}
