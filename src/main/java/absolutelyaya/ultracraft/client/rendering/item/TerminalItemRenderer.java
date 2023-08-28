package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.TerminalItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class TerminalItemRenderer extends GeoItemRenderer<TerminalItem>
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/block/terminal.png");
	
	public TerminalItemRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "terminal")));
	}
	
	@Override
	public Identifier getTextureLocation(TerminalItem animatable)
	{
		return TEXTURE;
	}
}
