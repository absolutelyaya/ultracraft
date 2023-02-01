package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockTagRegistry
{
	public static TagKey<Block> PUNCH_BREAKABLE = TagKey.of(Registry.BLOCK_KEY, new Identifier(Ultracraft.MOD_ID, "punch_breakable"));
	
	public static void register()
	{
	
	}
}
