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
			default -> new Identifier(Ultracraft.MOD_ID, "textures/item/plushie/v1.png");
			case YAYA -> new Identifier(Ultracraft.MOD_ID, "textures/item/plushie/yaya.png");
			case HAKITA -> new Identifier(Ultracraft.MOD_ID, "textures/item/plushie/hakita.png");
			case PITR, PITRPOIN -> new Identifier(Ultracraft.MOD_ID, "textures/item/plushie/pitr.png");
			case SWORDSMACHINE -> new Identifier(Ultracraft.MOD_ID, "textures/item/plushie/swordsmachine.png");
			case SWORDSMACHINE_TUNDRA -> new Identifier(Ultracraft.MOD_ID, "textures/item/plushie/swordsmachine_tundra.png");
			case SWORDSMACHINE_AGONY -> new Identifier(Ultracraft.MOD_ID, "textures/item/plushie/swordsmachine_agony.png");
		};
	}
}
