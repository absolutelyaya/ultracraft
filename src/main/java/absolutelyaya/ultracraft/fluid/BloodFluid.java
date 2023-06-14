package absolutelyaya.ultracraft.fluid;

import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.FluidRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class BloodFluid extends AbstractFluid
{
	@Override
	public Fluid getStill()
	{
		return FluidRegistry.STILL_BLOOD;
	}
	
	@Override
	public Fluid getFlowing()
	{
		return FluidRegistry.Flowing_BLOOD;
	}
	
	@Override
	protected BlockState toBlockState(FluidState state)
	{
		return BlockRegistry.BLOOD.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
	}
	
	@Override
	public Optional<SoundEvent> getBucketFillSound()
	{
		return Optional.ofNullable(SoundEvents.ITEM_BUCKET_FILL);
	}
	
	@Override
	public Item getBucketItem()
	{
		return ItemRegistry.BLOOD_BUCKET;
	}
	
	public static class Still extends BloodFluid
	{
		
		@Override
		public boolean isStill(FluidState state)
		{
			return true;
		}
		
		@Override
		public int getLevel(FluidState state)
		{
			return 8;
		}
	}
	
	public static class Flowing extends BloodFluid
	{
		@Override
		protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder)
		{
			super.appendProperties(builder);
			builder.add(LEVEL);
		}
		
		@Override
		public boolean isStill(FluidState state)
		{
			return false;
		}
		
		@Override
		public int getLevel(FluidState state)
		{
			return state.get(LEVEL);
		}
	}
}
