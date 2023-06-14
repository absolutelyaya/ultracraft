package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TagRegistry
{
	public static final TagKey<Block> FRAGILE = TagKey.of(RegistryKeys.BLOCK, new Identifier(Ultracraft.MOD_ID, "fragile"));
	public static final TagKey<Block> EXPLOSION_BREAKABLE = TagKey.of(RegistryKeys.BLOCK, new Identifier(Ultracraft.MOD_ID, "explosion_breakable"));
	public static final TagKey<Block> PUNCH_BREAKABLE = TagKey.of(RegistryKeys.BLOCK, new Identifier(Ultracraft.MOD_ID, "punch_breakable"));
	
	public static final TagKey<Fluid> UNSKIMMABLE_FLUIDS = TagKey.of(RegistryKeys.FLUID, new Identifier(Ultracraft.MOD_ID, "unskimmable"));
	
	public static final TagKey<Item> PUNCH_FLAMES = TagKey.of(RegistryKeys.ITEM, new Identifier(Ultracraft.MOD_ID, "punch_flames"));
	
	public static void register()
	{
	
	}
}
