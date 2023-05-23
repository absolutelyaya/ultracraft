package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockRegistry;
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
		state = state.with(LAYER, state.isOf(BlockRegistry.ELEVATOR_FLOOR) ? 0 : getLayer(pos, world))
						.with(SIDE, getSide(pos, world));
		return state;
	}
	
	int getLayer(BlockPos pos, WorldAccess world)
	{
		if(world.getBlockState(pos.down()).isOf(this))
		{
			int i = world.getBlockState(pos.down()).get(LAYER) - 1;
			return i == 0 ? 3 : i;
		}
		return 3;
	}
	
	boolean getSide(BlockPos pos, WorldAccess world)
	{
		if (world.getBlockState(pos.east()).isOf(this) && !world.getBlockState(pos.east()).get(SIDE))
			return true;
		return (world.getBlockState(pos.north()).isOf(this) && !world.getBlockState(pos.north()).get(SIDE));
	}
}
