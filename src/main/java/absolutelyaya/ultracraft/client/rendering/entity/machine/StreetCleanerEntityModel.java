package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.machine.StreetCleanerEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class StreetCleanerEntityModel extends GeoModel<StreetCleanerEntity>
{
	@Override
	public Identifier getModelResource(StreetCleanerEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/streetcleaner.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(StreetCleanerEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/streetcleaner.png");
	}
	
	@Override
	public Identifier getAnimationResource(StreetCleanerEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entities/streetcleaner.animation.json");
	}
}
