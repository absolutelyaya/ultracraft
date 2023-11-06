package absolutelyaya.ultracraft.client.rendering.entity.feature.gecko;

import absolutelyaya.ultracraft.client.rendering.entity.feature.EnragedFeature;
import absolutelyaya.ultracraft.entity.demon.HideousMassEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class HideousMassRageLayer extends GeoRenderLayer<HideousMassEntity>
{
	final EnragedFeature<HideousMassEntity> rage;
	
	public HideousMassRageLayer(GeoRenderer<HideousMassEntity> entityRendererIn)
	{
		super(entityRendererIn);
		rage = new EnragedFeature<>(MinecraftClient.getInstance().getEntityModelLoader());
	}
	
	@Override
	public void render(MatrixStack poseStack, HideousMassEntity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		rage.render(poseStack, bufferSource, packedLight, animatable, 0f, 0f, partialTick, 0f, animatable.headYaw, animatable.getPitch());
	}
}
