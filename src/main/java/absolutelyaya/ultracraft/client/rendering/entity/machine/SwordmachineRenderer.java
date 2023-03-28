package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.SwordmachineEmissiveLayer;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.SwordmachineRageLayer;
import absolutelyaya.ultracraft.entity.machine.SwordmachineEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SwordmachineRenderer extends GeoEntityRenderer<SwordmachineEntity>
{
	public SwordmachineRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new SwordmachineModel());
		addRenderLayer(new SwordmachineRageLayer(this));
		addRenderLayer(new SwordmachineEmissiveLayer(this));
	}
	
	@Override
	public Identifier getTexture(SwordmachineEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/swordmachine.png");
	}
	
	@Override
	public RenderLayer getRenderType(SwordmachineEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayer.getEntityTranslucent(texture);
	}
	
	@Override
	public void render(SwordmachineEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight)
	{
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
