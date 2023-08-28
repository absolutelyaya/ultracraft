package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockRegistry
{
	public static final Block ELEVATOR = register("elevator",
			new ElevatorBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.DULL_RED).requiresTool()),
			true);
	public static final Block ELEVATOR_WALL = register("elevator_wall",
			new ElevatorBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.DULL_RED).requiresTool()),
			true);
	public static final Block ELEVATOR_FLOOR = register("elevator_floor",
			new ElevatorFloorBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.DULL_RED).requiresTool()),
			true);
	public static final Block SECRET_ELEVATOR = register("secret_elevator",
			new ElevatorBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.TERRACOTTA_GRAY).requiresTool()),
			true);
	public static final Block SECRET_ELEVATOR_WALL = register("secret_elevator_wall",
			new ElevatorBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.TERRACOTTA_GRAY).requiresTool()),
			true);
	public static final Block SECRET_ELEVATOR_FLOOR = register("secret_elevator_floor",
			new ElevatorFloorBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.TERRACOTTA_GRAY).requiresTool()),
			true);
	public static final Block PEDESTAL = register("pedestal",
			new PedestalBlock(AbstractBlock.Settings.copy(Blocks.COBBLESTONE).mapColor(MapColor.GRAY).nonOpaque().requiresTool()),
			true);
	public static final Block CERBERUS = register("cerberus_block",
			new CerberusBlock(AbstractBlock.Settings.copy(Blocks.COBBLESTONE).mapColor(MapColor.DEEPSLATE_GRAY).nonOpaque()
									  .requiresTool().strength(5f, 6f).luminance(b -> 6)), true);
	public static final Block BLOOD = register("blood", new FluidBlock(FluidRegistry.STILL_BLOOD,
			FabricBlockSettings.copyOf(Blocks.WATER).replaceable()), false);
	public static final Block FLESH = register("flesh",
			new FleshBlock(AbstractBlock.Settings.copy(Blocks.NETHERRACK).mapColor(MapColor.DARK_RED).nonOpaque()), true);
	public static final Block RUSTY_PIPE = register("rusty_pipe",
			new PipeBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN)), true);
	public static final Block RUSTY_MESH = register("rusty_mesh",
			new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN)), true);
	public static final Block CRACKED_STONE = register("cracked_stone",
			new Block(AbstractBlock.Settings.copy(Blocks.STONE).mapColor(MapColor.GRAY)), true);
	public static final Block TERMINAL = register("terminal",
			new TerminalBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.GRAY).nonOpaque()), false);
	public static final Block TERMINAL_DISPLAY = register("terminal_display",
			new TerminalDisplayBlock(AbstractBlock.Settings.copy(BlockRegistry.TERMINAL)), false);
	
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
