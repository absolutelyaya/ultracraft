package absolutelyaya.ultracraft.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;

public class SkullItem extends Item implements Equipment
{
	public SkullItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public EquipmentSlot getSlotType()
	{
		return EquipmentSlot.HEAD;
	}
}
