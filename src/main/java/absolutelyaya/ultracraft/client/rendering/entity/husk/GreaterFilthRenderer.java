package absolutelyaya.ultracraft.client.rendering.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.EnragedGreaterFilthLayer;
import absolutelyaya.ultracraft.entity.husk.GreaterFilthEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class GreaterFilthRenderer extends GeoEntityRenderer<GreaterFilthEntity>
{
	public GreaterFilthRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new GreaterFilthModel());
		addRenderLayer(new EnragedGreaterFilthLayer(this));
	}
	
	@Override
	public Identifier getTexture(GreaterFilthEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, object.isRare() ? "textures/entity/blue_filth.png" : "textures/entity/filth.png");
	}
	
	@Override
	public RenderLayer getRenderType(GreaterFilthEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayer.getEntityTranslucent(texture);
	}
	
	@Override
	public void render(GreaterFilthEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight)
	{
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
