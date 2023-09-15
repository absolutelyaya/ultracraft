package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.HideousMassRageLayer;
import absolutelyaya.ultracraft.entity.demon.HideousMassEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HideousMassRenderer extends GeoEntityRenderer<HideousMassEntity>
{
	public HideousMassRenderer(EntityRendererFactory.Context renderManager)
	{
		super(renderManager, new HideousMassModel());
		addRenderLayer(new HideousMassRageLayer(this));
	}
	
	@Override
	public void render(HideousMassEntity entity, float entityYaw, float partialTick, MatrixStack matrices, VertexConsumerProvider bufferSource, int packedLight)
	{
		matrices.push();
		if(animatable != null && (animatable.isDying() || animatable.isDead()))
		{
			Random r = entity.getRandom();
			float f = UltracraftClient.getConfigHolder().get().safeVFX ? 0.1f : 0.5f;
			matrices.translate(r.nextFloat() * f, r.nextFloat() * f, r.nextFloat() * f);
		}
		super.render(entity, entityYaw, partialTick, matrices, bufferSource, packedLight);
		matrices.pop();
	}
}
