package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.block.SkyBlockEntity;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class SkyBlockItem extends BlockItem
{
	public SkyBlockItem(Settings settings)
	{
		super(BlockRegistry.SKY_BLOCK, settings);
	}
	
	public static ItemStack getStack(SkyBlockEntity.SkyType type)
	{
		ItemStack stack = new ItemStack(ItemRegistry.SKY);
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putString("type", type.toString().toLowerCase());
		nbt.putInt("CustomModelData", type.ordinal() + 1);
		stack.setNbt(nbt);
		return stack;
	}
}
