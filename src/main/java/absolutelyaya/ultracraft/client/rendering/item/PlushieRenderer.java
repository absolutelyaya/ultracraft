package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.PlushieItem;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.model.DefaultedItemGeoModel;

public class PlushieRenderer extends AbstractPlushieRenderer<PlushieItem>
{
	public PlushieRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "plushie")));
	}
}
