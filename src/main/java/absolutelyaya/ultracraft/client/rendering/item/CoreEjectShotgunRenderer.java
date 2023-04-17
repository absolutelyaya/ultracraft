package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.item.CoreEjectShotgunItem;
import net.minecraft.client.MinecraftClient;
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
		GunCooldownManager cdm = ((WingedPlayerEntity)MinecraftClient.getInstance().player).getGunCooldownManager();
		int primaryCD = cdm.getCooldown(animatable, 0);
		
		if(primaryCD < 32)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun1.png");
		else if(primaryCD < 37)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun0.png");
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
