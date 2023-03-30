package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.PlushieItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PlushieRenderer extends GeoItemRenderer<PlushieItem>
{
	public PlushieRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "plushie")));
	}
	
	@Override
	public Identifier getTextureLocation(PlushieItem animatable)
	{
		return switch(animatable.getType())
		{
			default -> new Identifier(Ultracraft.MOD_ID, "textures/item/v1_plush.png");
			case YAYA -> new Identifier(Ultracraft.MOD_ID, "textures/item/yaya_plush.png");
			case HAKITA -> new Identifier(Ultracraft.MOD_ID, "textures/item/hakita_plush.png");
			case PITR -> new Identifier(Ultracraft.MOD_ID, "textures/item/pitr_plush.png");
		};
	}
}
