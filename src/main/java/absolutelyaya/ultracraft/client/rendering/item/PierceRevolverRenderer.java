package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.item.PierceRevolverItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PierceRevolverRenderer extends GeoItemRenderer<PierceRevolverItem>
{
	public PierceRevolverRenderer()
	{
		super(new PierceRevolverModel());
	}
}
