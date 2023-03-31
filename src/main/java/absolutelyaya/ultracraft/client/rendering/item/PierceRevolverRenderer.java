package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.PierceRevolverItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PierceRevolverRenderer extends GeoItemRenderer<PierceRevolverItem>
{
	public PierceRevolverRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "pierce_revolver")));
	}
	
	@Override
	public Identifier getTextureLocation(PierceRevolverItem animatable)
	{
		double cooldown = 0f;
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player != null)
			cooldown = player.getItemCooldownManager().getCooldownProgress(animatable, 0f);
		
		if (cooldown > 0.5f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver0.png");
		else if (cooldown > 0f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver1.png");
		
		float useTime = 1f - (animatable.getMaxUseTime(null) - animatable.getApproxUseTime()) / (float)(animatable.getMaxUseTime(null));
		if(useTime > 0.99f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver.png");
		else if(useTime > 0.5f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver3.png");
		else if(useTime > 0f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver2.png");
		
		return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver.png");
	}
}
