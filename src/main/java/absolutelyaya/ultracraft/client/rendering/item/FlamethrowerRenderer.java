package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.FlamethrowerItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class FlamethrowerRenderer extends GeoItemRenderer<FlamethrowerItem>
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/item/flamethrower.png");
	
	public FlamethrowerRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "flamethrower")));
	}
	
	@Override
	public Identifier getTextureLocation(FlamethrowerItem animatable)
	{
		return TEXTURE;
	}
}
