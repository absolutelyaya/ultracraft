package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class HellBulletRenderer extends FlyingItemEntityRenderer<HellBulletEntity>
{
	public HellBulletRenderer(EntityRendererFactory.Context context)
	{
		super(context);
	}
	
	@Override
	public void render(HellBulletEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
		matrices.translate(0f, 0.1f, 0f);
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, 15728880);
		matrices.pop();
	}
}
