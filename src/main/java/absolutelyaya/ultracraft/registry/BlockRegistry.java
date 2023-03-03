package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.ElevatorBlock;
import absolutelyaya.ultracraft.block.PedestalBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockRegistry
{
	public static final Block ELEVATOR = register("elevator",
			new ElevatorBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.DULL_RED).requiresTool()),
			true);
	public static final Block PEDESTAL = register("pedestal",
			new PedestalBlock(AbstractBlock.Settings.copy(Blocks.COBBLESTONE).mapColor(MapColor.GRAY).nonOpaque().requiresTool()),
			true);
	
	@SuppressWarnings("SameParameterValue")
	private static Block register(String name, Block block, boolean item, int burn, int spread)
	{
		FlammableBlockRegistry.getDefaultInstance().add(block, burn, spread);
		if(item)
			registerItem(name, block);
		return Registry.register(Registries.BLOCK, new Identifier(Ultracraft.MOD_ID, name), block);
	}
	
	private static Block register(String name, Block block, boolean item)
	{
		if(item)
			registerItem(name, block);
		return Registry.register(Registries.BLOCK, new Identifier(Ultracraft.MOD_ID, name), block);
	}
	
	private static void registerItem(String name, Block block)
	{
		Registry.register(Registries.ITEM, new Identifier(Ultracraft.MOD_ID, name),
				new BlockItem(block, new FabricItemSettings()));
	}
	
	public static void registerBlocks()
	{
	
	}
}