package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.demon.RodentEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class RodentModel extends GeoModel<RodentEntity>
{
	@Override
	public Identifier getModelResource(RodentEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/rodent.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(RodentEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/rodent.png");
	}
	
	@Override
	public Identifier getAnimationResource(RodentEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entities/rodent.animation.json");
	}
}
