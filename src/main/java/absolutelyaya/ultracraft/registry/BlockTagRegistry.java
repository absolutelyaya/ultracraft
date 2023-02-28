package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class BlockTagRegistry
{
	public static TagKey<Block> PUNCH_BREAKABLE = TagKey.of(RegistryKeys.BLOCK, new Identifier(Ultracraft.MOD_ID, "punch_breakable"));
	
	public static void register()
	{
	
	}
}
