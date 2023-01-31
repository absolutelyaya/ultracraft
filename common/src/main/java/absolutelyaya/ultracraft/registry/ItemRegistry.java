package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemRegistry
{
	public static final ItemGroup ULTRACRAFT_TAB = CreativeTabRegistry.create(new Identifier(Ultracraft.MOD_ID, "items"),
			() -> new ItemStack(ItemRegistry.BLUE_SKULL.get()));
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Ultracraft.MOD_ID, Registry.ITEM_KEY);
	public static final RegistrySupplier<Item> BLUE_SKULL = ITEMS.register("blue_skull",
			() -> new Item(new Item.Settings().group(ULTRACRAFT_TAB)));
	public static final RegistrySupplier<Item> RED_SKULL = ITEMS.register("red_skull",
			() -> new Item(new Item.Settings().group(ULTRACRAFT_TAB)));
	public static void register()
	{
		ITEMS.register();
	}
}
