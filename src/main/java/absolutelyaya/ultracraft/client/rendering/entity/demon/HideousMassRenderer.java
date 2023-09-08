package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.HideousMassRageLayer;
import absolutelyaya.ultracraft.entity.demon.HideousMassEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HideousMassRenderer extends GeoEntityRenderer<HideousMassEntity>
{
	public HideousMassRenderer(EntityRendererFactory.Context renderManager)
	{
		super(renderManager, new HideousMassModel());
		addRenderLayer(new HideousMassRageLayer(this));
	}
}
