package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.SharpshooterRevolverItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SharpshooterRevolverRenderer extends GeoItemRenderer<SharpshooterRevolverItem>
{
	public SharpshooterRevolverRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "revolver")));
	}
	
	@Override
	public Identifier getTextureLocation(SharpshooterRevolverItem animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver.png");
	}
}
