package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin
{
	@Shadow protected abstract void moveBy(double x, double y, double z);
	
	@Shadow protected abstract double clipToSpace(double desiredCameraDistance);
	
	@Inject(method = "update", at = @At("TAIL"))
	void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci)
	{
		float f = UltracraftClient.getConfigHolder().get().slideCamOffset / 100f;
		if(thirdPerson && focusedEntity instanceof PlayerEntity player && f > 0f)
		{
			WingedPlayerEntity winged = (WingedPlayerEntity)player;
			if(winged.isWingsActive() && player.isSprinting())
				moveBy(clipToSpace(1.5f * f), f, -1.5f * f);
		}
	}
}
