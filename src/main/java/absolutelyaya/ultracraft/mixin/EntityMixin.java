package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Shadow public abstract World getWorld();
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	void onTick(CallbackInfo ci)
	{
		if (Ultracraft.isTimeFrozen())
			ci.cancel();
	}
	
	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	void onMove(CallbackInfo ci)
	{
		if (Ultracraft.isTimeFrozen())
			ci.cancel();
	}
	
	@ModifyArg(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"), index = 2)
	public float onConvertMovementInputToVel(float yaw)
	{
		if(getWorld().isClient() && this instanceof WingedPlayerEntity winged && winged.isWingsActive() && winged instanceof PlayerEntity p && p.isSprinting())
			return (float)(Math.toDegrees(Math.atan2(winged.getSlideDir().z, winged.getSlideDir().x) - Math.toRadians(90)));
		return yaw;
	}
}
