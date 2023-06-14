package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.registry.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public class MixinFluidBlock
{
	@Shadow @Final public static IntProperty LEVEL;
	
	@Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
	void onCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir)
	{
		if(state.getFluidState().isIn(TagRegistry.UNSKIMMABLE_FLUIDS))
			return;
		VoxelShape collisionShape = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.5, 16.0);
		cir.setReturnValue(context.isAbove(collisionShape, pos, true) && state.get(LEVEL) == 0 &&
								   context.canWalkOnFluid(world.getFluidState(pos.up()), state.getFluidState()) ? collisionShape : VoxelShapes.empty());
	}
}
