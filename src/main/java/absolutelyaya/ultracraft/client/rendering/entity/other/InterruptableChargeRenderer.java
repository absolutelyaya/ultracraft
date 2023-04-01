package absolutelyaya.ultracraft.client.rendering.entity.other;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.entity.other.InterruptableCharge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class InterruptableChargeRenderer extends EntityRenderer<InterruptableCharge>
{
	InterruptableChargeModel model;
	
	public InterruptableChargeRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx);
		model = new InterruptableChargeModel(ctx.getModelLoader().getModelPart(UltracraftClient.INTERRUPTABLE_CHARGE_LAYER));
	}
	
	@Override
	public Identifier getTexture(InterruptableCharge entity)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/interruptable_charge.png");
	}
	
	@Override
	public void render(InterruptableCharge entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		RenderLayer renderLayer = RenderLayer.getEntityTranslucent(getTexture(entity));
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);
		matrices.push();
		float f = entity.getScale();
		matrices.scale(f, f, f);
		matrices.multiply(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
		model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
		matrices.pop();
	}
	
	@Override
	public boolean shouldRender(InterruptableCharge entity, Frustum frustum, double x, double y, double z)
	{
		return true;
	}
}
