package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.PedestalBlock;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class ModelPredicateRegistry
{
	@SuppressWarnings("ConstantConditions")
	public static void registerModels()
	{
		registerHoldUsageItem(ItemRegistry.PIERCE_REVOLVER.get());
		ModelPredicateProviderRegistry.register(ItemRegistry.PEDESTAL.get(), new Identifier(Ultracraft.MOD_ID, "type"),
				(stack, world, entity, seed) -> {
					if(!stack.hasNbt())
						return 0;
					NbtCompound state = stack.getNbt().getCompound("BlockStateTag");
					PedestalBlock.Type type = PedestalBlock.Type.valueOf(state.getString("type").toUpperCase());
					for (int i = 0; i < PedestalBlock.Type.values().length; i++)
					{
						if(type.equals(PedestalBlock.Type.values()[i]))
							return i / 10f;
					}
					return 0;
				});
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
