package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TerminalDisplayBlock extends BlockWithEntity
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	
	public TerminalDisplayBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder.add(FACING));
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new TerminalBlockEntity(pos, state);
	}
	
	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state)
	{
		return ItemRegistry.TERMINAL.getDefaultStack();
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return switch(state.get(FACING))
		{
			case NORTH -> VoxelShapes.cuboid(0f, 0f, 0.5f, 1f, 1f, 1f);
			case EAST -> VoxelShapes.cuboid(0f, 0f, 0f, 0.5f, 1f, 1f);
			case SOUTH -> VoxelShapes.cuboid(0f, 0f, 0f, 1f, 1f, 0.5f);
			case WEST -> VoxelShapes.cuboid(0.5f, 0f, 0f, 1f, 1f, 1f);
			default -> VoxelShapes.fullCube(); //shouldn't ever happen
		};
	}
	
	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player)
	{
		BlockPos pos2 = pos.offset(Direction.UP, 1);
		BlockState state2 = world.getBlockState(pos2);
		if(state2.isOf(BlockRegistry.TERMINAL) && state2.get(TerminalBlock.HALF).equals(DoubleBlockHalf.UPPER))
		{
			world.setBlockState(pos2, Blocks.AIR.getDefaultState());
			world.addBlockBreakParticles(pos2, state);
		}
		pos2 = pos.offset(Direction.DOWN, 1);
		state2 = world.getBlockState(pos2);
		if(state2.isOf(BlockRegistry.TERMINAL) && state2.get(TerminalBlock.HALF).equals(DoubleBlockHalf.LOWER))
		{
			world.setBlockState(pos2, Blocks.AIR.getDefaultState());
			world.addBlockBreakParticles(pos2, state);
		}
		super.onBreak(world, pos, state, player);
	}
}
