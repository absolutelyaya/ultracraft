package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.HellSpawnerItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HellSpawnerItemRenderer extends GeoItemRenderer<HellSpawnerItem>
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/block/hell_spawner.png");
	
	public HellSpawnerItemRenderer()
	{
		super(new HellSpawnerItemModel());
	}
	
	@Override
	public Identifier getTextureLocation(HellSpawnerItem animatable)
	{
		return TEXTURE;
	}
}
