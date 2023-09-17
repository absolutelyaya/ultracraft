package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.EntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import absolutelyaya.ultracraft.registry.TagRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityAccessor
{
	@Shadow public abstract World getWorld();
	
	@Shadow public abstract float getYaw(float tickDelta);
	
	@Shadow public abstract BlockPos getBlockPos();
	
	@Shadow public abstract Box getBoundingBox();
	
	@Shadow private World world;
	
	@Shadow public abstract boolean isAlive();
	
	Supplier<Boolean> isTargettableSupplier = this::isAlive;
	Supplier<Vec3d> relativeTargetPointSupplier = () -> getBoundingBox().getCenter();
	Function<Entity, Integer> targetPriorityFunction = entity -> 0;
	
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
		if(getWorld().isClient() && this instanceof WingedPlayerEntity winged && UltraComponents.WING_DATA.get(winged).isVisible() &&
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
		if(!(isInBlood()))
			return effect;
		if(effect.equals(ParticleTypes.SPLASH))
			return ParticleRegistry.BLOOD_SPLASH;
		else if(effect.equals(ParticleTypes.BUBBLE))
			return ParticleRegistry.BLOOD_BUBBLE;
		else return effect;
	}
	
	boolean isInBlood()
	{
		Box box = getBoundingBox().contract(0.001);
		for(int x = MathHelper.floor(box.minX); x < MathHelper.ceil(box.maxX); ++x)
		{
			for(int y = MathHelper.floor(box.minY); y < MathHelper.ceil(box.maxY); ++y)
			{
				for(int z = MathHelper.floor(box.minZ); z < MathHelper.ceil(box.maxZ); ++z)
				{
					BlockPos pos = new BlockPos(x, y, z);
					FluidState fluidState = world.getFluidState(pos);
					if (fluidState.isIn(TagRegistry.BLOOD_FLUID) && y + fluidState.getHeight(world, pos) >= box.minY)
						return true;
				}
			}
		}
		return false;
	}
	
	public float angleDelta(float alpha, float beta)
	{
		float capDelta = Math.abs(beta - alpha) % 360;
		return capDelta > 180 ? 360 - capDelta : capDelta;
	}
	
	@Override
	public boolean isTargettable()
	{
		return isTargettableSupplier.get();
	}
	
	@Override
	public void setTargettableSupplier(Supplier<Boolean> supplier)
	{
		isTargettableSupplier = supplier;
	}
	
	@Override
	public Vec3d getRelativeTargetPoint()
	{
		return relativeTargetPointSupplier.get();
	}
	
	@Override
	public void setRelativeTargetPointSupplier(Supplier<Vec3d> supplier)
	{
		relativeTargetPointSupplier = supplier;
	}
	
	@Override
	public int getTargetPriority(Entity source)
	{
		return targetPriorityFunction.apply(source);
	}
	
	@Override
	public void setTargetpriorityFunction(Function<Entity, Integer> function)
	{
		targetPriorityFunction = function;
	}
}
