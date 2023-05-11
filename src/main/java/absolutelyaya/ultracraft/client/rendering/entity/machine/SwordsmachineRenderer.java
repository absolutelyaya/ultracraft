package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.SwordsmachineEmissiveLayer;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.SwordsmachineRageLayer;
import absolutelyaya.ultracraft.entity.machine.SwordsmachineEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class SwordsmachineRenderer extends GeoEntityRenderer<SwordsmachineEntity>
{
	private static final String SWORD = "machinesword";
	
	public SwordsmachineRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new SwordsmachineModel());
		addRenderLayer(new SwordsmachineEmissiveLayer(this));
		
		addRenderLayer(new BlockAndItemGeoLayer<>(this)
		{
			@Override
			protected ItemStack getStackForBone(GeoBone bone, SwordsmachineEntity animatable)
			{
				if(bone.getName().equals(SWORD) && animatable.isHasSword())
					return animatable.getSwordStack();
				return super.getStackForBone(bone, animatable);
			}
			
			@Override
			protected ModelTransformationMode getTransformTypeForStack(GeoBone bone, ItemStack stack, SwordsmachineEntity animatable)
			{
				return super.getTransformTypeForStack(bone, stack, animatable);
			}
			
			@Override
			protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack, SwordsmachineEntity animatable, VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay)
			{
				poseStack.push();
				if(bone.getName().equals(SWORD))
				{
					poseStack.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(45), 1f, 0f, 0f)));
					poseStack.translate(0f, -0.325f, 0f);
				}
				super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
				poseStack.pop();
			}
		});
		addRenderLayer(new SwordsmachineRageLayer(this));
	}
	
	@Override
	public Identifier getTexture(SwordsmachineEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/swordsmachine.png");
	}
	
	@Override
	public RenderLayer getRenderType(SwordsmachineEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayer.getEntityTranslucent(texture);
	}
	
	@Override
	public void render(SwordsmachineEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight)
	{
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
