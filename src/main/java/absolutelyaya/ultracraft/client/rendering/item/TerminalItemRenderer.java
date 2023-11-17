package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.TerminalItem;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.model.DefaultedItemGeoModel;
import mod.azure.azurelib.renderer.GeoItemRenderer;

public class TerminalItemRenderer extends GeoItemRenderer<TerminalItem>
{
	public TerminalItemRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "terminal")));
	}
	
	@Override
	public Identifier getTextureLocation(TerminalItem animatable)
	{
		return TerminalItem.getBase(getCurrentItemStack()).getTexture();
	}
}
