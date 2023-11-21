package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin
{
	private static final ModelIdentifier HARPOON_LONG;
	
	@Shadow @Final private ItemModels models;
	
	@Shadow public abstract BakedModel getModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed);
	
	@ModifyVariable(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"), argsOnly = true)
	public BakedModel onRenderItem(BakedModel value, ItemStack stack, ModelTransformationMode mode)
	{
		if(stack.isOf(ItemRegistry.HARPOON) && !mode.equals(ModelTransformationMode.GUI))
			return models.getModelManager().getModel(HARPOON_LONG);
		return value;
	}
	
	static {
		HARPOON_LONG = new ModelIdentifier(new Identifier(Ultracraft.MOD_ID, "harpoon_long"), "inventory");
	}
}
