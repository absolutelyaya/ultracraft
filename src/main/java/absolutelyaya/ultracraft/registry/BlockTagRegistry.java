package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class BlockTagRegistry
{
	public static final TagKey<Block> FRAGILE = TagKey.of(RegistryKeys.BLOCK, new Identifier(Ultracraft.MOD_ID, "fragile"));
	
	public static final TagKey<Fluid> UNSKIPPABLE_FLUIDS = TagKey.of(RegistryKeys.FLUID, new Identifier(Ultracraft.MOD_ID, "unskippable"));
	
	public static void register()
	{
	
	}
}
