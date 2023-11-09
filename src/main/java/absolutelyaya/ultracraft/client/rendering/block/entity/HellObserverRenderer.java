package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.block.HellObserverBlockEntity;
import absolutelyaya.ultracraft.client.gui.screen.HellObserverScreen;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3i;

public class HellObserverRenderer implements BlockEntityRenderer<HellObserverBlockEntity>
{
	@Override
	public void render(HellObserverBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		boolean focused;
		float distance;
		if(client.currentScreen instanceof HellObserverScreen screen && screen.isEditingArea())
		{
			focused = entity.getPos().equals(screen.getObserverPos());
			distance = screen.getObserverPos().getManhattanDistance(entity.getPos());
		}
		else if(client.crosshairTarget instanceof BlockHitResult bHit && client.world.getBlockState(bHit.getBlockPos()).isOf(BlockRegistry.HELL_OBSERVER))
		{
			BlockEntity entity1 = client.world.getBlockEntity(bHit.getBlockPos());
			if(entity1 instanceof HellObserverBlockEntity hellObserver && !hellObserver.shouldPreviewArea())
				return;
			focused = entity.getPos().equals(bHit.getBlockPos());
			distance = bHit.getBlockPos().getManhattanDistance(entity.getPos());
		}
		else return;
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.LINES); //POSITION COLOR NORMAL PADDING
		Vec3i size = entity.getCheckDimensions();
		Vec3i offset = entity.getCheckOffset();
		
		matrices.push();
		matrices.translate(offset.getX() - Math.floor(size.getX() / 2f), offset.getY() - Math.floor(size.getY() / 2f), offset.getZ() - Math.floor(size.getZ() / 2f));
		if(!focused)
		{
			float f = 1f - distance / 100f;
			matrices.scale(f, f, f);
		}
		WorldRenderer.drawBox(matrices, consumer, 0, 0, 0, size.getX(), size.getY(), size.getZ(), 1f, 1f, 1f,
				focused ? 1f : 0.2f);
		matrices.pop();
	}
}
