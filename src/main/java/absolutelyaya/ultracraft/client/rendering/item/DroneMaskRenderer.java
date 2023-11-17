package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.DroneMaskItem;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.model.DefaultedItemGeoModel;
import mod.azure.azurelib.renderer.GeoItemRenderer;

public class DroneMaskRenderer extends GeoItemRenderer<DroneMaskItem>
{
	public DroneMaskRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "drone_mask")));
	}
	
	@Override
	public Identifier getTextureLocation(DroneMaskItem animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/item/drone_mask.png");
	}
}
