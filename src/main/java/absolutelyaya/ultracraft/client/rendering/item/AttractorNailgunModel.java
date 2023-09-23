package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.AttractorNailgunItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class AttractorNailgunModel extends GeoModel<AttractorNailgunItem>
{
	@Override
	public Identifier getModelResource(AttractorNailgunItem animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/item/nailgun.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(AttractorNailgunItem animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/item/nailgun.png");
	}
	
	@Override
	public Identifier getAnimationResource(AttractorNailgunItem animatable)
	{
		return null;
	}
}
