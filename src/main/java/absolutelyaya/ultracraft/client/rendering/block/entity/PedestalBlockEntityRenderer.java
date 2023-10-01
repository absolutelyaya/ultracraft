package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.block.PedestalBlock;
import absolutelyaya.ultracraft.block.PedestalBlockEntity;
import absolutelyaya.ultracraft.item.*;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class PedestalBlockEntityRenderer implements BlockEntityRenderer<PedestalBlockEntity>
{
	final ItemRenderer renderer;
	
	public PedestalBlockEntityRenderer(BlockEntityRendererFactory.Context context)
	{
		renderer = context.getItemRenderer();
	}
	
	@Override
	public void render(PedestalBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay)
	{
		ItemStack stack = entity.getHeld();
		if(!stack.isEmpty())
		{
			matrices.push();
			matrices.translate(0.5f, 1.25f, 0.5f);
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(
					entity.getCachedState().get(PedestalBlock.FACING).getOpposite().asRotation()), 0f, -1f, 0f)));
			if(entity.isFancy())
				applyFloatTransformation(matrices);
			applyCustomTransformations(stack.getItem(), matrices);
			renderer.renderItem(stack, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
			matrices.pop();
		}
	}
	
	void applyCustomTransformations(Item item, MatrixStack matrices)
	{
		if(item instanceof PlushieItem)
		{
			float scaleInverse = 0.75f / 1.5f;
			matrices.scale(scaleInverse, scaleInverse, scaleInverse);
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(90), 1f, 0f, 0f)));
			matrices.translate(0f, 0.2f, 0.4f);
		}
		else if (item instanceof AbstractWeaponItem)
		{
			if(item instanceof AbstractRevolverItem)
			{
				matrices.translate(0f, -0.125f, 0f);
				matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(10), 0f, 0f, 1f)));
			}
			if(item instanceof AbstractShotgunItem)
			{
				matrices.translate(-0.075f, -0.055f, 0f);
				matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(15), 0f, 0f, 1f)));
			}
			if(item instanceof AbstractNailgunItem)
			{
				matrices.translate(0.15f, -0.1f, 0f);
				matrices.scale(1.1f, 1.1f, 1.1f);
				matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(5), 0f, 0f, 1f)));
			}
		}
		else if (item.equals(ItemRegistry.MACHINE_SWORD))
		{
			matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(180), 0f, 0f, 1f)));
			matrices.scale(1.75f, 1.75f, 1.75f);
			matrices.translate(0f, -0.1f, 0f);
		}
		else if (!(item instanceof BlockItem) && !(item.equals(ItemRegistry.BLUE_SKULL) || item.equals(ItemRegistry.RED_SKULL)))
		{
			matrices.scale(0.75f, 0.75f, 0.75f);
			if(item instanceof SwordItem)
				matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(135), 0f, 0f, 1f)));
			else
				matrices.translate(0f, 0.1f, 0f);
		}
	}
	
	void applyFloatTransformation(MatrixStack matrices)
	{
		long time = MinecraftClient.getInstance().world.getTime() % 189;
		matrices.scale(0.75f, 0.75f, 0.75f);
		matrices.translate(0f, Math.sin(time / 30f) * 0.2 + 0.1f, 0f);
		matrices.multiply(new Quaternionf(new AxisAngle4f(time / 30f, 0f, 1f, 0f)));
	}
}
