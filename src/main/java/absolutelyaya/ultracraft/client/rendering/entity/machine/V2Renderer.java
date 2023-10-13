package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.V2RageLayer;
import absolutelyaya.ultracraft.entity.machine.V2Entity;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class V2Renderer extends GeoEntityRenderer<V2Entity>
{
	private static final String WEAPON = "weapon";
	
	public V2Renderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new V2Model());
		
		addRenderLayer(new BlockAndItemGeoLayer<>(this) //TODO: actually hold the currently used weapon
		{
			@Override
			protected ItemStack getStackForBone(GeoBone bone, V2Entity animatable)
			{
				if(bone.getName().equals(WEAPON))
					return ItemRegistry.CORE_SHOTGUN.getDefaultStack();
				return super.getStackForBone(bone, animatable);
			}
			
			@Override
			protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack, V2Entity animatable, VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay)
			{
				poseStack.push();
				if(bone.getName().equals(WEAPON))
				{
					poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
					poseStack.translate(0f, 0.05f, -0.3f);
				}
				super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
				poseStack.pop();
			}
		});
		addRenderLayer(new V2RageLayer(this));
	}
	
	@Override
	public Identifier getTexture(V2Entity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/v2.png");
	}
	
	@Override
	public RenderLayer getRenderType(V2Entity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick)
	{
		return RenderLayer.getEntityTranslucent(texture);
	}
	
	@Override
	public void render(V2Entity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight)
	{
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
