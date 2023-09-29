package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class HellObserverBlockEntity extends BlockEntity
{
	public HellObserverBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.HELL_OBSERVER, pos, state);
	}
}
