package absolutelyaya.ultracraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;

public class AdornedRailingBlock extends PaneBlock
{
	public static final BooleanProperty FLIPPED = BooleanProperty.of("flipped");
	
	public AdornedRailingBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder);
		builder.add(FLIPPED);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		BlockPos pos = ctx.getBlockPos();
		return super.getPlacementState(ctx).with(FLIPPED, (pos.getX() % 2 + pos.getZ()) % 2 == 0);
	}
}
