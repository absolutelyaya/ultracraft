package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.MachineSwordItem;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.model.DefaultedItemGeoModel;
import mod.azure.azurelib.renderer.GeoItemRenderer;

public class MachineSwordRenderer extends GeoItemRenderer<MachineSwordItem>
{
	public MachineSwordRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "machinesword")));
	}
	
	@Override
	public Identifier getTextureLocation(MachineSwordItem animatable)
	{
		return switch(MachineSwordItem.getType(getCurrentItemStack()))
		{
			case NORMAL -> new Identifier(Ultracraft.MOD_ID, "textures/item/machinesword.png");
			case TUNDRA -> new Identifier(Ultracraft.MOD_ID, "textures/item/machinesword_tundra.png");
			case AGONY -> new Identifier(Ultracraft.MOD_ID, "textures/item/machinesword_agony.png");
		};
	}
}
