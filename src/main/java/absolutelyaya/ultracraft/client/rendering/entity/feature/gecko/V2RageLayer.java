package absolutelyaya.ultracraft.client.rendering.entity.feature.gecko;

import absolutelyaya.ultracraft.client.rendering.entity.feature.EnragedFeature;
import absolutelyaya.ultracraft.entity.machine.V2Entity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;

public class V2RageLayer extends GeoRenderLayer<V2Entity>
{
	final EnragedFeature<V2Entity> rage;
	
	public V2RageLayer(GeoRenderer<V2Entity> entityRendererIn)
	{
		super(entityRendererIn);
		rage = new EnragedFeature<>(MinecraftClient.getInstance().getEntityModelLoader());
	}
	
	@Override
	public void render(MatrixStack poseStack, V2Entity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		rage.render(poseStack, bufferSource, packedLight, animatable, 0f, 0f, partialTick, 0f, animatable.headYaw, animatable.getPitch());
	}
}
