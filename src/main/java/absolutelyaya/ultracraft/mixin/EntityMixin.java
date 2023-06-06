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
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Shadow public abstract World getWorld();
	
	@Shadow public abstract float getYaw(float tickDelta);
	
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
	
	@ModifyArgs(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"))
	public void onConvertMovementInputToVel(Args args)
	{
		if(getWorld().isClient() && this instanceof WingedPlayerEntity winged && winged.isWingsActive() &&
				   winged instanceof PlayerEntity p && p.isSprinting())
		{
			float slideDirRot = (float)Math.toDegrees(Math.atan2(winged.getSlideDir().z, winged.getSlideDir().x));
			float cappedYaw = ((float)args.get(2) + 90f) % 360f;
			if(cappedYaw < 0)
				cappedYaw += 360;
			float delta = angleDelta(cappedYaw, slideDirRot);
			args.set(1, (float)args.get(1) * (Math.max((1f - delta / 45f), 0f) - Math.max((delta - 135f) / 45f, 0f)));
			args.set(2, slideDirRot - 90f);
		}
	}
	
	public float angleDelta(float alpha, float beta)
	{
		float capDelta = Math.abs(beta - alpha) % 360;
		return capDelta > 180 ? 360 - capDelta : capDelta;
	}
}
