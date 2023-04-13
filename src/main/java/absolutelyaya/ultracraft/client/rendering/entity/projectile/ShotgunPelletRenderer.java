package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.entity.projectile.ShotgunPelletEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class ShotgunPelletRenderer extends FlyingItemEntityRenderer<ShotgunPelletEntity>
{
	public ShotgunPelletRenderer(EntityRendererFactory.Context context)
	{
		super(context);
	}
	
	@Override
	public void render(ShotgunPelletEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
		matrices.scale(0.5f, 0.5f, 0.5f);
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, 15728880);
		matrices.pop();
	}
}
