package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.item.PumpShotgunItem;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PumpShotgunRenderer extends GeoItemRenderer<PumpShotgunItem>
{
	public PumpShotgunRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "shotgun")));
	}
	
	@Override
	public Identifier getTextureLocation(PumpShotgunItem animatable)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)MinecraftClient.getInstance().player).getGunCooldownManager();
		float primaryCD = cdm.getCooldownPercent(animatable, 0);
		if(primaryCD > 0.6f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pump_shotgun0.png");
		if(primaryCD > 0.4f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pump_shotgun.png");
		
		int charge = 0;
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player != null && player.getMainHandStack().isOf(ItemRegistry.PUMP_SHOTGUN) && player.getMainHandStack().hasNbt())
			charge = player.getMainHandStack().getNbt().getInt("charge");
		if(charge == 3)
		{
			if(player.age % 6 > 2)
				return new Identifier(Ultracraft.MOD_ID, "textures/item/pump_shotgun4.png");
			else
				return new Identifier(Ultracraft.MOD_ID, "textures/item/pump_shotgun3.png");
		}
		else if(charge == 2)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pump_shotgun2.png");
		else if(charge == 1)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pump_shotgun1.png");
		
		return new Identifier(Ultracraft.MOD_ID, "textures/item/pump_shotgun.png");
	}
}
