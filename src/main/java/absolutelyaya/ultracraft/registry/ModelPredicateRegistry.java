package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.PedestalBlock;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class ModelPredicateRegistry
{
	@SuppressWarnings("ConstantConditions")
	public static void registerModels()
	{
		ModelPredicateProviderRegistry.register(BlockRegistry.PEDESTAL.asItem(), new Identifier(Ultracraft.MOD_ID, "type"),
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
}
