package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.block.CerberusBlockEntity;
import mod.azure.azurelib.renderer.GeoBlockRenderer;

public class CerberusBlockRenderer extends GeoBlockRenderer<CerberusBlockEntity>
{
	public CerberusBlockRenderer()
	{
		super(new CerberusBlockModel());
	}
}
