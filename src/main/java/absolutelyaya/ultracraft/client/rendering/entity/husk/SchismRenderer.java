package absolutelyaya.ultracraft.client.rendering.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.husk.SchismEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.renderer.GeoEntityRenderer;

public class SchismRenderer extends GeoEntityRenderer<SchismEntity>
{
	public SchismRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new SchismModel());
	}
	
	@Override
	public Identifier getTexture(SchismEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/schism.png");
	}
	
	@Override
	public RenderLayer getRenderType(SchismEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayer.getEntityTranslucent(texture);
	}
	
	@Override
	public void render(SchismEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight)
	{
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
