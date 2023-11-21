package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.projectile.MagnetEntity;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;

public class MagnetEntityModel extends GeoModel<MagnetEntity>
{
	@Override
	public Identifier getModelResource(MagnetEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/magnet.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(MagnetEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/magnet.png");
	}
	
	@Override
	public Identifier getAnimationResource(MagnetEntity animatable)
	{
		return null;
	}
	
	@Override
	public void setCustomAnimations(MagnetEntity animatable, long instanceId, AnimationState<MagnetEntity> animationState)
	{
		super.setCustomAnimations(animatable, instanceId, animationState);
		CoreGeoBone flash = this.getAnimationProcessor().getBone("Flash");
		
		flash.setScaleX(animatable.getFlash());
		flash.setScaleY(animatable.getFlash());
		flash.setScaleZ(animatable.getFlash());
	}
}
