package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.PedestalBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class BlockEntityRegistry
{
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Ultracraft.MOD_ID, Registry.BLOCK_ENTITY_TYPE_KEY);
	public static final RegistrySupplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL = BLOCK_ENTITIES.register("pedestal",
			() -> BlockEntityType.Builder.create(PedestalBlockEntity::new, BlockRegistry.PEDESTAL.get()).build(null));
	
	@SuppressWarnings("ConstantConditions")
	public static void register()
	{
		BLOCK_ENTITIES.register();
	}
}
