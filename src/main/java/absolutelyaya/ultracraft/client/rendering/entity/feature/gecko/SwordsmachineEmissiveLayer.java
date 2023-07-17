package absolutelyaya.ultracraft.client.rendering.entity.feature.gecko;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.machine.DestinyBondSwordsmachineEntity;
import absolutelyaya.ultracraft.entity.machine.SwordsmachineEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class SwordsmachineEmissiveLayer extends GeoRenderLayer<SwordsmachineEntity>
{
	private static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/entity/swordsmachine_emissive.png");
	
	public SwordsmachineEmissiveLayer(GeoRenderer<SwordsmachineEntity> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	@Override
	public void render(MatrixStack poseStack, SwordsmachineEntity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		RenderLayer armorRenderType;
		if(animatable instanceof DestinyBondSwordsmachineEntity destinySM)
			armorRenderType = RenderLayer.getEntityTranslucentEmissive(
					destinySM.getVariant() == 0 ? new Identifier(Ultracraft.MOD_ID, "textures/entity/swordsmachine_emissive_tundra.png") :
							new Identifier(Ultracraft.MOD_ID, "textures/entity/swordsmachine_emissive_agony.png"));
		else
			armorRenderType = RenderLayer.getEntityTranslucentEmissive(TEXTURE);
		
		getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, armorRenderType,
				bufferSource.getBuffer(armorRenderType), partialTick, packedLight, OverlayTexture.DEFAULT_UV,
				1, 1, 1, 1);
	}
}
