package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.entity.demon.CerberusEntity;
import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class CerberusBlock extends HorizontalFacingBlock implements BlockEntityProvider
{
	public static final BooleanProperty EMPTY = BooleanProperty.of("empty");
	public static final BooleanProperty SPAWNING = BooleanProperty.of("spawning");
	public static final IntProperty PROXIMITY = IntProperty.of("proximity", 0, 64);
	
	public CerberusBlock(Settings settings)
	{
		super(settings);
		setDefaultState(getDefaultState().with(SPAWNING, false).with(EMPTY, false).with(PROXIMITY, 0));
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return BlockEntityRegistry.CERBERUS.instantiate(pos, state);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, EMPTY, SPAWNING, PROXIMITY);
	}
	
	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		Direction dir = context.getPlayerLookDirection().getOpposite();
		if(dir == Direction.DOWN || dir == Direction.UP)
			dir = Direction.fromRotation(context.getPlayerYaw()).getOpposite();
		return getDefaultState().with(FACING, dir).with(EMPTY, false).with(SPAWNING, false);
	}
	
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
	{
		super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
		if(world.isReceivingRedstonePower(pos) && !state.get(EMPTY) && !state.get(SPAWNING))
		{
			world.setBlockState(pos, state.with(SPAWNING, true), Block.NOTIFY_LISTENERS);
			world.scheduleBlockTick(pos, this, 60);
		}
	}
	
	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		super.scheduledTick(state, world, pos, random);
		if(state.get(SPAWNING))
		{
			world.setBlockState(pos, state.with(SPAWNING, false).with(EMPTY, true), Block.NOTIFY_LISTENERS);
			CerberusEntity cerb = new CerberusEntity(EntityRegistry.CERBERUS, world);
			Direction dir = state.get(FACING);
			BlockPos spawnPos = pos.add(dir.getVector());
			cerb.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 0.1, spawnPos.getZ() + 0.5);
			cerb.setRotation(dir.asRotation());
			world.spawnEntity(cerb);
		}
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public boolean emitsRedstonePower(BlockState state)
	{
		return true;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return switch (state.get(FACING))
		{
			default -> VoxelShapes.union(VoxelShapes.cuboid(0f, 0f, 0f, 1f, 1f, 0.5f),
					VoxelShapes.cuboid(0.5f, 0f, 0.5f, 1f, 0.5f, 1f));
			case EAST -> VoxelShapes.union(VoxelShapes.cuboid(0f, 0f, 0f, 0.5f, 1f, 1f),
					VoxelShapes.cuboid(0.5f, 0f, 0f, 1f, 0.5f, 0.5f));
			case NORTH -> VoxelShapes.union(VoxelShapes.cuboid(0f, 0f, 0.5f, 1f, 1f, 1f),
					VoxelShapes.cuboid(0f, 0f, 0f, 0.5f, 0.5f, 0.5f));
			case WEST -> VoxelShapes.union(VoxelShapes.cuboid(0.5f, 0f, 0f, 1f, 1f, 1f),
					VoxelShapes.cuboid(0f, 0f, 0.5f, 0.5f, 0.5f, 1f));
		};
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return getCollisionShape(state, world, pos, context);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options)
	{
		tooltip.add(Text.translatable("block.ultracraft.cerberus_block.lore"));
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return CerberusBlockEntity::tick;
	}
	
	public ItemStack getProximityStack()
	{
		ItemStack stack = new ItemStack(asItem());
		stack.setCustomName(Text.translatable("block.ultracraft.cerberus_block.proximity").getWithStyle(Style.EMPTY.withItalic(false)).get(0));
		NbtCompound nbt = stack.getOrCreateNbt();
		NbtCompound compound = new NbtCompound();
		compound.putInt("proximity", 16);
		nbt.put("BlockEntityTag", compound);
		stack.setNbt(nbt);
		return stack;
	}
}
