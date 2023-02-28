package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.PierceRevolverItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class PierceRevolverModel extends GeoModel<PierceRevolverItem>
{
	@Override
	public Identifier getModelResource(PierceRevolverItem object)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/items/pierce_revolver.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(PierceRevolverItem object)
	{
		double cooldown = 0f;
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player != null)
			cooldown = player.getItemCooldownManager().getCooldownProgress(object, 0f);
		
		if (cooldown > 0.5f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver0.png");
		else if (cooldown > 0f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver1.png");
		
		float useTime = 1f - (object.getMaxUseTime(null) - object.getApproxUseTime()) / (float)(object.getMaxUseTime(null));
		if(useTime > 0.99f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver.png");
		else if(useTime > 0.5f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver3.png");
		else if(useTime > 0f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver2.png");
		
		return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver.png");
	}
	
	@Override
	public Identifier getAnimationResource(PierceRevolverItem animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/items/pierce_revolver.animation.json");
	}
}
