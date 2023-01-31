package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.block.*;
import net.minecraft.util.registry.Registry;

public class BlockRegistry
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Ultracraft.MOD_ID, Registry.BLOCK_KEY);
	
	public static void register()
	{
		BLOCKS.register();
	}
}
