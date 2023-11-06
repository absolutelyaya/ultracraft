package absolutelyaya.ultracraft.client.rendering.entity.other;

import absolutelyaya.ultracraft.entity.other.ShockwaveEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class VerticalShockwaveRenderer extends ShockwaveRenderer
{
	public VerticalShockwaveRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx);
	}
	
	@Override
	public void render(ShockwaveEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
		matrices.multiply(new Quaternionf(new AxisAngle4f(90f * MathHelper.RADIANS_PER_DEGREE, 0f, 0f, 1f)));
		matrices.multiply(new Quaternionf(new AxisAngle4f(-yaw * MathHelper.RADIANS_PER_DEGREE, 1f, 0f, 0f)));
		matrices.translate(0f, -1f, 0f);
		matrices.scale(1f, 2f, 1f);
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		matrices.pop();
	}
}
