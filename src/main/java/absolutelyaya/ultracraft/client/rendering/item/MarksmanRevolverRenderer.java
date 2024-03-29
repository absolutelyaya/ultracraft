package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.MarksmanRevolverItem;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.model.DefaultedItemGeoModel;
import mod.azure.azurelib.renderer.GeoItemRenderer;

public class MarksmanRevolverRenderer extends GeoItemRenderer<MarksmanRevolverItem>
{
	public MarksmanRevolverRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "revolver")));
	}
	
	@Override
	public Identifier getTextureLocation(MarksmanRevolverItem animatable)
	{
		int coins = animatable.getNbt(currentItemStack, "coins");
		
		if (coins == 3)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/marksman_revolver3.png");
		else if (coins == 2)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/marksman_revolver2.png");
		else if (coins == 1)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/marksman_revolver1.png");
		else if (coins == 0)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/marksman_revolver0.png");
		
		return new Identifier(Ultracraft.MOD_ID, "textures/item/marksman_revolver.png");
	}
}
