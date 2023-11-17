package absolutelyaya.ultracraft.client.rendering.entity.feature.gecko;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.machine.DroneEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;

public class DroneEmissiveLayer extends GeoRenderLayer<DroneEntity>
{
	private static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/entity/drone_emissive.png");
	private static final Identifier TEXTURE2 = new Identifier(Ultracraft.MOD_ID, "textures/entity/drone2_emissive.png");
	
	public DroneEmissiveLayer(GeoRenderer<DroneEntity> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	@Override
	public void render(MatrixStack poseStack, DroneEntity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		RenderLayer armorRenderType = RenderLayer.getEntityTranslucentEmissive(animatable.isAttacking() ? TEXTURE2 : TEXTURE);
		
		getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, armorRenderType,
				bufferSource.getBuffer(armorRenderType), partialTick, packedLight, OverlayTexture.DEFAULT_UV,
				1, 1, 1, 1);
	}
}
