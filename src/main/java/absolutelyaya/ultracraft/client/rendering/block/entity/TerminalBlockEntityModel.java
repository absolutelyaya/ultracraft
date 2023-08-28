package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

public class TerminalBlockEntityModel extends DefaultedBlockGeoModel<TerminalBlockEntity>
{
	public TerminalBlockEntityModel()
	{
		super(new Identifier(Ultracraft.MOD_ID, "terminal"));
	}
	
	@Override
	public Identifier getTextureResource(TerminalBlockEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/block/terminal.png");
	}
	
	@Override
	public RenderLayer getRenderType(TerminalBlockEntity animatable, Identifier texture)
	{
		return RenderLayer.getEntitySolid(getTextureResource(animatable));
	}
}
