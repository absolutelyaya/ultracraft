package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class PipeBlock extends PillarBlock
{
	public static final IntProperty TYPE = IntProperty.of("type", 0, 3);
	
	public PipeBlock(Settings settings)
	{
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(TYPE, 0));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder);
		builder.add(TYPE);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		BlockState state = super.getPlacementState(ctx);
		return getStateForPos(state, ctx.getBlockPos(), ctx.getWorld());
	}
	
	BlockState getStateForPos(BlockState state, BlockPos pos, WorldAccess world)
	{
		BlockState positive = world.getBlockState(pos.add(Direction.from(state.get(AXIS), Direction.AxisDirection.POSITIVE).getVector()));
		BlockState negative = world.getBlockState(pos.add(Direction.from(state.get(AXIS), Direction.AxisDirection.NEGATIVE).getVector()));
		int type = 0;
		if(positive.isOf(BlockRegistry.RUSTY_PIPE) && negative.isOf(BlockRegistry.RUSTY_PIPE))
		{
			if(positive.get(TYPE) == 3 && negative.get(TYPE) == 2)
				type = 1;
			else if((positive.get(TYPE) == 1 || negative.get(TYPE) == 3) && negative.get(TYPE) == 2)
				type = 2;
			else if((positive.get(TYPE) == 2 && negative.get(TYPE) == 2) ||
							((positive.get(TYPE) == 1 || positive.get(TYPE) == 2) && negative.get(TYPE) == 3))
				type = 2;
			else if(positive.get(TYPE) == 2 && negative.get(TYPE) <= 1)
				type = 3;
		}
		else if(positive.isOf(BlockRegistry.RUSTY_PIPE))
		{
			if(positive.get(TYPE) == 0 || positive.get(TYPE) == 3)
				type = world.getRandom().nextFloat() < 0.25 ? 0 : 1;
			else if(positive.get(TYPE) == 1 || positive.get(TYPE) == 2)
				type = world.getRandom().nextFloat() < 0.5 ? 3 : 2;
		}
		else if(negative.isOf(BlockRegistry.RUSTY_PIPE))
		{
			if(negative.get(TYPE) <= 1)
				type = world.getRandom().nextFloat() < 0.25 ? 0 : 3;
			else if(negative.get(TYPE) == 3 || negative.get(TYPE) == 2)
				type = world.getRandom().nextFloat() < 0.5 ? 1 : 2;
		}
		state = state.with(TYPE, type);
		return state;
	}
}
