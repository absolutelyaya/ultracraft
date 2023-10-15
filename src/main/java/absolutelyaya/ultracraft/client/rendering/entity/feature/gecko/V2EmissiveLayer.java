package absolutelyaya.ultracraft.client.rendering.entity.feature.gecko;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.machine.V2Entity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class V2EmissiveLayer extends GeoRenderLayer<V2Entity>
{
	static final Identifier YELLOW = new Identifier(Ultracraft.MOD_ID, "textures/entity/v2/yellow_e.png");
	static final Identifier BLUE = new Identifier(Ultracraft.MOD_ID, "textures/entity/v2/blue_e.png");
	static final Identifier RED = new Identifier(Ultracraft.MOD_ID, "textures/entity/v2/red_e.png");
	static final Identifier GREEN = new Identifier(Ultracraft.MOD_ID, "textures/entity/v2/green_e.png");
	
	public V2EmissiveLayer(GeoRenderer<V2Entity> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	@Override
	public void render(MatrixStack poseStack, V2Entity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		RenderLayer armorRenderType;
		Identifier tex = switch((animatable.getMovementMode() + 3) % 4)
		{
			default -> YELLOW;
			case 1 -> BLUE;
			case 2 -> RED;
			case 3 -> GREEN;
		};
		armorRenderType = RenderLayer.getEntityTranslucentEmissive(tex);
		getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, armorRenderType,
				bufferSource.getBuffer(armorRenderType), partialTick, packedLight, OverlayTexture.DEFAULT_UV,
				1, 1, 1, 1);
	}
}
