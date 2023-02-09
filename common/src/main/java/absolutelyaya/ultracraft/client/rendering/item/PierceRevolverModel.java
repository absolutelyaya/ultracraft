package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.PierceRevolverItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PierceRevolverModel extends AnimatedGeoModel<PierceRevolverItem>
{
	float lastRotSpeed;
	
	@Override
	public Identifier getModelResource(PierceRevolverItem object)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/items/pierce_revolver.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(PierceRevolverItem object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver.png");
	}
	
	@Override
	public Identifier getAnimationResource(PierceRevolverItem animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/items/pierce_revolver.animation.json");
	}
}
