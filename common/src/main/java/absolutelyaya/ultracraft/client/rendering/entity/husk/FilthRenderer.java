package absolutelyaya.ultracraft.client.rendering.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class FilthRenderer extends GeoEntityRenderer<FilthEntity>
{
	public FilthRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new FilthModel());
	}
	
	@Override
	public Identifier getTextureResource(FilthEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/filth.png");
	}
	
	@Override
	public RenderLayer getRenderType(FilthEntity animatable, float partialTicks, MatrixStack stack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, Identifier textureLocation)
	{
		return RenderLayer.getEntityTranslucent(textureLocation);
	}
	
	@Override
	public void render(GeoModel model, FilthEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		super.render(model, animatable, partialTicks, type, matrixStack, renderTypeBuffer, vertexBuilder, packedLightIn,
				packedOverlayIn, red, green, blue, alpha);
	}
}
