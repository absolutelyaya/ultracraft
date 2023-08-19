package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.demon.HideousMassEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class HideousMassModel extends GeoModel<HideousMassEntity>
{
	@Override
	public Identifier getModelResource(HideousMassEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/hideous_mass.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(HideousMassEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/hideous_mass.png");
	}
	
	@Override
	public Identifier getAnimationResource(HideousMassEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entities/hideous_mass.animation.json");
	}
}
