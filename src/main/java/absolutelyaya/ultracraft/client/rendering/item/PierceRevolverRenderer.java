package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.PierceRevolverItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.Random;

public class PierceRevolverRenderer extends GeoItemRenderer<PierceRevolverItem>
{
	final Random random = new Random();
	
	public PierceRevolverRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "revolver")));
	}
	
	@Override
	public Identifier getTextureLocation(PierceRevolverItem animatable)
	{
		double cooldown = 0f;
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player != null)
			cooldown = player.getItemCooldownManager().getCooldownProgress(animatable, 0f);
		
		if (cooldown > 0.5f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver0.png");
		else if (cooldown > 0f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver1.png");
		
		float useTime = 1f - (animatable.getMaxUseTime(null) - animatable.getApproxUseTime()) / (float)(animatable.getMaxUseTime(null));
		if(useTime > 0.99f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver.png");
		else if(useTime > 0.5f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver3.png");
		else if(useTime > 0f)
			return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver2.png");
		
		return new Identifier(Ultracraft.MOD_ID, "textures/item/pierce_revolver.png");
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
		float useTime = 1f - (stack.getItem().getMaxUseTime(stack) - ((PierceRevolverItem)stack.getItem()).getApproxUseTime()) / (float)(stack.getItem().getMaxUseTime(stack));
		useTime = MathHelper.clamp(useTime, 0f, 1f);
		poseStack.translate((random.nextFloat() - 0.5f) * useTime * 0.01f, (random.nextFloat() - 0.5f) * useTime * 0.01f, (random.nextFloat() - 0.5f) * useTime * 0.01f);
		super.render(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
		poseStack.pop();
	}
}
