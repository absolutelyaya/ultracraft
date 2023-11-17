package absolutelyaya.ultracraft.client.rendering.entity.feature;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.HideWingsSetting;
import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.screen.WingCustomizationScreen;
import absolutelyaya.ultracraft.components.player.IWingDataComponent;
import absolutelyaya.ultracraft.components.player.IWingedPlayerComponent;
import absolutelyaya.ultracraft.registry.WingPatterns;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.model.ModelTransform;
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
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class WingsFeature<T extends PlayerEntity, M extends PlayerEntityModel<T>> extends FeatureRenderer<T, M>
{
	private static final Identifier TEXTURE_CLR = new Identifier(Ultracraft.MOD_ID, "textures/entity/wings_clr.png");
	private static final Identifier TEXTURE_WNG = new Identifier(Ultracraft.MOD_ID, "textures/entity/wings_wing.png");
	private static final Identifier TEXTURE_MTL = new Identifier(Ultracraft.MOD_ID, "textures/entity/wings_metal.png");
	private final WingsModel<T> wingsModel;
	
	public WingsFeature(FeatureRendererContext<T, M> context, EntityModelLoader loader)
	{
		super(context);
		wingsModel = new WingsModel<>(loader.getModelPart(UltracraftClient.WINGS_LAYER),
				(identifier) -> RenderLayers.getWingsPattern(identifier, UltracraftClient.wingPattern));
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
	{
		HideWingsSetting hideWings = UltracraftClient.getConfig().hideWings;
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
		IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(entity);
		IWingDataComponent wings = UltraComponents.WING_DATA.get(entity);
		VertexConsumer vertexConsumer;
		if(wings.isActive() || (entity.isMainPlayer() && WingCustomizationScreen.MenuOpen))
		{
			Vector3f[] clrs = wings.getColors();
			matrices.push();
			wingsModel.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch, winged);
			ModelTransform transform = getContextModel().body.getTransform();
			matrices.translate(transform.pivotX / 16, transform.pivotY / 16, transform.pivotZ / 16);
			matrices.multiply(new Quaternionf(new AxisAngle4f(transform.pitch, 1f, 0f, 0f)));
			matrices.multiply(new Quaternionf(new AxisAngle4f(transform.yaw, 0f, 1f, 0f)));
			matrices.multiply(new Quaternionf(new AxisAngle4f(transform.roll, 0f, 0f, 1f)));
			if(UltracraftClient.IRIS)
			{
				vertexConsumer = vertexConsumers.getBuffer(RenderLayers.getEntityCutout(TEXTURE_WNG));
				Vector3f clr = clrs[0];
				wingsModel.render(matrices, vertexConsumer, 15728880,
						OverlayTexture.DEFAULT_UV, clr.x / 255f, clr.y / 255f, clr.z / 255f, 1f);
				vertexConsumer = vertexConsumers.getBuffer(RenderLayers.getEntityCutout(TEXTURE_MTL));
				clr = clrs[1];
				wingsModel.render(matrices, vertexConsumer, light,
						OverlayTexture.DEFAULT_UV, clr.x / 255f, clr.y / 255f, clr.z / 255f, 1f);
			}
			else
			{
				String patternID = wings.getPattern();
				WingPatterns.WingPattern p = null;
				if(patternID.length() > 0)
					p = WingPatterns.getPattern(patternID);
				ShaderProgram wingShader = p == null ? UltracraftClient.getWingsColoredShaderProgram() : p.program().get();
				wingShader.getUniform("WingColor").set(clrs[0]);
				wingShader.getUniform("MetalColor").set(clrs[1]);
				RenderSystem.setShader(p == null ? UltracraftClient::getWingsColoredShaderProgram : p.program());
				vertexConsumer = vertexConsumers.getBuffer(RenderLayers.getWingsPattern(TEXTURE_CLR, patternID));
				wingsModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
			}
			matrices.pop();
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	}
}
