package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.components.player.IArmComponent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin
{
	@Inject(method = "setModelPose", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isInSneakingPose()Z"))
	void onSetBodyPartsVisible(AbstractClientPlayerEntity player, CallbackInfo ci)
	{
		IArmComponent arm = UltraComponents.ARMS.get(player);
		if(!arm.isVisible() || arm.getActiveArm() == -1)
			return;
		PlayerEntityModel<?> playerEntityModel = ((PlayerEntityRenderer)(Object)this).getModel();
		if(player.getMainArm().equals(Arm.LEFT))
		{
			playerEntityModel.rightArm.visible = playerEntityModel.rightSleeve.visible = false;
			playerEntityModel.leftArm.visible = playerEntityModel.leftSleeve.visible = true;
		}
		else
		{
			playerEntityModel.rightArm.visible = playerEntityModel.rightSleeve.visible = true;
			playerEntityModel.leftArm.visible = playerEntityModel.leftSleeve.visible = false;
		}
	}
}
