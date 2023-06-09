package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class MaliciousFaceRenderer extends MobEntityRenderer<MaliciousFaceEntity, MaliciousFaceModel<MaliciousFaceEntity>>
{
	public MaliciousFaceRenderer(EntityRendererFactory.Context context)
	{
		super(context, new MaliciousFaceModel<>(context.getPart(UltracraftClient.MALICIOUS_LAYER)), 1f);
		addFeature(new MaliciousGlowFeatureRenderer<>(this));
	}
	
	@Override
	public Identifier getTexture(MaliciousFaceEntity entity)
	{
		return new Identifier(Ultracraft.MOD_ID, entity.isCracked() ? "textures/entity/malicious_face_cracked.png" : "textures/entity/malicious_face.png");
	}
	
	@Override
	public void render(MaliciousFaceEntity mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		RenderSystem.enableBlend();
		super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
	}
	
	@Nullable
	@Override
	protected RenderLayer getRenderLayer(MaliciousFaceEntity entity, boolean showBody, boolean translucent, boolean showOutline)
	{
		return RenderLayer.getEntityTranslucent(getTexture(entity));
	}
	
	static class MaliciousGlowFeatureRenderer<T extends MaliciousFaceEntity> extends FeatureRenderer<T, MaliciousFaceModel<T>>
	{
		private static final RenderLayer NORMAL = RenderLayer.getEntityTranslucent(new Identifier(Ultracraft.MOD_ID, "textures/entity/malicious_face_emissive.png"));
		private static final RenderLayer ENRAGED = RenderLayer.getEntityTranslucent(new Identifier(Ultracraft.MOD_ID, "textures/entity/malicious_face_cracked_emissive.png"));
		
		public MaliciousGlowFeatureRenderer(FeatureRendererContext<T, MaliciousFaceModel<T>> featureRendererContext)
		{
			super(featureRendererContext);
		}
		
		@Override
		public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
		{
			VertexConsumer vertexConsumer = vertexConsumers.getBuffer(getContextModel().cracked && UltracraftClient.isViolentFeaturesEnabled(entity.world) ? ENRAGED : NORMAL);
			matrices.scale(1.005f, 1.005f, 1.005f);
			((Model)this.getContextModel()).render(matrices, vertexConsumer, 15728880, OverlayTexture.DEFAULT_UV, 0.0f, 1.0f, 1.0f, 1.0f);
		}
	}
}
