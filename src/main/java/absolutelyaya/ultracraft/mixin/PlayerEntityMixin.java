package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.accessor.EntityAccessor;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.components.IProgressionComponent;
import absolutelyaya.ultracraft.components.IWingedPlayerComponent;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.entity.other.BackTank;
import absolutelyaya.ultracraft.item.IOverrideMeleeDamageType;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
	
	@Shadow public abstract boolean isSpectator();
	
	Multimap<EntityAttribute, EntityAttributeModifier> curSpeedMod;
	BackTank backtank;
	
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
		((EntityAccessor)this).setTargettableSupplier(() -> !isCreative() && !isSpectator());
	}
	
	@Redirect(method = "updatePose", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"))
	void onUpdatePose(PlayerEntity instance, EntityPose entityPose)
	{
		WingedPlayerEntity winged = ((WingedPlayerEntity)instance);
		boolean hiVelMode = UltraComponents.WING_DATA.get(winged).isVisible();
		if(hiVelMode)
		{
			if(UltraComponents.WINGED_ENTITY.get(winged).isDashing())
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
		if(UltraComponents.WINGED_ENTITY.get(this).isDashing() && !source.isIn(DamageTypeTags.UNDODGEABLE))
			cir.setReturnValue(false);
		if(isWingsActive() && source.isOf(DamageTypes.FALL) && ((!getWorld().isClient && !getWorld().getGameRules().get(GameruleRegistry.HIVEL_FALLDAMAGE).get()) ||
				   getSteppingBlockState().getBlock() instanceof FluidBlock))
			cir.setReturnValue(false);
	}
	
	@Inject(method = "damage", at = @At("TAIL"))
	void afterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		if(isWingsActive() && !source.isIn(DamageTypeTags.IS_PER_TICK) && !source.isOf(DamageTypes.OUT_OF_WORLD))
		{
			if(source.isOf(DamageSources.GUN) || source.isOf(DamageSources.SHOTGUN))
				timeUntilRegen = 9;
			else
				timeUntilRegen = 11 + getWorld().getGameRules().getInt(GameruleRegistry.INVINCIBILITY);
		}
		UltraComponents.WINGED_ENTITY.get(this).setBloodHealCooldown(4);
	}
	
	@Inject(method = "isSwimming", at = @At("HEAD"), cancellable = true)
	void onIsSwimming(CallbackInfoReturnable<Boolean> cir)
	{
		if(isWingsActive())
			cir.setReturnValue(false);
	}
	
	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	void onShouldSwimInFluids(CallbackInfoReturnable<Boolean> cir)
	{
		if(isWingsActive() || abilities.flying)
			cir.setReturnValue(false);
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
	
	Multimap<EntityAttribute, EntityAttributeModifier> getSpeedMod()
	{
		Multimap<EntityAttribute, EntityAttributeModifier> speedMod = HashMultimap.create();
		speedMod.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("9c92fac8-0018-11ee-be56-0242ac120002"), "spd_up",
				0.2f * getWorld().getGameRules().getInt(GameruleRegistry.HIVEL_SPEED), EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
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
	
	public boolean isWingsActive()
	{
		return UltraComponents.WING_DATA.get(this).isVisible();
	}
	
	@Override
	public void startSlam()
	{
		UltraComponents.WINGED_ENTITY.get(this).setSlamming(true);
	}
	
	@Override
	public void endSlam(boolean strong)
	{
		IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(this);
		winged.setSlamming(false);
		if(!isOnGround())
			return;
		getWorld().playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS,
				strong ? 1f : 0.75f, strong ? 0.75f : 1.25f);
		getWorld().getOtherEntities(this, getBoundingBox().expand(0f, 1f, 0f).offset(0f, -0.5f, 0f)).forEach(e ->
				e.damage(DamageSources.get(getWorld(), DamageSources.POUND, this), winged.getSlamDamageCooldown() > 0 ? 1 : 6));
		winged.setSlamDamageCooldown(30);
		if(!strong)
			return;
		getWorld().getOtherEntities(this, getBoundingBox().expand(3f, 0.5f, 3f)).forEach(e -> {
			if((e instanceof LivingEntityAccessor l) && l.takePunchKnockback())
				e.addVelocity(0f, 1f, 0f);
		});
	}
	
	@Inject(method = "tick", at = @At("TAIL"))
	void onTick(CallbackInfo ci)
	{
		if(!getWorld().isClient() && getMainHandStack().isOf(ItemRegistry.FLAMETHROWER) && (backtank == null || backtank.isRemoved()))
			backtank = BackTank.spawn(getWorld(), this);
	}
	
	@Inject(method = "tickMovement", at = @At("TAIL"))
	void onTickMovement(CallbackInfo ci)
	{
		if(UltraComponents.WINGED_ENTITY.get(this).getDashingTicks() >= -1)
		{
			Vec3d dir = getVelocity();
			Vec3d particleVel = new Vec3d(-dir.x, 0, -dir.z).multiply(random.nextDouble() * 0.33 + 0.1);
			Vec3d pos = getPos().add((random.nextDouble() - 0.5) * getWidth(),
					random.nextDouble() * getHeight(), (random.nextDouble() - 0.5) * getWidth()).add(dir.multiply(0.25));
			getWorld().addParticle(ParticleRegistry.DASH, true, pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
		}
		if(isSprinting() && isWingsActive())
		{
			Vec3d dir = getVelocity().multiply(1.0, 0.0, 1.0).normalize();
			Vec3d particleVel = new Vec3d(-dir.x, -dir.y, -dir.z).multiply(random.nextDouble() * 0.1 + 0.025);
			Vec3d pos = getPos().add(dir.multiply(1.5));
			getWorld().addParticle(ParticleRegistry.SLIDE, true, pos.x, pos.y + 0.1, pos.z, particleVel.x, particleVel.y, particleVel.z);
		}
		if(UltraComponents.WINGED_ENTITY.get(this).isSlamming())
		{
			Vec3d particleVel = new Vec3d(0, 1, 0);
			for (int i = 0; i < random.nextInt(4) + 8; i++)
			{
				Vec3d pos = getPos().add((random.nextDouble() - 0.5) * 10.0, 5.0 * ((random.nextDouble() - 0.9) * 2), (random.nextDouble() - 0.5) * 10.0);
				getWorld().addParticle(ParticleRegistry.GROUND_POUND, true, pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
			}
			fallDistance = 0f;
		}
		if(backtank != null && !backtank.isRemoved())
			backtank.positionSelf(this);
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
		if(!UltraComponents.WING_DATA.get(instance).isVisible())
			instance.addExhaustion(exhaustion);
	}
	
	@ModifyConstant(method = "getOffGroundSpeed", constant = @Constant(floatValue = 0.02f))
	float modifyAirControl(float val)
	{
		if(isWingsActive() && UltraComponents.WINGED_ENTITY.get(this).isAirControlIncreased())
			return 0.05f;
		else
			return val;
	}
	
	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	void onWriteCustomData(NbtCompound nbt, CallbackInfo ci)
	{
		NbtCompound progression = new NbtCompound();
		UltraComponents.PROGRESSION.get(this).writeToNbt(progression);
		nbt.put("progression", progression);
	}
	
	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	void onReadCustomData(NbtCompound nbt, CallbackInfo ci)
	{
		if(nbt.contains("progression", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound progression = nbt.getCompound("progression");
			IProgressionComponent progressionComponent = UltraComponents.PROGRESSION.get(this);
			progressionComponent.readFromNbt(progression);
			progressionComponent.sync();
		}
	}
	
	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSources;playerAttack(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/entity/damage/DamageSource;"))
	DamageSource onGetDamageSource(net.minecraft.entity.damage.DamageSources instance, PlayerEntity attacker)
	{
		if(attacker.getMainHandStack().getItem() instanceof IOverrideMeleeDamageType weapon)
			return weapon.getDamageSource(attacker.getWorld(), attacker);
		return instance.playerAttack(attacker);
	}
	
	@Override
	public boolean canBreatheInWater()
	{
		return isWingsActive() && !getWorld().getGameRules().getBoolean(GameruleRegistry.HIVEL_DROWNING);
	}
	
	@Override
	public void setFocusedTerminal(TerminalBlockEntity terminal)
	{
	
	}
	
	@Override
	public TerminalBlockEntity getFocusedTerminal()
	{
		return null;
	}
	
	@Override
	public boolean isOpped()
	{
		return getPermissionLevel() >= 2;
	}
	
	@Override
	public void setBackTank(BackTank backtank)
	{
		this.backtank = backtank;
	}
	
	@Override
	public BackTank getBacktank()
	{
		return backtank;
	}
}
