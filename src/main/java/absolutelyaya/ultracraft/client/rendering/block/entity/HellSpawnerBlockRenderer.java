package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.block.HellSpawnerBlock;
import absolutelyaya.ultracraft.block.HellSpawnerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.renderer.GeoBlockRenderer;

import java.util.Optional;

public class HellSpawnerBlockRenderer extends GeoBlockRenderer<HellSpawnerBlockEntity>
{
	final ItemRenderer renderer;
	
	public HellSpawnerBlockRenderer()
	{
		super(new HellSpawnerBlockModel());
		renderer = MinecraftClient.getInstance().getItemRenderer();
	}
	
	@Override
	public void actuallyRender(MatrixStack poseStack, HellSpawnerBlockEntity animatable, BakedGeoModel model, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		BlockState state = animatable.getWorld().getBlockState(animatable.getPos());
		Optional<Direction> dir = state.getOrEmpty(HellSpawnerBlock.FACING);
		if(dir.isEmpty())
			return;
		poseStack.push();
		if(dir.get() == Direction.UP)
			poseStack.translate(0, 0.5, -0.5);
		if(dir.get() == Direction.DOWN)
			poseStack.translate(0, 0.5, 0.5);
		super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		if(animatable.hasSpawnStack())
		{
			poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(dir.get().asRotation()));
			if(dir.get() == Direction.UP)
			{
				poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
				poseStack.translate(1.5, -1.0, -0.5);
			}
			if(dir.get() == Direction.DOWN)
			{
				poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
				poseStack.translate(0.5, -1.0, 0.5);
			}
			if(dir.get() == Direction.EAST)
				poseStack.translate(1, 0, -1);
			if(dir.get() == Direction.SOUTH)
				poseStack.translate(1, 0, 1);
			poseStack.translate(-0.5, 0.5, 0);
			poseStack.scale(0.5f, 0.5f, 0.5f);
			renderer.renderItem(animatable.getSpawnStack(), ModelTransformationMode.FIXED, packedLight, packedOverlay,
					poseStack, bufferSource, animatable.getWorld(), 0);
		}
		poseStack.pop();
	}
}
