package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.AttractorNailgunItem;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.model.DefaultedItemGeoModel;
import mod.azure.azurelib.renderer.GeoItemRenderer;

public class AttractorNailgunRenderer extends GeoItemRenderer<AttractorNailgunItem>
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/item/nailgun.png");
	
	public AttractorNailgunRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "nailgun")));
	}
	
	@Override
	public Identifier getTextureLocation(AttractorNailgunItem animatable)
	{
		return TEXTURE;
	}
}
