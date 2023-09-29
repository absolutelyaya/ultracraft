package absolutelyaya.ultracraft.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class HellObserverBlock extends BlockWithEntity implements BlockEntityProvider
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final IntProperty ACTIVE = IntProperty.of("active", 0, 2);
	
	public HellObserverBlock(Settings settings)
	{
		super(settings);
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(ACTIVE, 0));
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new HellObserverBlockEntity(pos, state);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, ACTIVE);
	}
	
	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return super.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(ACTIVE, 0);
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
}
