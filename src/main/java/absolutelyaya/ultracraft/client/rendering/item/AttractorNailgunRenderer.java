package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.item.AttractorNailgunItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AttractorNailgunRenderer extends GeoItemRenderer<AttractorNailgunItem>
{
	public AttractorNailgunRenderer()
	{
		super(new AttractorNailgunModel());
	}
}
