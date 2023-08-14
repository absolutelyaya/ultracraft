package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.registry.TagRegistry;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.*;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin
{
	@Shadow @Final public static IntProperty LEVEL;
	
	@Shadow @Final protected FlowableFluid fluid;
	
	@Shadow @Final public static ImmutableList<Direction> FLOW_DIRECTIONS;
	
	@Shadow protected abstract void playExtinguishSound(WorldAccess world, BlockPos pos);
	
	@Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
	void onCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir)
	{
		if(state.getFluidState().isIn(TagRegistry.UNSKIMMABLE_FLUIDS))
			return;
		VoxelShape collisionShape = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.5, 16.0);
		cir.setReturnValue(context.isAbove(collisionShape, pos, true) && state.get(LEVEL) == 0 &&
								   context.canWalkOnFluid(world.getFluidState(pos.up()), state.getFluidState()) ? collisionShape : VoxelShapes.empty());
	}
	
	@Inject(method = "receiveNeighborFluids", at = @At("HEAD"), cancellable = true)
	void onReceiveNeighborFluids(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir)
	{
		FluidState self = world.getFluidState(pos);
		if(self.isIn(FluidTags.LAVA))
		{
			FLOW_DIRECTIONS.forEach(dir -> {
				BlockPos pos1 = pos.offset(dir);
				if(world.getFluidState(pos1).isIn(TagRegistry.BLOOD_FLUID))
				{
					world.setBlockState(pos1, Blocks.NETHER_WART_BLOCK.getDefaultState()); //TODO: Replace with flesh
					playExtinguishSound(world, pos);
					cir.setReturnValue(false);
				}
			});
		}
		else if(self.isIn(FluidTags.WATER) && !self.isIn(TagRegistry.BLOOD_FLUID))
		{
			FLOW_DIRECTIONS.forEach(dir -> {
				BlockPos pos1 = pos.offset(dir);
				if(world.getFluidState(pos1).isIn(TagRegistry.BLOOD_FLUID))
				{
					world.setBlockState(pos1, Blocks.NETHERRACK.getDefaultState());
					playExtinguishSound(world, pos);
					cir.setReturnValue(false);
				}
			});
		}
	}
}
