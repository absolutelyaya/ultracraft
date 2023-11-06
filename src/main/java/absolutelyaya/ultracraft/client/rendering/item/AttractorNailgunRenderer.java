package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.AttractorNailgunItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

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
