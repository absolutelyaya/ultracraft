package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PedestalBlock extends BlockWithEntity implements IPunchableBlock, BlockEntityProvider
{
	public static final EnumProperty<Type> TYPE = EnumProperty.of("type", Type.class);
	
	public PedestalBlock(Settings settings)
	{
		super(settings);
	}
	
	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return super.getDefaultState().with(TYPE, Type.NONE);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(TYPE);
	}
	
	@Override
	public boolean onPunch(PlayerEntity puncher, BlockPos pos)
	{
		BlockEntity blockEntity = puncher.world.getBlockEntity(pos);
		if(blockEntity instanceof PedestalBlockEntity pedestal)
		{
			boolean result = pedestal.onPunch(puncher);
			if(!puncher.world.isClient)
				puncher.world.emitGameEvent(puncher, GameEvent.BLOCK_CHANGE, pos);
			puncher.world.updateNeighbors(pos, this);
			return result;
		}
		return false;
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new PedestalBlockEntity(pos, state);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.getBlock() != newState.getBlock())
		{
			BlockEntity entity = world.getBlockEntity(pos);
			if(entity instanceof PedestalBlockEntity pedestal)
			{
				ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), pedestal.getStack());
			}
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
	
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public boolean emitsRedstonePower(BlockState state)
	{
		return true;
	}
	
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if(blockEntity instanceof PedestalBlockEntity pedestal)
		{
			ItemStack stack = pedestal.getStack();
			boolean b = state.get(TYPE).equals(Type.BLUE) && stack.getItem().equals(ItemRegistry.BLUE_SKULL.get()) ||
								state.get(TYPE).equals(Type.RED) && stack.getItem().equals(ItemRegistry.RED_SKULL.get());
			return b ? 15 : 0;
		}
		return 0;
	}
	
	@Override
	public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction)
	{
		return getWeakRedstonePower(state, world, pos, direction);
	}
	
	enum Type implements StringIdentifiable
	{
		NONE("none"),
		BLUE("blue"),
		RED("red");
		
		final String name;
		Type(String name)
		{
			this.name = name;
		}
		
		@Override
		public String asString()
		{
			return name;
		}
	}
}
