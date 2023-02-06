package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class ModelPredicateRegistry
{
	public static void registerModels()
	{
		registerHoldUsageItem(ItemRegistry.PIERCE_REVOLVER.get());
	}
	
	@SuppressWarnings("SameParameterValue")
	private static void registerHoldUsageItem(Item item)
	{
		ModelPredicateProviderRegistry.register(item, new Identifier("use_time"),
				(stack, world, entity, seed) -> {
					if(entity == null)
						return 0f;
					if(entity.getActiveItem() != stack)
						return 0f;
					return 1f - entity.getItemUseTimeLeft() / (float)(stack.getMaxUseTime());
				});
		
		ModelPredicateProviderRegistry.register(item, new Identifier(Ultracraft.MOD_ID, "cooldown"),
				(stack, world, entity, seed) -> {
					if(stack.getItem() instanceof AbstractWeaponItem gun)
						return gun.getApproxCooldown() > 0 ? 1f : 0f;
					return 0f;
				});
	}
}
