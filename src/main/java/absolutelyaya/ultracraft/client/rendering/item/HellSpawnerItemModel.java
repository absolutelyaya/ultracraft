package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.HellSpawnerItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class HellSpawnerItemModel extends GeoModel<HellSpawnerItem>
{
	@Override
	public Identifier getModelResource(HellSpawnerItem animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/block/hell_spawner.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(HellSpawnerItem animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/block/hell_spawner.png");
	}
	
	@Override
	public Identifier getAnimationResource(HellSpawnerItem animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/block/hell_spawner.animation.json");
	}
}
