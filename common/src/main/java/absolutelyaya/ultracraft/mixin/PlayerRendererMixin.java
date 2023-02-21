package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.client.rendering.entity.feature.WingsFeature;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>
{
	public PlayerRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius)
	{
		super(ctx, model, shadowRadius);
	}
	
	@Inject(method = "<init>", at = @At("TAIL"))
	public void constructor(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci)
	{
		addFeature(new WingsFeature<>(this, ctx.getModelLoader()));
	}
}
