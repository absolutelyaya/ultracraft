package absolutelyaya.ultracraft.client.rendering.entity.feature;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class PlayerBackTankFeature<T extends PlayerEntity, M extends PlayerEntityModel<T>> extends FeatureRenderer<T, M>
{
	final BakedModel model;
	
	public PlayerBackTankFeature(FeatureRendererContext<T, M> context)
	{
		super(context);
		model = MinecraftClient.getInstance().getBakedModelManager().getModel(new ModelIdentifier(Ultracraft.MOD_ID, "trinkets/player_back_tank", "inventory"));
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
	{
		if(!(entity instanceof WingedPlayerEntity) || !entity.getMainHandStack().isOf(ItemRegistry.FLAMETHROWER))
			return;
		ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayers.getCutout());
		matrices.push();
		ModelTransform transform = getContextModel().body.getTransform();
		matrices.translate(transform.pivotX / 16f + 0.5f, transform.pivotY / 16f + 0.75, transform.pivotZ / 16f - 0.5f);
		matrices.multiply(new Quaternionf(new AxisAngle4f(transform.pitch, 1f, 0f, 0f)));
		matrices.multiply(new Quaternionf(new AxisAngle4f(transform.yaw, 0f, 1f, 0f)));
		matrices.multiply(new Quaternionf(new AxisAngle4f(transform.roll + 180 * MathHelper.RADIANS_PER_DEGREE, 0f, 0f, 1f)));
		renderer.renderBakedItemModel(model, ItemStack.EMPTY, light, OverlayTexture.DEFAULT_UV, matrices, consumer);
		matrices.pop();
	}
}
