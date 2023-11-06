package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.item.CoreEjectShotgunItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.Random;

public class CoreEjectShotgunRenderer extends GeoItemRenderer<CoreEjectShotgunItem>
{
	final Random random = new Random();
	
	public CoreEjectShotgunRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "shotgun")));
	}
	
	@Override
	public Identifier getTextureLocation(CoreEjectShotgunItem animatable)
	{
		float useTime = 1f - (animatable.getMaxUseTime(null) - animatable.getApproxUseTime()) / (float)(animatable.getMaxUseTime(null));
		if(useTime > 0.99f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun7.png");
		else if(useTime > 0.79f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun6.png");
		else if(useTime > 0.59f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun5.png");
		else if(useTime > 0.39f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun4.png");
		else if(useTime > 0f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun3.png");
		
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		float primaryCD = cdm.getCooldownPercent(animatable, 0);
		if(primaryCD < 0.45f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun2.png");
		else if(primaryCD < 0.55f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun1.png");
		else if(primaryCD < 0.65f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun0.png");
		
		return new Identifier(Ultracraft.MOD_ID, "textures/item/core_shotgun.png");
	}
	
	@Override
	public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay)
	{
		if(!transformType.isFirstPerson())
		{
			super.render(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
			return;
		}
		poseStack.push();
		float useTime = 1f - (stack.getItem().getMaxUseTime(stack) - ((CoreEjectShotgunItem)stack.getItem()).getApproxUseTime()) / (float)(stack.getItem().getMaxUseTime(stack));
		useTime = MathHelper.clamp(useTime, 0f, 1f);
		float f = UltracraftClient.getConfig().safeVFX ? 0.01f : 0.025f;
		poseStack.translate((random.nextFloat() - 0.5f) * useTime * f, (random.nextFloat() - 0.5f) * useTime * f, (random.nextFloat() - 0.5f) * useTime * f);
		super.render(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
		poseStack.pop();
	}
}
