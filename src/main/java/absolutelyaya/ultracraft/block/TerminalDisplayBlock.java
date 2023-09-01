package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.item.TerminalItem;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public class TerminalDisplayBlock extends BlockWithEntity
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty GLOWS = BooleanProperty.of("glows");
	
	public TerminalDisplayBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder.add(FACING, GLOWS));
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
		return TerminalItem.getStack(((TerminalBlockEntity)world.getBlockEntity(pos)).getBase());
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
		BlockEntity entity = world.getBlockEntity(pos);
		if(entity instanceof TerminalBlockEntity terminal)
			terminal.onBlockBreak();
		super.onBreak(world, pos, state, player);
		//TODO: manually drop item with all NBT data
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(player instanceof WingedPlayerEntity winged)
		{
			BlockEntity entity = world.getBlockEntity(pos);
			if(entity instanceof TerminalBlockEntity terminal)
			{
				if(winged.getFocusedTerminal() == null || !winged.getFocusedTerminal().equals(terminal))
				{
					winged.setFocusedTerminal(terminal);
					terminal.focusedPlayers.add(winged);
				}
				if(world.isClient)
					MinecraftClient.getInstance().gameRenderer.setRenderHand(false);
			}
		}
		return super.onUse(state, world, pos, player, hand, hit);
	}
	
	public void onPoint(World world, BlockPos pos, BlockHitResult hit, PlayerEntity player)
	{
		if(!world.isClient)
			return;
		Direction dir = world.getBlockState(pos).get(FACING);
		Direction.Axis axis = dir.getAxis();
		Vec3d p = hit.getPos();
		Vec3d local = hit.getBlockPos().toCenterPos().subtract(p);
		if((axis.equals(Direction.Axis.X) && local.x != 0f) || (axis.equals(Direction.Axis.Z) && local.z != 0f))
			return; //the front face wasn't hit.
		BlockEntity entity = world.getBlockEntity(pos);
		if(entity instanceof TerminalBlockEntity terminalEntity)
		{
			double screenX = axis == Direction.Axis.X ? local.z + 0.5f : local.x + 0.5f;
			if((dir.getDirection().equals(Direction.AxisDirection.POSITIVE) && axis.equals(Direction.Axis.X)) ||
					   (dir.getDirection().equals(Direction.AxisDirection.NEGATIVE) && axis.equals(Direction.Axis.Z)))
				terminalEntity.setCursor(new Vector2d((float)screenX, (float)local.y + 0.5f));
			else
				terminalEntity.setCursor(new Vector2d(1f - (float)screenX, (float)local.y + 0.5f));
		}
	}
	
	public void onHit(World world, BlockPos pos, BlockHitResult hit, PlayerEntity player)
	{
		if(!world.isClient)
			return;
		Direction.Axis axis = world.getBlockState(pos).get(FACING).getAxis();
		Vec3d p = hit.getPos();
		Vec3d local = hit.getBlockPos().toCenterPos().subtract(p);
		if((axis.equals(Direction.Axis.X) && local.x != 0f) || (axis.equals(Direction.Axis.Z) && local.z != 0f))
			return; //the front face wasn't hit.
		BlockEntity entity = world.getBlockEntity(pos);
		if(entity instanceof TerminalBlockEntity terminalEntity)
			terminalEntity.onHit();
	}
}
