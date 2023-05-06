package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.registry.DamageSources;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
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
	
	@Shadow public abstract boolean isSwimming();
	
	boolean wingsActive, groundPounding, ignoreSlowdown;
	byte wingState, lastState;
	float wingAnimTime;
	int dashingTicks = -2, stamina, wingHintDisplayTicks;
	GunCooldownManager gunCDM;
	
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInit(CallbackInfo ci)
	{
		POSE_DIMENSIONS = new HashMap<>(POSE_DIMENSIONS);
		POSE_DIMENSIONS.put(ClassTinkerers.getEnum(EntityPose.class, "SLIDE"), EntityDimensions.changing(0.6f, 1f));
		gunCDM = new GunCooldownManager((PlayerEntity)(Object)this);
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
	
	@Inject(method = "damage", at = @At("TAIL"))
	void afterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		if(wingsActive)
			timeUntilRegen = 11;
	}
	
	@Inject(method = "isSwimming", at = @At("HEAD"), cancellable = true)
	void onIsSwimming(CallbackInfoReturnable<Boolean> cir)
	{
		if(wingsActive)
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
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.75f, 1.6f);
	}
	
	@Override
	public void onDashJump()
	{
		dashingTicks = -2;
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.75f, 1.8f);
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
	public boolean wasDashing(int i)
	{
		return dashingTicks + i >= 0;
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
	
	@Override
	public @NotNull GunCooldownManager getGunCooldownManager()
	{
		return gunCDM;
	}
	
	@Override
	public void startGroundPound()
	{
		groundPounding = true;
	}
	
	@Override
	public void completeGroundPound(boolean strong)
	{
		groundPounding = false;
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS,
				strong ? 1f : 0.75f, strong ? 0.75f : 1.25f);
		world.getOtherEntities(this, getBoundingBox().expand(3f, 0.5f, 3f)).forEach(e -> {
			if((e instanceof LivingEntityAccessor l) && l.takePunchKnockback())
				e.addVelocity(0f, 1f, 0f);
		});
		world.getOtherEntities(this, getBoundingBox().expand(0f, 1f, 0f).offset(0f, -0.5f, 0f)).forEach(e ->
				e.damage(DamageSources.getPound(this), 6));
	}
	
	@Override
	public boolean isGroundPounding()
	{
		return groundPounding;
	}
	
	@Inject(method = "tick", at = @At("TAIL"))
	void onTick(CallbackInfo ci)
	{
		gunCDM.tickCooldowns();
	}
	
	@Inject(method = "tickMovement", at = @At("TAIL"))
	void onTickMovement(CallbackInfo ci)
	{
		if(dashingTicks-- >= -1)
		{
			Vec3d dir = getVelocity();
			Vec3d particleVel = new Vec3d(-dir.x, 0, -dir.z).multiply(random.nextDouble() * 0.33 + 0.1);
			Vec3d pos = getPos().add((random.nextDouble() - 0.5) * getWidth(),
					random.nextDouble() * getHeight(), (random.nextDouble() - 0.5) * getWidth()).add(dir.multiply(0.25));
			world.addParticle(ParticleRegistry.DASH, true, pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
		}
		if(isSprinting() && isWingsVisible())
		{
			Vec3d dir = getVelocity().multiply(1.0, 0.0, 1.0).normalize();
			Vec3d particleVel = new Vec3d(-dir.x, -dir.y, -dir.z).multiply(random.nextDouble() * 0.1 + 0.025);
			Vec3d pos = getPos().add(dir.multiply(1.5));
			world.addParticle(ParticleRegistry.SLIDE, true, pos.x, pos.y + 0.1, pos.z, particleVel.x, particleVel.y, particleVel.z);
		}
		if(groundPounding)
		{
			Vec3d particleVel = new Vec3d(0, 1, 0);
			for (int i = 0; i < random.nextInt(4) + 8; i++)
			{
				Vec3d pos = getPos().add((random.nextDouble() - 0.5) * 10.0, 5.0 * ((random.nextDouble() - 0.9) * 2), (random.nextDouble() - 0.5) * 10.0);
				world.addParticle(ParticleRegistry.GROUND_POUND, true, pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
			}
			fallDistance = 0f;
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
	
	@Redirect(method = "increaseTravelMotionStats", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V", ordinal = 3))
	void addExhaustion(PlayerEntity instance, float exhaustion)
	{
		if(!((WingedPlayerEntity)instance).isWingsVisible())
			instance.addExhaustion(exhaustion);
	}
	
	@Override
	public boolean shouldIgnoreSlowdown()
	{
		return ignoreSlowdown;
	}
	
	@Override
	public void setIgnoreSlowdown(boolean ignoreSlowdown)
	{
		this.ignoreSlowdown = ignoreSlowdown;
	}
}
