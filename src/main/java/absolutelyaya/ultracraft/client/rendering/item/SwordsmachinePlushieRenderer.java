package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.PlushieItem;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.model.DefaultedItemGeoModel;

public class SwordsmachinePlushieRenderer extends AbstractPlushieRenderer<PlushieItem>
{
	public SwordsmachinePlushieRenderer()
	{
		super(new DefaultedItemGeoModel<PlushieItem>(new Identifier(Ultracraft.MOD_ID, "plushie"))
					  .withAltModel(new Identifier(Ultracraft.MOD_ID, "swordsmachine_plushie"))
					  .withAltAnimations(new Identifier(Ultracraft.MOD_ID, "swordsmachine_plushie")));
	}
}
