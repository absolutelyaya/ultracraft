package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.demon.HideousMassEntity;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;

public class HideousMassModel extends GeoModel<HideousMassEntity>
{
	final Identifier NORMAL = new Identifier(Ultracraft.MOD_ID, "textures/entity/hideous_mass.png");
	final Identifier ENRAGED = new Identifier(Ultracraft.MOD_ID, "textures/entity/hideous_mass_enraged.png");
	final Identifier DYING = new Identifier(Ultracraft.MOD_ID, "textures/entity/hideous_mass_dying.png");
	
	@Override
	public Identifier getModelResource(HideousMassEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/hideous_mass.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(HideousMassEntity animatable)
	{
		if(animatable.isDying() || animatable.isDead())
			return DYING;
		return animatable.isEnraged() ? ENRAGED : NORMAL;
	}
	
	@Override
	public Identifier getAnimationResource(HideousMassEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entities/hideous_mass.animation.json");
	}
	
	@Override
	public void setCustomAnimations(HideousMassEntity animatable, long instanceId, AnimationState<HideousMassEntity> animationState)
	{
		super.setCustomAnimations(animatable, instanceId, animationState);
		CoreGeoBone harpoon = this.getAnimationProcessor().getBone("harpoon");
		
		harpoon.setHidden(!animatable.isHasHarpoon());
	}
}
