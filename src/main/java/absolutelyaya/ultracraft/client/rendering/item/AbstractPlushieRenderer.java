package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.PlushieItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public abstract class AbstractPlushieRenderer<P extends PlushieItem & GeoAnimatable> extends GeoItemRenderer<P>
{
	public AbstractPlushieRenderer(DefaultedItemGeoModel<P> model)
	{
		super(model);
	}
	
	@Override
	public Identifier getTextureLocation(P animatable)
	{
		return switch(animatable.getType())
		{
			default -> new Identifier(Ultracraft.MOD_ID, "textures/item/v1_plush.png");
			case YAYA -> new Identifier(Ultracraft.MOD_ID, "textures/item/yaya_plush.png");
			case HAKITA -> new Identifier(Ultracraft.MOD_ID, "textures/item/hakita_plush.png");
			case PITR, PITRPOIN -> new Identifier(Ultracraft.MOD_ID, "textures/item/pitr_plush.png");
		};
	}
}
