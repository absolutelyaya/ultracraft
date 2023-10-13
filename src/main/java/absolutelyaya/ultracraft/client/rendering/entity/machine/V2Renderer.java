package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.SwordsmachineEmissiveLayer;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.SwordsmachineRageLayer;
import absolutelyaya.ultracraft.client.rendering.entity.feature.gecko.V2RageLayer;
import absolutelyaya.ultracraft.entity.machine.DestinyBondSwordsmachineEntity;
import absolutelyaya.ultracraft.entity.machine.SwordsmachineEntity;
import absolutelyaya.ultracraft.entity.machine.V2Entity;
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

public class V2Renderer extends GeoEntityRenderer<V2Entity>
{
	public V2Renderer(EntityRendererFactory.Context ctx)
	{
		super(ctx, new V2Model());
		
		addRenderLayer(new BlockAndItemGeoLayer<>(this) //TODO: actually hold the currently used weapon
		{
			@Override
			protected ItemStack getStackForBone(GeoBone bone, V2Entity animatable)
			{
				return super.getStackForBone(bone, animatable);
			}
			
			@Override
			protected ModelTransformationMode getTransformTypeForStack(GeoBone bone, ItemStack stack, V2Entity animatable)
			{
				return super.getTransformTypeForStack(bone, stack, animatable);
			}
			
			@Override
			protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack, V2Entity animatable, VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay)
			{
				super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
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
