package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.entity.machine.StreetCleanerEntity;
import absolutelyaya.ultracraft.entity.machine.SwordsmachineEntity;
import absolutelyaya.ultracraft.registry.ItemRegistry;
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

public class StreetCleanerEntityRenderer extends GeoEntityRenderer<StreetCleanerEntity>
{
	private static final String FLAMETHROWER = "FlameThrower";
	final ItemStack flameThrowerStack;
	
	public StreetCleanerEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new StreetCleanerEntityModel());
		
		flameThrowerStack = ItemRegistry.FLAMETHROWER.getDefaultStack();
		addRenderLayer(new BlockAndItemGeoLayer<>(this)
		{
			@Override
			protected ItemStack getStackForBone(GeoBone bone, StreetCleanerEntity animatable)
			{
				if(bone.getName().equals(FLAMETHROWER))
					return flameThrowerStack;
				return super.getStackForBone(bone, animatable);
			}
			
			@Override
			protected ModelTransformationMode getTransformTypeForStack(GeoBone bone, ItemStack stack, StreetCleanerEntity animatable)
			{
				return super.getTransformTypeForStack(bone, stack, animatable);
			}
			
			@Override
			protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack, StreetCleanerEntity animatable, VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay)
			{
				poseStack.push();
				if(bone.getName().equals(FLAMETHROWER))
				{
					poseStack.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-32.5), 0f, 1f, 0f)));
					poseStack.translate(0f, -0.1f, -0.5f);
				}
				super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
				poseStack.pop();
			}
		});
	}
	
	@Override
	public RenderLayer getRenderType(StreetCleanerEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayer.getEntityTranslucent(texture);
	}
}
