package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.HellBulletItem;
import absolutelyaya.ultracraft.item.PierceRevolverItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemRegistry
{
	public static final Item BLUE_SKULL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "blue_skull"), new Item(new FabricItemSettings()));
	public static final Item RED_SKULL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "red_skull"), new Item(new FabricItemSettings()));
	public static final Item HELL_BULLET = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "hell_bullet"), new HellBulletItem(new FabricItemSettings().fireproof().maxCount(25)));
	public static final Item CERBERUS_BALL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "cerberus_ball"), new HellBulletItem(new FabricItemSettings().fireproof().maxCount(21)));
	
	public static final PierceRevolverItem PIERCE_REVOLVER = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "pierce_revolver"), new PierceRevolverItem(new FabricItemSettings().maxCount(1)));
	
	public static final SpawnEggItem FILTH_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "filth_spawn_egg"),
			new SpawnEggItem(EntityRegistry.FILTH, 0x717038, 0xacaa7a, new FabricItemSettings()));
	public static final SpawnEggItem STRAY_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "stray_spawn_egg"),
			new SpawnEggItem(EntityRegistry.STRAY, 0xaa6f5e, 0x922923, new FabricItemSettings()));
	public static final SpawnEggItem SCHISM_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "schism_spawn_egg"),
			new SpawnEggItem(EntityRegistry.SCHISM, 0x572f21, 0xa0938e, new FabricItemSettings()));
	public static final SpawnEggItem MALICIOUS_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "malicious_spawn_egg"),
			new SpawnEggItem(EntityRegistry.MALICIOUS_FACE, 0xa0938e, 0x5a5353, new FabricItemSettings()));
	public static final SpawnEggItem CERBERUS_SPAWN_EGG = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "cerberus_spawn_egg"),
			new SpawnEggItem(EntityRegistry.CERBERUS, 0xa0938e, 0x5a5353, new FabricItemSettings()));
	
	public static final ItemGroup ULTRACRAFT_TAB =
			FabricItemGroup.builder(new Identifier(Ultracraft.MOD_ID, "item")).icon(() -> new ItemStack(BLUE_SKULL)).build();
	
	public static void register()
	{
		ItemGroupEvents.modifyEntriesEvent(ULTRACRAFT_TAB).register(content -> {
			content.add(BLUE_SKULL);
			content.add(RED_SKULL);
			content.add(HELL_BULLET);
			content.add(CERBERUS_BALL);
			content.add(BlockRegistry.ELEVATOR.asItem());
			content.add(BlockRegistry.PEDESTAL.asItem());
			content.add(BlockRegistry.CERBERUS.asItem());
			content.add(PIERCE_REVOLVER);
			content.add(FILTH_SPAWN_EGG);
			content.add(STRAY_SPAWN_EGG);
			content.add(SCHISM_SPAWN_EGG);
			content.add(MALICIOUS_SPAWN_EGG);
			content.add(CERBERUS_SPAWN_EGG);
		});
	}
}
