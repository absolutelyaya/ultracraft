package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import com.chocohead.mm.api.ClassTinkerers;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements WingedPlayerEntity
{
	@Shadow public abstract boolean isCreative();
	
	@Shadow @Final @Mutable private static Map<EntityPose, EntityDimensions> POSE_DIMENSIONS;
	
	@Shadow @Final private PlayerAbilities abilities;
	
	@Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);
	
	@Shadow public abstract void playSound(SoundEvent event, SoundCategory category, float volume, float pitch);
	
	boolean wingsActive, groundPounding, ignoreSlowdown;
	byte wingState, lastState;
	float wingAnimTime;
	int dashingTicks = -2, slamDamageCooldown, stamina, wingHintDisplayTicks;
	GunCooldownManager gunCDM;
	Multimap<EntityAttribute, EntityAttributeModifier> curSpeedMod;
	
	private final Vec3d[] curWingPose = new Vec3d[] {new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f), new Vec3d(0.0f, 0.0f, 0.0f)};
	
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
		boolean hiVelMode = winged.isWingsActive();
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
		if(wingsActive && source.isOf(DamageTypes.FALL) && ((!world.isClient && !world.getGameRules().get(GameruleRegistry.HIVEL_FALLDAMAGE).get()) ||
				   getSteppingBlockState().getBlock() instanceof FluidBlock))
			cir.setReturnValue(false);
	}
	
	@Inject(method = "damage", at = @At("TAIL"))
	void afterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		if(wingsActive && !source.isIn(DamageTypeTags.IS_PER_TICK) && !source.isOf(DamageTypes.OUT_OF_WORLD))
		{
			if(source.isOf(DamageSources.GUN) || source.isOf(DamageSources.SHOTGUN))
				timeUntilRegen = 9;
			else
				timeUntilRegen = 11;
		}
	}
	
	@Inject(method = "isSwimming", at = @At("HEAD"), cancellable = true)
	void onIsSwimming(CallbackInfoReturnable<Boolean> cir)
	{
		if(wingsActive)
			cir.setReturnValue(false);
	}
	
	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	void onShouldSwimInFluids(CallbackInfoReturnable<Boolean> cir)
	{
		if(wingsActive || abilities.flying)
			cir.setReturnValue(false);
	}
	
	@Override
	public void setWingState(byte state)
	{
		if(wingState != state)
		{
			setWingAnimTime(0);
			lastState = wingState;
			wingState = state;
		}
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
	public Vec3d[] getWingPose()
	{
		Vec3d[] pose = new Vec3d[8];
		System.arraycopy(curWingPose, 0, pose, 0, 8);
		return pose;
	}
	
	@Override
	public void setWingPose(Vec3d[] pose)
	{
		System.arraycopy(pose, 0, curWingPose, 0, 8);
	}
	
	@Override
	public void onDash()
	{
		dashingTicks = 3;
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.75f, 1.6f);
	}
	
	@Override
	public void cancelDash()
	{
		dashingTicks = -2;
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
		return dashingTicks + 1 >= 0;
	}
	
	@Override
	public boolean wasDashing(int i)
	{
		return dashingTicks + i >= 0;
	}
	
	@Override
	public int getDashingTicks()
	{
		return dashingTicks;
	}
	
	@Override
	public void setWingsVisible(boolean b)
	{
		wingsActive = b;
		setWingState((byte)(b ? 1 : 0));
		wingHintDisplayTicks = 60;
		if(b)
		{
			curSpeedMod = getSpeedMod();
			getAttributes().addTemporaryModifiers(curSpeedMod);
		}
		else if(curSpeedMod != null)
		{
			getAttributes().removeModifiers(curSpeedMod);
			curSpeedMod = null;
		}
	}
	
	Multimap<EntityAttribute, EntityAttributeModifier> getSpeedMod()
	{
		Multimap<EntityAttribute, EntityAttributeModifier> speedMod = HashMultimap.create();
		speedMod.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("9c92fac8-0018-11ee-be56-0242ac120002"), "spd_up",
				0.2f * world.getGameRules().getInt(GameruleRegistry.HIVEL_SPEED), EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
		return speedMod;
	}
	
	@Override
	public void updateSpeedGamerule()
	{
		if(isWingsActive() && curSpeedMod != null)
		{
			getAttributes().removeModifiers(curSpeedMod);
			curSpeedMod = getSpeedMod();
			getAttributes().addTemporaryModifiers(curSpeedMod);
		}
	}
	
	@Override
	public boolean isWingsActive()
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
		else
			playSound(SoundEvents.BLOCK_ANVIL_LAND, 0.5f, 1.8f);
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
	public void startSlam()
	{
		groundPounding = true;
	}
	
	@Override
	public void endSlam(boolean strong)
	{
		groundPounding = false;
		if(!onGround)
			return;
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS,
				strong ? 1f : 0.75f, strong ? 0.75f : 1.25f);
		world.getOtherEntities(this, getBoundingBox().expand(0f, 1f, 0f).offset(0f, -0.5f, 0f)).forEach(e ->
				e.damage(DamageSources.get(world, DamageSources.POUND, this), slamDamageCooldown > 0 ? 1 : 6));
		slamDamageCooldown = 30;
		if(!strong)
			return;
		world.getOtherEntities(this, getBoundingBox().expand(3f, 0.5f, 3f)).forEach(e -> {
			if((e instanceof LivingEntityAccessor l) && l.takePunchKnockback())
				e.addVelocity(0f, 1f, 0f);
		});
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
		if(wingHintDisplayTicks > 0)
			wingHintDisplayTicks--;
		if(dashingTicks > -60)
			dashingTicks--;
		if(slamDamageCooldown > 0)
			slamDamageCooldown--;
	}
	
	@Inject(method = "tickMovement", at = @At("TAIL"))
	void onTickMovement(CallbackInfo ci)
	{
		if(dashingTicks >= -1)
		{
			Vec3d dir = getVelocity();
			Vec3d particleVel = new Vec3d(-dir.x, 0, -dir.z).multiply(random.nextDouble() * 0.33 + 0.1);
			Vec3d pos = getPos().add((random.nextDouble() - 0.5) * getWidth(),
					random.nextDouble() * getHeight(), (random.nextDouble() - 0.5) * getWidth()).add(dir.multiply(0.25));
			world.addParticle(ParticleRegistry.DASH, true, pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
		}
		if(isSprinting() && isWingsActive())
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
		{
			stamina++;
			if(stamina % 30 == 0)
				playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1f + stamina / 30f * 0.1f);
		}
	}
	
	@Inject(method = "adjustMovementForSneaking", at = @At("HEAD"), cancellable = true)
	void onAdjustMovementForSneaking(Vec3d movement, MovementType type, CallbackInfoReturnable<Vec3d> cir)
	{
		if(isWingsActive())
			cir.setReturnValue(movement);
	}
	
	@Redirect(method = "increaseTravelMotionStats", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V", ordinal = 3))
	void addExhaustion(PlayerEntity instance, float exhaustion)
	{
		if(!((WingedPlayerEntity)instance).isWingsActive())
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
	
	@Override
	public boolean canBreatheInWater()
	{
		return isWingsActive() && !world.getGameRules().getBoolean(GameruleRegistry.HIVEL_DROWNING);
	}
}
