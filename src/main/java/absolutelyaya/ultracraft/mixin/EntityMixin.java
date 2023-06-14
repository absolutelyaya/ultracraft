package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.fluid.BloodFluid;
import absolutelyaya.ultracraft.registry.FluidRegistry;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Shadow public abstract World getWorld();
	
	@Shadow public abstract float getYaw(float tickDelta);
	
	@Shadow public abstract BlockPos getBlockPos();
	
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
	
	@ModifyArg(method = "onSwimmingStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"))
	ParticleEffect onSwimmingStart(ParticleEffect effect)
	{
		if(!(getWorld().getFluidState(getBlockPos()).getFluid() instanceof BloodFluid))
			return effect;
		if(effect.equals(ParticleTypes.SPLASH))
			return ParticleRegistry.BLOOD_SPLASH;
		else if(effect.equals(ParticleTypes.BUBBLE))
			return ParticleRegistry.BLOOD_BUBBLE;
		else return effect;
	}
	
	public float angleDelta(float alpha, float beta)
	{
		float capDelta = Math.abs(beta - alpha) % 360;
		return capDelta > 180 ? 360 - capDelta : capDelta;
	}
}
