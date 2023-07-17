package absolutelyaya.ultracraft.client.rendering.entity.feature;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.HideWingsSetting;
import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.screen.WingCustomizationScreen;
import absolutelyaya.ultracraft.registry.WingPatterns;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class WingsFeature<T extends PlayerEntity, M extends PlayerEntityModel<T>> extends FeatureRenderer<T, M>
{
	private static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/entity/wings_clr.png");
	private final WingsModel<T> wings;
	
	public WingsFeature(FeatureRendererContext<T, M> context, EntityModelLoader loader)
	{
		super(context);
		wings = new WingsModel<>(loader.getModelPart(UltracraftClient.WINGS_LAYER), (identifier) -> RenderLayers.getWingsPattern(identifier, UltracraftClient.wingPattern));
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
	{
		WingedPlayerEntity winged = ((WingedPlayerEntity)entity);
		HideWingsSetting hideWings = UltracraftClient.getConfigHolder().get().hideWings;
		switch(hideWings)
		{
			case ALL -> {
				return;
			}
			case ONLY_OWN -> {
				if(entity.isMainPlayer())
					return;
			}
			case ONLY_OTHERS -> {
				if(!entity.isMainPlayer())
					return;
			}
			case NONE -> {}
		}
		if(winged.isWingsActive() || (entity.isMainPlayer() && WingCustomizationScreen.MenuOpen))
		{
			Vec3d[] clrs = winged.getWingColors();
			String patternID = winged.getWingPattern();
			WingPatterns.WingPattern p = null;
			if(patternID.length() > 0)
				p = WingPatterns.getPattern(patternID);
			ShaderProgram wingShader = p == null ? UltracraftClient.getWingsColoredShaderProgram() : p.program().get();
			wingShader.getUniform("WingColor").set(clrs[0].toVector3f());
			wingShader.getUniform("MetalColor").set(clrs[1].toVector3f());
			RenderSystem.setShader(p == null ? UltracraftClient::getWingsColoredShaderProgram : p.program());
			matrices.push();
			wings.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch, winged);
			VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayers.getWingsPattern(TEXTURE, patternID));
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
