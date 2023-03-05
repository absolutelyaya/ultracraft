package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.HellBulletItem;
import absolutelyaya.ultracraft.item.PierceRevolverItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Identifier;

public class ItemRegistry
{
	public static final Item BLUE_SKULL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "blue_skull"), new Item(new Item.Settings()));
	public static final Item RED_SKULL = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "red_skull"), new Item(new Item.Settings()));
	public static final Item HELL_BULLET = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "hell_bullet"), new HellBulletItem(new Item.Settings().fireproof().maxCount(25)));
	
	public static final PierceRevolverItem PIERCE_REVOLVER = Registry.register(Registries.ITEM,
			new Identifier(Ultracraft.MOD_ID, "pierce_revolver"), new PierceRevolverItem(new Item.Settings().maxCount(1)));
	
	public static final ItemGroup ULTRACRAFT_TAB =
			FabricItemGroup.builder(new Identifier(Ultracraft.MOD_ID, "item")).icon(() -> new ItemStack(BLUE_SKULL)).build();
	
	public static void register()
	{
		ItemGroupEvents.modifyEntriesEvent(ULTRACRAFT_TAB).register(content -> {
			content.add(BLUE_SKULL);
			content.add(RED_SKULL);
			content.add(HELL_BULLET);
			content.add(BlockRegistry.ELEVATOR.asItem());
			content.add(BlockRegistry.PEDESTAL.asItem());
			content.add(PIERCE_REVOLVER);
		});
	}
}
