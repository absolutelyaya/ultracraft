package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.SharpshooterRevolverItem;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.model.DefaultedItemGeoModel;
import mod.azure.azurelib.renderer.GeoItemRenderer;

public class SharpshooterRevolverRenderer extends GeoItemRenderer<SharpshooterRevolverItem>
{
	public SharpshooterRevolverRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "revolver")));
	}
	
	@Override
	public Identifier getTextureLocation(SharpshooterRevolverItem animatable)
	{
		int charges = animatable.getNbt(currentItemStack, "charges");
		
		return switch (charges)
		{
			case 0 -> new Identifier(Ultracraft.MOD_ID, "textures/item/sharpshooter_revolver2.png");
			case 1 -> new Identifier(Ultracraft.MOD_ID, "textures/item/sharpshooter_revolver1.png");
			case 2 -> new Identifier(Ultracraft.MOD_ID, "textures/item/sharpshooter_revolver0.png");
			default -> new Identifier(Ultracraft.MOD_ID, "textures/item/sharpshooter_revolver.png");
		};
	}
}
