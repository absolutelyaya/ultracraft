package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.CerberusBlockEntity;
import mod.azure.azurelib.model.DefaultedBlockGeoModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class CerberusBlockModel extends DefaultedBlockGeoModel<CerberusBlockEntity>
{
	public CerberusBlockModel()
	{
		super(new Identifier(Ultracraft.MOD_ID, "cerberus_block"));
	}
	
	@Override
	public Identifier getTextureResource(CerberusBlockEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/cerberus.png");
	}
	
	@Override
	public RenderLayer getRenderType(CerberusBlockEntity animatable, Identifier texture)
	{
		return RenderLayer.getEntitySolid(getTextureResource(animatable));
	}
}
