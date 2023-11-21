package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.item.SkyBlockItem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SkyBlock extends BlockWithEntity
{
	public SkyBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	@Override
	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos)
	{
		return 1f;
	}
	
	@Override
	public int getOpacity(BlockState state, BlockView world, BlockPos pos)
	{
		return 0;
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new SkyBlockEntity(pos, state);
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
	{
		super.onPlaced(world, pos, state, placer, itemStack);
		if(world.getBlockEntity(pos) instanceof SkyBlockEntity sky && itemStack.hasNbt())
		{
			NbtCompound nbt = itemStack.getNbt();
			if(nbt.contains("type", NbtElement.STRING_TYPE))
				sky.type = SkyBlockEntity.SkyType.valueOf(nbt.getString("type").toUpperCase());
		}
	}
	
	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state)
	{
		if(world.getBlockEntity(pos) instanceof SkyBlockEntity sky)
			return SkyBlockItem.getStack(sky.type);
		return super.getPickStack(world, pos, state);
	}
}
