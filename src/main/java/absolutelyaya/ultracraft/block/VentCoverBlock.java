package absolutelyaya.ultracraft.block;

import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class VentCoverBlock extends FacingBlock
{
	public VentCoverBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder.add(FACING));
	}
	
	@Override
	public int getOpacity(BlockState state, BlockView world, BlockPos pos)
	{
		return 4;
	}
	
	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return super.getPlacementState(ctx).with(FACING, ctx.getPlayerLookDirection().getOpposite());
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return switch(state.get(FACING))
		{
			case NORTH -> VoxelShapes.cuboid(0f, 0f, 2f / 16f, 1f, 1f, 4f / 16f);
			case EAST -> VoxelShapes.cuboid(12f / 16f, 0f, 0f, 14f / 16f, 1f, 1f);
			case SOUTH -> VoxelShapes.cuboid(0f, 0f, 12f / 16f, 1f, 1f, 14f / 16f);
			case WEST -> VoxelShapes.cuboid(2f / 16f, 0f, 0f, 4f / 16f, 1f, 1f);
			case UP -> VoxelShapes.cuboid(0f, 12f / 16f, 0f, 1f, 14f / 16f, 1f);
			case DOWN -> VoxelShapes.cuboid(0f, 2f / 16f, 0f, 1f, 4f / 16f, 1f);
		};
	}
}
