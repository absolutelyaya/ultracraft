package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.item.TerminalItem;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TerminalBlock extends BlockWithEntity
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
	
	public TerminalBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder.add(FACING, HALF));
	}
	
	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		World world = ctx.getWorld();
		BlockPos pos = ctx.getBlockPos();
		if(world.getBlockState(pos.up()).canReplace(ctx) && world.getBlockState(pos.up(2)).canReplace(ctx))
			return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(HALF, DoubleBlockHalf.LOWER);
		return null;
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
	{
		super.onPlaced(world, pos, state, placer, itemStack);
		if(state.get(HALF).equals(DoubleBlockHalf.LOWER))
		{
			world.setBlockState(pos.up(),
					BlockRegistry.TERMINAL_DISPLAY.getDefaultState().with(FACING, state.get(FACING)));
			TerminalBlockEntity terminal = (TerminalBlockEntity)world.getBlockEntity(pos.up());
			terminal.base = TerminalItem.getBase(itemStack);
			if(placer.isPlayer())
				terminal.owner = placer.getUuid();
			world.setBlockState(pos.up(2),
					BlockRegistry.TERMINAL.getDefaultState().with(FACING, state.get(FACING)).with(HALF, DoubleBlockHalf.UPPER));
		}
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new TerminalBlockEntity(pos, state);
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		if(state.get(HALF).equals(DoubleBlockHalf.LOWER))
			return VoxelShapes.fullCube();
		return switch(state.get(FACING))
		{
			case NORTH -> VoxelShapes.cuboid(0f, 0f, 4f / 16f, 1f, 8f / 16f, 1f);
			case EAST -> VoxelShapes.cuboid(0f, 0f, 0f, 12f / 16f, 8f / 16f, 1f);
			case SOUTH -> VoxelShapes.cuboid(0f, 0f, 0f, 1f, 8f / 16f, 12f / 16f);
			case WEST -> VoxelShapes.cuboid(4f / 16f, 0f, 0f, 1f, 8f / 16f, 1f);
			default -> VoxelShapes.fullCube(); //shouldn't ever happen
		};
	}
	
	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player)
	{
		Direction dir = state.get(HALF).equals(DoubleBlockHalf.LOWER) ? Direction.UP : Direction.DOWN;
		pos = pos.offset(dir, 1);
		if(world.getBlockState(pos).isOf(BlockRegistry.TERMINAL_DISPLAY))
		{
			BlockEntity be = world.getBlockEntity(pos);
			if(be instanceof TerminalBlockEntity terminal && world.getGameRules().getBoolean(GameruleRegistry.TERMINAL_PROT) && terminal.isCannotBreak(player))
				return;
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			world.addBlockBreakParticles(pos, state);
			world.getBlockState(pos).getBlock().onBreak(world, pos, state, player);
		}
		pos = pos.offset(dir, 1);
		if(world.getBlockState(pos).isOf(BlockRegistry.TERMINAL))
		{
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			world.addBlockBreakParticles(pos, state);
		}
		super.onBreak(world, pos, state, player);
	}
	
	@Override
	public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos)
	{
		Direction dir = state.get(HALF).equals(DoubleBlockHalf.LOWER) ? Direction.UP : Direction.DOWN;
		pos = pos.offset(dir, 1);
		BlockEntity be = world.getBlockEntity(pos);
		if(be instanceof TerminalBlockEntity terminal && player.getWorld().getGameRules().getBoolean(GameruleRegistry.TERMINAL_PROT) &&
				   !terminal.isOwner(player.getUuid()))
			return 0f;
		return super.calcBlockBreakingDelta(state, player, world, pos);
	}
	
	@Override
	public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player)
	{
		Direction dir = state.get(HALF).equals(DoubleBlockHalf.LOWER) ? Direction.UP : Direction.DOWN;
		pos = pos.offset(dir, 1);
		BlockEntity be = world.getBlockEntity(pos);
		if(be instanceof TerminalBlockEntity terminal && world.getGameRules().getBoolean(GameruleRegistry.TERMINAL_PROT) && !terminal.isOwner(player.getUuid()))
		{
			if(!world.isClient)
				player.sendMessage(Text.translatable("message.ultracraft.flamethrower.terminal-prot"));
			return;
		}
		super.onBlockBreakStart(state, world, pos, player);
	}
	
	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state)
	{
		Direction dir = state.get(HALF).equals(DoubleBlockHalf.LOWER) ? Direction.UP : Direction.DOWN;
		pos = pos.offset(dir, 1);
		BlockEntity be = world.getBlockEntity(pos);
		if(be instanceof TerminalBlockEntity terminal)
			return TerminalItem.getStack(terminal.getBase());
		return ItemStack.EMPTY;
	}
	
	@Override
	public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		Direction dir = state.get(HALF).equals(DoubleBlockHalf.LOWER) ? Direction.UP : Direction.DOWN;
		pos = pos.offset(dir, 1);
		BlockEntity be = world.getBlockEntity(pos);
		if(be instanceof TerminalBlockEntity terminal)
			return terminal.getRedstoneStrength();
		return super.getStrongRedstonePower(state, world, pos, direction);
	}
	
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		return getStrongRedstonePower(state, world, pos, direction);
	}
	
	@Override
	public boolean emitsRedstonePower(BlockState state)
	{
		return true;
	}
}
