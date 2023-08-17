package absolutelyaya.ultracraft.client.rendering.entity.other;

import absolutelyaya.ultracraft.entity.other.BackTank;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class BackTankRenderer extends EntityRenderer<BackTank>
{
	public BackTankRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx);
	}
	
	@Override
	public Identifier getTexture(BackTank entity)
	{
		return null;
	}
	
	@Override
	public boolean shouldRender(BackTank entity, Frustum frustum, double x, double y, double z)
	{
		return true;
	}
}
