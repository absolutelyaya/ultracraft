package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.entity.demon.RodentEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RodentRenderer extends GeoEntityRenderer<RodentEntity>
{
	public RodentRenderer(EntityRendererFactory.Context renderManager)
	{
		super(renderManager, new RodentModel());
	}
	
	@Override
	public void render(RodentEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight)
	{
		poseStack.push();
		int size = entity.getSize();
		if(size == 0)
			poseStack.scale(0.25f, 0.25f, 0.25f);
		else if (size > 0)
			poseStack.scale(size, size, size);
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		poseStack.pop();
	}
}
