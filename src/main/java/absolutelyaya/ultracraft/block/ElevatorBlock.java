package absolutelyaya.ultracraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class ElevatorBlock extends Block
{
	public static final IntProperty LAYER = IntProperty.of("layer", 1, 3);
	public static final BooleanProperty SIDE = BooleanProperty.of("side");
	
	public ElevatorBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(LAYER, SIDE);
	}
	
	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		BlockState state = super.getPlacementState(ctx);
		return getStateForPos(state, ctx.getBlockPos(), ctx.getWorld());
	}
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		return getStateForPos(state, pos, world);
	}
	
	BlockState getStateForPos(BlockState state, BlockPos pos, WorldAccess world)
	{
		state = state.with(LAYER, 3 - (pos.getY() % 3));
		state = state.with(SIDE, getSide(pos, world));
		return state;
	}
	
	boolean getSide(BlockPos pos, WorldAccess world)
	{
		if (world.getBlockState(pos.east()).isOf(this) && !world.getBlockState(pos.east()).get(SIDE))
			return true;
		return (world.getBlockState(pos.north()).isOf(this) && !world.getBlockState(pos.north()).get(SIDE));
	}
}
