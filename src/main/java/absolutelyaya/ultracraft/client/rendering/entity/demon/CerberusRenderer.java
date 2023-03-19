package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.EnragedCerebusLayer;
import absolutelyaya.ultracraft.entity.demon.CerberusEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CerberusRenderer extends GeoEntityRenderer<CerberusEntity>
{
	public CerberusRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new CerberusModel());
		addRenderLayer(new EnragedCerebusLayer(this));
	}
	
	@Override
	public Identifier getTexture(CerberusEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/cerberus.png");
	}
	
	@Override
	public RenderLayer getRenderType(CerberusEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayer.getEntityTranslucent(texture);
	}
	
	@Override
	public void render(CerberusEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight)
	{
		poseStack.push();
		poseStack.scale(2, 2, 2);
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		poseStack.pop();
	}
}
