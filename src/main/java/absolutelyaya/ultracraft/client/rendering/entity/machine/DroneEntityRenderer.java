package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.RenderLayers;
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
	}
	
	@Override
	public Identifier getTexture(DroneEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/drone.png");
	}
	
	@Override
	public RenderLayer getRenderType(DroneEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayers.getEntityCutoutNoCull(texture);
	}
}
