package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.entity.machine.StreetCleanerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StreetCleanerEntityRenderer extends GeoEntityRenderer<StreetCleanerEntity>
{
	public StreetCleanerEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new StreetCleanerEntityModel());
	}
	
	@Override
	public RenderLayer getRenderType(StreetCleanerEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayer.getEntityTranslucent(texture);
	}
}
