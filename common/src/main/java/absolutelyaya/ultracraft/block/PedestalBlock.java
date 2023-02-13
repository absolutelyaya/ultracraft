package absolutelyaya.ultracraft.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PedestalBlock extends BlockWithEntity implements IPunchableBlock
{
	public PedestalBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public boolean onPunch(PlayerEntity puncher, BlockPos pos)
	{
		BlockEntity blockEntity = puncher.world.getBlockEntity(pos);
		if(blockEntity instanceof PedestalBlockEntity pedestal)
		{
			boolean result = pedestal.onPunch(puncher);
			puncher.world.emitGameEvent(puncher, GameEvent.BLOCK_CHANGE, pos);
			return result;
		}
		return false;
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new PedestalBlockEntity(pos, state);
	}
	
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
}
