package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.entity.projectile.ThrownSoapEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class ThrownSoapRenderer extends FlyingItemEntityRenderer<ThrownSoapEntity>
{
	public ThrownSoapRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, 0.75f, true);
	}
	
	@Override
	public void render(ThrownSoapEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		float deltaTick = MinecraftClient.getInstance().getTickDelta();
		matrices.push();
		matrices.multiply(new Quaternionf(new AxisAngle4f(((entity.age + deltaTick) * Math.min((entity.age + deltaTick) / 20f, 2f) * 0.25f), 1f, 0f, 0f)));
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		matrices.pop();
	}
}
