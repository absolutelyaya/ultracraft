package absolutelyaya.ultracraft.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class InventoryUtil
{
	public static boolean containsItem(DefaultedList<ItemStack> inv, Item item, int count)
	{
		int found = 0;
		for (ItemStack stack : inv)
		{
			if(stack.isOf(item))
			{
				found += stack.getCount();
				if(found >= count)
					return true;
			}
		}
		return false;
	}
}
