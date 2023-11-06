package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.DroneEmissiveLayer;
import absolutelyaya.ultracraft.entity.machine.DroneEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DroneEntityRenderer extends GeoEntityRenderer<DroneEntity>
{
	public DroneEntityRenderer(EntityRendererFactory.Context renderManager)
	{
		super(renderManager, new DroneEntityModel());
		addRenderLayer(new DroneEmissiveLayer(this));
	}
	
	@Override
	public Identifier getTexture(DroneEntity animatable)
	{
		return model.getTextureResource(animatable);
	}
	
	@Override
	public RenderLayer getRenderType(DroneEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayers.getEntityCutoutNoCull(texture);
	}
}
