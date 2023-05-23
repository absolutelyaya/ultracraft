package absolutelyaya.ultracraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class ElevatorFloorBlock extends ElevatorBlock
{
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	
	public ElevatorFloorBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(SIDE, FACING);
	}
	
	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		BlockState state = getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing()).with(SIDE, false);
		return getStateForPos(state, ctx.getBlockPos(), ctx.getWorld());
	}
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		return getStateForPos(state, pos, world);
	}
	
	@Override
	BlockState getStateForPos(BlockState state, BlockPos pos, WorldAccess world)
	{
		BlockState check = switch(state.get(FACING)) {
			case DOWN, UP -> null;
			case NORTH, SOUTH -> world.getBlockState(pos.west());
			case WEST, EAST -> world.getBlockState(pos.north());
		};
		state = state.with(SIDE, check.isOf(this) && check.get(FACING).equals(state.get(FACING)) && !check.get(SIDE));
		return state;
	}
}
