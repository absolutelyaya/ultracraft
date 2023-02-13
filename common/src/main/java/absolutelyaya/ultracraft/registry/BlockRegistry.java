package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.ElevatorBlock;
import absolutelyaya.ultracraft.block.PedestalBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class BlockRegistry
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Ultracraft.MOD_ID, Registry.BLOCK_KEY);
	public static final RegistrySupplier<Block> ELEVATOR = BLOCKS.register("elevator",
			() -> new ElevatorBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.DULL_RED)));
	public static final RegistrySupplier<Block> PEDESTAL = BLOCKS.register("pedestal",
			() -> new PedestalBlock(AbstractBlock.Settings.copy(Blocks.COBBLESTONE).mapColor(MapColor.GRAY).nonOpaque()));
	
	public static void register()
	{
		BLOCKS.register();
	}
}
