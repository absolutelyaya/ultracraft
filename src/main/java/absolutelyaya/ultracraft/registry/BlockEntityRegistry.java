package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.CerberusBlockEntity;
import absolutelyaya.ultracraft.block.HellObserverBlockEntity;
import absolutelyaya.ultracraft.block.PedestalBlockEntity;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockEntityRegistry
{
	public static final BlockEntityType<PedestalBlockEntity> PEDESTAL;
	public static final BlockEntityType<CerberusBlockEntity> CERBERUS;
	public static final BlockEntityType<TerminalBlockEntity> TERMINAL;
	public static final BlockEntityType<HellObserverBlockEntity> HELL_OBSERVER;
	
	public static void register() {
	}
	
	static
	{
		PEDESTAL = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Ultracraft.MOD_ID, "pedestal"),
				FabricBlockEntityTypeBuilder.create(PedestalBlockEntity::new, BlockRegistry.PEDESTAL).build());
		CERBERUS = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Ultracraft.MOD_ID, "cerberus_block"),
				FabricBlockEntityTypeBuilder.create(CerberusBlockEntity::new, BlockRegistry.CERBERUS).build());
		TERMINAL = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Ultracraft.MOD_ID, "terminal"),
				FabricBlockEntityTypeBuilder.create(TerminalBlockEntity::new, BlockRegistry.TERMINAL_DISPLAY).build());
		HELL_OBSERVER = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				new Identifier(Ultracraft.MOD_ID, "hell_observer"),
				FabricBlockEntityTypeBuilder.create(HellObserverBlockEntity::new, BlockRegistry.HELL_OBSERVER).build());
	}
}
