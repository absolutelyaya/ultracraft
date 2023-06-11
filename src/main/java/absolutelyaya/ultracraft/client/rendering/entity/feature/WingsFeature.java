package absolutelyaya.ultracraft.client.rendering.entity.feature;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class WingsFeature<T extends PlayerEntity, M extends PlayerEntityModel<T>> extends FeatureRenderer<T, M>
{
	private static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/entity/wings.png");
	private final WingsModel<T> wings;
	
	public WingsFeature(FeatureRendererContext<T, M> context,  EntityModelLoader loader)
	{
		super(context);
		wings = new WingsModel<>(loader.getModelPart(UltracraftClient.WINGS_LAYER), RenderLayer::getEntityCutoutNoCull);
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
	{
		WingedPlayerEntity winged = ((WingedPlayerEntity)entity);
		if(winged.isWingsActive())
		{
			matrices.push();
			wings.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch, winged);
			VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(TEXTURE), false, false);
			if(entity.isSneaking())
			{
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20f));
				matrices.translate(0f , 0.15f, 0f);
			}
			if(entity.isSprinting())
			{
				matrices.translate(0f , 13f / 16f, 8f / 16f);
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45f));
			}
			wings.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
			matrices.pop();
		}
	}
}
