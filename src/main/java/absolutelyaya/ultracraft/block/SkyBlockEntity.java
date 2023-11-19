package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

public class SkyBlockEntity extends BlockEntity
{
	SkyType type = SkyType.EVENING;
	
	public SkyBlockEntity( BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.SKY, pos, state);
	}
	
	public SkyType getSkyType()
	{
		return type;
	}
	
	enum SkyType
	{
		DAY,
		EVENING,
		NIGHT;
		
		@Override
		public String toString()
		{
			return super.toString().toLowerCase();
		}
	}
}
