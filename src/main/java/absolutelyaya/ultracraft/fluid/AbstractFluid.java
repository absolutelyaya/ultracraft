package absolutelyaya.ultracraft.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class AbstractFluid extends FlowableFluid
{
	@Override
	public boolean matchesType(Fluid fluid)
	{
		return getStill() == fluid || getFlowing() == fluid;
	}
	
	@Override
	protected boolean isInfinite(World world)
	{
		return true;
	}
	
	@Override
	protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropStacks(state, world, pos, blockEntity);
	}
	
	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction)
	{
		return false;
	}
	
	@Override
	protected int getFlowSpeed(WorldView world)
	{
		return 4;
	}
	
	@Override
	protected int getLevelDecreasePerBlock(WorldView world)
	{
		return 1;
	}
	
	@Override
	public int getTickRate(WorldView world)
	{
		return 5;
	}
	
	@Override
	protected float getBlastResistance()
	{
		return 100f;
	}
}
