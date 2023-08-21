package absolutelyaya.ultracraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class FleshBlock extends Block
{
	public FleshBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context)
	{
		return VoxelShapes.cuboid(0.001f, 0.001f, 0.001f, 0.999f, 0.999f, 0.999f);
	}
}
