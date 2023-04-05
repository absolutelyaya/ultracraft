package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.SwordmachineEmissiveLayer;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.SwordmachineRageLayer;
import absolutelyaya.ultracraft.entity.machine.SwordmachineEntity;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.DynamicGeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class SwordmachineRenderer extends DynamicGeoEntityRenderer<SwordmachineEntity>
{
	private static final String SWORD = "machinesword";
	
	public SwordmachineRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new SwordmachineModel());
		addRenderLayer(new SwordmachineRageLayer(this));
		addRenderLayer(new SwordmachineEmissiveLayer(this));
		
		addRenderLayer(new BlockAndItemGeoLayer<>(this)
		{
			@Override
			protected ItemStack getStackForBone(GeoBone bone, SwordmachineEntity animatable)
			{
				if(bone.getName().equals(SWORD) && animatable.isHasSword())
					return ItemRegistry.MACHINE_SWORD.getDefaultStack();
				return super.getStackForBone(bone, animatable);
			}
			
			@Override
			protected ModelTransformation.Mode getTransformTypeForStack(GeoBone bone, ItemStack stack, SwordmachineEntity animatable)
			{
				return super.getTransformTypeForStack(bone, stack, animatable);
			}
			
			@Override
			protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack, SwordmachineEntity animatable, VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay)
			{
				if(bone.getName().equals(SWORD))
				{
					poseStack.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(35), 1f, 0f, 0f)));
					poseStack.translate(0f, -0.325f, 0f);
				}
				super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
			}
		});
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
