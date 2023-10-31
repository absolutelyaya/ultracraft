package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class HellSpawnerBlock extends BlockWithEntity implements BlockEntityProvider
{
	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
	
	public HellSpawnerBlock(Settings settings)
	{
		super(settings);
		setDefaultState(getDefaultState().with(FACING, Direction.UP).with(TRIGGERED, false));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, TRIGGERED);
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new HellSpawnerBlockEntity(pos, state);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(player.isSneaking() || !(world.getBlockEntity(pos) instanceof HellSpawnerBlockEntity spawner))
			return super.onUse(state, world, pos, player, hand, hit);
		ItemStack stack = player.getStackInHand(hand);
		if(stack.isEmpty() && spawner.hasSpawnStack())
		{
			spawner.setSpawnStack(null);
			return ActionResult.SUCCESS;
		}
		if(stack.getItem() instanceof SpawnEggItem)
		{
			 spawner.setSpawnStack(stack);
			 return ActionResult.SUCCESS;
		}
		return super.onUse(state, world, pos, player, hand, hit);
	}
	
	@Override
	public boolean emitsRedstonePower(BlockState state)
	{
		return true;
	}
	
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
	{
		if(world.isClient)
			return;
		boolean powered = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
		if(!state.get(TRIGGERED) && powered)
		{
			if(world instanceof ServerWorld serverWorld && world.getBlockEntity(pos) instanceof HellSpawnerBlockEntity spawner)
				spawner.spawn(serverWorld, pos, state);
		}
		world.setBlockState(pos, state.with(TRIGGERED, powered));
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public Item asItem()
	{
		return ItemRegistry.HELL_SPAWNER;
	}
}
