package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements WingedPlayerEntity
{
	@Shadow public abstract boolean isCreative();
	
	@Shadow @Final @Mutable private static Map<EntityPose, EntityDimensions> POSE_DIMENSIONS;
	boolean wingsActive;
	byte wingState, lastState;
	float wingAnimTime;
	int dashingTicks = -2, stamina, wingHintDisplayTicks;
	
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInit(CallbackInfo ci)
	{
		POSE_DIMENSIONS = new HashMap<>(POSE_DIMENSIONS);
		POSE_DIMENSIONS.put(ClassTinkerers.getEnum(EntityPose.class, "SLIDE"), EntityDimensions.changing(0.6f, 1f));
	}
	
	@Redirect(method = "updatePose", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"))
	void onUpdatePose(PlayerEntity instance, EntityPose entityPose)
	{
		WingedPlayerEntity winged = ((WingedPlayerEntity)instance);
		boolean hiVelMode = winged.isWingsVisible();
		if(hiVelMode)
		{
			if(winged.isDashing())
				setPose(ClassTinkerers.getEnum(EntityPose.class, "DASH"));
			else if(isSprinting())
				setPose(ClassTinkerers.getEnum(EntityPose.class, "SLIDE"));
			else
				setPose(entityPose);
		}
		else
			setPose(entityPose);
	}
	
	@Inject(method = "getActiveEyeHeight", at = @At("HEAD"), cancellable = true)
	void onGetActiveEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir)
	{
		if(pose.equals(ClassTinkerers.getEnum(EntityPose.class, "SLIDE")))
			cir.setReturnValue(0.4f);
		else if(pose.equals(ClassTinkerers.getEnum(EntityPose.class, "DASH")))
			cir.setReturnValue(1.27f);
	}
	
	@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		if(isDashing())
			cir.setReturnValue(false);
	}
	
	@Override
	public void setWingState(byte state)
	{
		lastState = wingState;
		wingState = state;
		setWingAnimTime(0);
	}
	
	@Override
	public byte getWingState()
	{
		if(isDashing())
			setWingState((byte)0);
		else if (isSprinting())
			setWingState((byte)2);
		else if ((wingState == 0 && isOnGround()) || (wingState == 2 && !isSprinting()))
			setWingState((byte)1);
		return wingState;
	}
	
	@Override
	public void onDash()
	{
		dashingTicks = 3;
	}
	
	@Override
	public void onDashJump()
	{
		dashingTicks = -2;
	}
	
	public boolean isDashing()
	{
		return dashingTicks > 0;
	}
	
	@Override
	public boolean wasDashing()
	{
		return dashingTicks + 1 == 0;
	}
	
	@Override
	public byte getLastState()
	{
		return lastState;
	}
	
	@Override
	public float getWingAnimTime()
	{
		return wingAnimTime;
	}
	
	@Override
	public void setWingAnimTime(float f)
	{
		wingAnimTime = f;
	}
	
	@Override
	public void setWingsVisible(boolean b)
	{
		wingsActive = b;
		setWingState((byte)(b ? 1 : 0));
		wingHintDisplayTicks = 60;
	}
	
	@Override
	public boolean isWingsVisible()
	{
		return wingsActive;
	}
	
	@Override
	public int getStamina()
	{
		return stamina;
	}
	
	@Override
	public boolean consumeStamina()
	{
		if(isCreative())
			return true;
		if(stamina >= 30)
		{
			stamina = Math.max(stamina - 30, 0);
			return true;
		}
		return false;
	}
	
	@Override
	public int getWingHintDisplayTicks()
	{
		return wingHintDisplayTicks;
	}
	
	@Inject(method = "tickMovement", at = @At("TAIL"))
	void onTick(CallbackInfo ci)
	{
		if(dashingTicks >= -1)
		{
			dashingTicks--;
			Vec3d dir = getVelocity();
			Vec3d particleVel = new Vec3d(-dir.x, 0, -dir.z).multiply(random.nextDouble() * 0.33 + 0.1);
			Vec3d pos = getPos().add((random.nextDouble() - 0.5) * getWidth(),
					random.nextDouble() * getHeight(), (random.nextDouble() - 0.5) * getWidth()).add(dir.multiply(0.25));
			world.addParticle(ParticleRegistry.DASH, pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
		}
		if(isSprinting() && isWingsVisible())
		{
			Vec3d dir = getVelocity().multiply(1.0, 0.0, 1.0).normalize();
			Vec3d particleVel = new Vec3d(-dir.x, -dir.y, -dir.z).multiply(random.nextDouble() * 0.1 + 0.025);
			Vec3d pos = getPos().add(dir.multiply(1.5));
			world.addParticle(ParticleRegistry.SLIDE, pos.x, pos.y + 0.1, pos.z, particleVel.x, particleVel.y, particleVel.z);
		}
		if(stamina < 90)
			stamina++;
		if(wingHintDisplayTicks > 0)
			wingHintDisplayTicks--;
	}
	
	@Inject(method = "adjustMovementForSneaking", at = @At("HEAD"), cancellable = true)
	void onAdjustMovementForSneaking(Vec3d movement, MovementType type, CallbackInfoReturnable<Vec3d> cir)
	{
		if(isWingsVisible())
			cir.setReturnValue(movement);
	}
}
