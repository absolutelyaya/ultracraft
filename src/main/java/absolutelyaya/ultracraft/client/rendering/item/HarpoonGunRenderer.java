package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.HarpoonGunItem;
import absolutelyaya.ultracraft.item.MachineSwordItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HarpoonGunRenderer extends GeoItemRenderer<HarpoonGunItem>
{
	public HarpoonGunRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "harpoon_gun")));
	}
	
	@Override
	public Identifier getTextureLocation(HarpoonGunItem animatable)
	{
		return switch(animatable.texture) {
			default -> new Identifier(Ultracraft.MOD_ID, "textures/item/harpoon_gun.png");
			case 1 -> new Identifier(Ultracraft.MOD_ID, "textures/item/harpoon_gun0.png");
			case 2 -> new Identifier(Ultracraft.MOD_ID, "textures/item/harpoon_gun1.png");
			case 3 -> new Identifier(Ultracraft.MOD_ID, "textures/item/harpoon_gun2.png");
		};
	}
}
