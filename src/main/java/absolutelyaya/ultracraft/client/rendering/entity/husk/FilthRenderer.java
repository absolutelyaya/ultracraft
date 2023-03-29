package absolutelyaya.ultracraft.client.rendering.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FilthRenderer extends GeoEntityRenderer<FilthEntity>
{
	public FilthRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new FilthModel());
	}
	
	@Override
	public Identifier getTexture(FilthEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, object.isRare() ? "textures/entity/blue_filth.png" : "textures/entity/filth.png");
	}
	
	@Override
	public RenderLayer getRenderType(FilthEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayer.getEntityTranslucent(texture);
	}
	
	@Override
	public void render(FilthEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight)
	{
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
