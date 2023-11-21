package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.PlushieItem;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.model.DefaultedItemGeoModel;

public class V2PlushieRenderer extends AbstractPlushieRenderer<PlushieItem>
{
	public V2PlushieRenderer()
	{
		super(new DefaultedItemGeoModel<PlushieItem>(new Identifier(Ultracraft.MOD_ID, "plushie"))
					  .withAltModel(new Identifier(Ultracraft.MOD_ID, "v2_plushie")));
	}
}
