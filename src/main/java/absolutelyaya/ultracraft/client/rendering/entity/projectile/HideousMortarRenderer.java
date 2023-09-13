package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.entity.projectile.HellBulletEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class HideousMortarRenderer extends HellBulletRenderer
{
	public HideousMortarRenderer(EntityRendererFactory.Context context)
	{
		super(context);
	}
	
	@Override
	public void render(HellBulletEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
		matrices.translate(0f, 0.1f, 0f);
		matrices.scale(2.5f, 2.5f, 2.5f);
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, 15728880);
		matrices.pop();
	}
	
	@Override
	public Identifier getTexture(HellBulletEntity entity)
	{
		return CERB_BALL;
	}
}
