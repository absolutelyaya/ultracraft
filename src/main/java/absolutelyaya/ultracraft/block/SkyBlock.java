package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SkyBlock extends Block //TODO: actually render a skybox
{
	public SkyBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options)
	{
		tooltip.add(Text.translatable("block.ultracraft.sky_block.lore"));
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.INVISIBLE;
	}
	
	@Override
	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos)
	{
		return 1f;
	}
	
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos blockPos, Random random)
	{
		if(world.isClient && MinecraftClient.getInstance().player.getMainHandStack().isOf(BlockRegistry.SKY_BLOCK.asItem()))
		{
			Vec3d pos = blockPos.toCenterPos();
			world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK_MARKER, state), pos.x, pos.y, pos.z, 0, 0, 0);
		}
	}
	
	@Override
	public int getOpacity(BlockState state, BlockView world, BlockPos pos)
	{
		return 0;
	}
}
