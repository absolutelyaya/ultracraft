package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.CoreEjectShotgunItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CoreEjectShotgunRenderer extends GeoItemRenderer<CoreEjectShotgunItem>
{
	public CoreEjectShotgunRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "core_shotgun")));
	}
	
	@Override
	public Identifier getTextureLocation(CoreEjectShotgunItem animatable)
	{
		//TODO: charge texture animation
		//float useTime = 1f - (animatable.getMaxUseTime(null) - animatable.getApproxUseTime()) / (float)(animatable.getMaxUseTime(null));
		//if(useTime > 0.99f)
		//	return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver.png");
		//else if(useTime > 0.5f)
		//	return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver3.png");
		//else if(useTime > 0f)
		//	return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver2.png");
		
		return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun.png");
	}
}
