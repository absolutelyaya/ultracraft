package absolutelyaya.ultracraft.client.entity.projectile;

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
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, 15728880);
		//TODO: add Aura
	}
}
