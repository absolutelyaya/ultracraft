package absolutelyaya.ultracraft.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

public class MultiColorSpawnEggItem extends SpecialSpawnEggItem
{
	int[] colors;
	
	public MultiColorSpawnEggItem(EntityType<? extends MobEntity> type, int[] colors, Settings settings)
	{
		super(type, 0, 0, settings);
		this.colors = colors;
	}
	
	@Override
	public int getColor(int tintIndex)
	{
		return colors[tintIndex % colors.length];
	}
}
