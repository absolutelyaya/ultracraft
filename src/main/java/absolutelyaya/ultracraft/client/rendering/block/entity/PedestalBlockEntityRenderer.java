package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.block.PedestalBlockEntity;
import absolutelyaya.ultracraft.item.PlushieItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class PedestalBlockEntityRenderer implements BlockEntityRenderer<PedestalBlockEntity>
{
	ItemRenderer renderer;
	
	public PedestalBlockEntityRenderer(BlockEntityRendererFactory.Context context)
	{
		renderer = context.getItemRenderer();
	}
	
	@Override
	public void render(PedestalBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay)
	{
		ItemStack stack = entity.getStack();
		if(!stack.isEmpty())
		{
			matrices.push();
			matrices.translate(0.5f, 1.25f, 0.5f);
			if(stack.getItem() instanceof PlushieItem)
			{
				float scaleInverse = 0.75f / 1.5f;
				matrices.scale(scaleInverse, scaleInverse, scaleInverse);
				matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(90), 1f, 0f, 0f)));
				matrices.translate(0f, 0.2f, 0.4f);
			}
			renderer.renderItem(stack, ModelTransformation.Mode.FIXED, light, overlay, matrices, vertexConsumers, 0);
			matrices.pop();
		}
	}
}
