package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;
import java.util.function.Supplier;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAccessor
{
	@Shadow public abstract boolean isPushable();
	
	@Shadow public abstract void swingHand(Hand hand);
	
	@Shadow protected boolean jumping;
	
	@Shadow protected abstract void jump();
	
	@Shadow private int jumpingCooldown;
	
	@Shadow protected abstract float getBaseMovementSpeedMultiplier();
	
	@Shadow public abstract float getMovementSpeed();
	
	@Shadow public abstract boolean hasStatusEffect(StatusEffect effect);
	
	@Shadow protected abstract boolean shouldSwimInFluids();
	
	@Shadow public abstract boolean isClimbing();
	
	@Shadow public abstract boolean canWalkOnFluid(FluidState state);
	
	@Shadow public abstract void updateLimbs(boolean flutter);
	
	final int punchDuration = 6;
	Supplier<Boolean> canBleedSupplier = () -> true, takePunchKnockpackSupplier = this::isPushable; //TODO: add Sandy Enemies (eventually)
	int punchTicks;
	boolean punching;
	float punchProgress, prevPunchProgress, recoil;
	
	boolean timeFrozen;
	
	public LivingEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	void onTick(CallbackInfo ci)
	{
		timeFrozen = Ultracraft.isTimeFrozen();
		if(!timeFrozen || punchTicks < 2)
			punchTick();
		recoil = MathHelper.lerp(0.3f, recoil, 0f);
	}
	
	@ModifyArgs(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
	void modifyAppliedDamage(Args args)
	{
		DamageSource source = args.get(0);
		float amount = args.get(1);
		if(source.isIn(DamageTypeTags.ULTRACRAFT) && !source.isOf(DamageSources.SWORDSMACHINE) && !((Object)this instanceof AbstractUltraHostileEntity))
			args.set(1, amount * 2.5f);
	}
	
	@SuppressWarnings("EqualsBetweenInconvertibleTypes")
	@Inject(method = "damage", at = @At("RETURN"))
	void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		if(!cir.getReturnValue() || world.isClient || !IsCanBleed())
			return;
		List<PlayerEntity> nearby = world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), getBoundingBox().expand(32), e -> true);
		List<PlayerEntity> heal = world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), getBoundingBox().expand(2), e -> !e.equals(this));
		for (PlayerEntity player : nearby)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeFloat(amount);
			buf.writeDouble(getPos().x);
			buf.writeDouble(getPos().y);
			buf.writeDouble(getPos().z);
			buf.writeDouble(getHeight() / 2);
			buf.writeBoolean(source.isOf(DamageSources.SHOTGUN));
			ServerPlayNetworking.send((ServerPlayerEntity)player, PacketRegistry.BLEED_PACKET_ID, buf);
		}
		GameruleRegistry.RegenOption healRule = world.getGameRules().get(GameruleRegistry.BLOODHEAL).get();
		if(!healRule.equals(GameruleRegistry.RegenOption.NEVER))
		{
			for (PlayerEntity player : heal)
			{
				if(healRule.equals(GameruleRegistry.RegenOption.ONLY_HIVEL) && (!(player instanceof WingedPlayerEntity winged) || !winged.isWingsActive()))
					continue;
				float healing = amount * (source.isOf(DamageSources.SHOTGUN) ? 1f : 2.5f);
				player.heal(healing);
				player.getHungerManager().add((int)(healing / 1.5f), 5f);
			}
		}
		if(source.isIn(DamageTypeTags.IS_PER_TICK))
			return;
		if(source.isOf(DamageSources.GUN) || source.isOf(DamageSources.SHOTGUN))
			timeUntilRegen = 9;
		if(source.isOf(DamageSources.SWORDSMACHINE))
			timeUntilRegen = 12;
	}
	
	@Inject(method = "getJumpVelocity", at = @At("RETURN"), cancellable = true)
	void onGetJumpVel(CallbackInfoReturnable<Float> cir)
	{
		
		
		if(this instanceof WingedPlayerEntity winged && winged.isWingsActive())
		{
			if(!world.isClient)
				cir.setReturnValue(cir.getReturnValue() + 0.1f * Math.max(world.getGameRules().getInt(GameruleRegistry.HIVEL_JUMP_BOOST) +
						(isTouchingWater() ? 0.5f : 0f), 0));
		}
	}
	
	Vec3d fluidMovement(double gravity, boolean falling, Vec3d motion)
	{
		gravity /= falling ? 16f : 3f;
		double vel = motion.y;
		if (falling && Math.abs(vel - gravity) > 0.15)
			vel = -0.15;
		else
			vel -= gravity;
		return new Vec3d(motion.x, vel, motion.z);
	}
	
	@Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;shouldSwimInFluids()Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	void onTravel(Vec3d movementInput, CallbackInfo ci, double d, boolean bl, FluidState fluidState)
	{
		if(!(this instanceof WingedPlayerEntity winged && winged.isWingsActive()) || ((PlayerEntity)winged).getAbilities().flying)
			return;
		if(!isTouchingWater() || shouldSwimInFluids() || canWalkOnFluid(fluidState))
			return;
		float f = isSprinting() ? 0.9F : getBaseMovementSpeedMultiplier();
		float g = 0.03f;
		if(fluidState.isIn(FluidTags.WATER))
		{
			float h = (float)EnchantmentHelper.getDepthStrider((LivingEntity)(Object)this);
			if (h > 3.0f)
				h = 3.0f;
			if (!onGround)
				h *= 0.5f;
			if (h > 0.0f)
			{
				f += (0.54f - f) * h / 3.0f;
				g += (getMovementSpeed() - g) * h / 3.0f;
			}
			if (hasStatusEffect(StatusEffects.DOLPHINS_GRACE))
				f = 0.96f;
		}
		updateVelocity(g, movementInput);
		move(MovementType.SELF, getVelocity());
		Vec3d vec3d = getVelocity();
		if (horizontalCollision && isClimbing())
			vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
		if(winged.shouldIgnoreSlowdown())
			f = 0.9f;
		setVelocity(vec3d.multiply(f, 1f, f));
		setVelocity(fluidMovement(d, getVelocity().y <= 0f, getVelocity()));
		
		updateLimbs(this instanceof Flutterer);
		ci.cancel();
	}
	
	@ModifyConstant(method = "travel", constant = @Constant(floatValue = 0.91f))
	float modifyDrag(float val)
	{
		if(!(this instanceof WingedPlayerEntity winged && winged.isWingsActive()) || ((PlayerEntity)winged).getAbilities().flying || !winged.shouldIgnoreSlowdown())
			return val;
		return 0.925f;
	}
	
	@ModifyVariable(method = "travel", ordinal = 0, at = @At("STORE"))
	private double modifyGravity(double value)
	{
		if(!(this instanceof WingedPlayerEntity winged && winged.isWingsActive()) || ((PlayerEntity)winged).getAbilities().flying || touchingWater)
			return value;
		int val = (world.isClient ? getGravityReduction() : world.getGameRules().get(GameruleRegistry.HIVEL_SLOWFALL).get());
		return Math.max(value * (1f - 0.1f * val), 0.01f);
	}
	
	@ModifyVariable(method = "computeFallDamage", ordinal = 2, at = @At("STORE"))
	private float modifyFalldamageReduction(float value)
	{
		if(!(this instanceof WingedPlayerEntity winged && winged.isWingsActive()))
			return value;
		return value + ((world.getGameRules().get(GameruleRegistry.HIVEL_JUMP_BOOST).get() + 1) *
								(1f + (world.getGameRules().get(GameruleRegistry.HIVEL_SLOWFALL).get() / 2f)));
	}
	
	@ModifyVariable(method = "computeFallDamage", ordinal = 1, at = @At("LOAD"), argsOnly = true)
	private float modifyFalldamageMultiplier(float value)
	{
		if(!(this instanceof WingedPlayerEntity winged && winged.isWingsActive()))
			return value;
		return 0.5f;
	}
	
	@Inject(method = "computeFallDamage", at = @At("RETURN"), cancellable = true)
	private void onComputeFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir)
	{
		if(!(this instanceof WingedPlayerEntity winged && winged.isWingsActive()) && isSprinting())
			cir.setReturnValue(0);
	}
	
	@Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;shouldSwimInFluids()Z"))
	void onTickMovement(CallbackInfo ci)
	{
		if (this instanceof WingedPlayerEntity winged && winged.isWingsActive() && onGround && jumping && jumpingCooldown == 0)
		{
			jump();
			jumpingCooldown = 10;
		}
	}
	
	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	void onShouldSwimInFluids(CallbackInfoReturnable<Boolean> cir)
	{
		if(this instanceof WingedPlayerEntity winged && winged.isWingsActive() &&
				   world.getFluidState(((PlayerEntity)winged).getBlockPos()).isIn(FluidTags.WATER))
			cir.setReturnValue(false);
	}
	
	@Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
	void onCanWalkOnFluid(FluidState state, CallbackInfoReturnable<Boolean> cir)
	{
		if(this instanceof WingedPlayerEntity winged && winged.isWingsActive() && isSprinting())
			cir.setReturnValue(true);
	}
	
	void punchTick()
	{
		int i = punchDuration;
		if (punching)
		{
			++punchTicks;
			if (punchTicks >= i)
			{
				punchTicks = 0;
				punching = false;
			}
			else if(timeFrozen)
				punchTicks = 1;
		}
		else
			punchTicks = 0;
		
		prevPunchProgress = punchProgress;
		punchProgress = (float)punchTicks / (float)i;
		if(timeFrozen)
			prevPunchProgress = punchProgress;
	}
	
	@Override
	public boolean punch()
	{
		if(!punching)
		{
			punchTicks = 0;
			punching = true;
			if(!world.isClient)
				swingHand(Hand.OFF_HAND);
			return true;
		}
		return false;
	}
	
	@Override
	public float GetPunchProgress(float tickDelta)
	{
		float f = punchProgress - prevPunchProgress;
		if (f < 0.0F) {
			++f;
		}
		
		if (Ultracraft.isTimeFrozen())
			return punchProgress;
		else
			return prevPunchProgress + f * tickDelta;
	}
	
	@Override
	public boolean IsPunching()
	{
		return punching;
	}
	
	@Override
	public boolean IsCanBleed()
	{
		return canBleedSupplier.get();
	}
	
	@Override
	public void SetCanBleedSupplier(Supplier<Boolean> supplier)
	{
		canBleedSupplier = supplier;
	}
	
	@Override
	public boolean takePunchKnockback()
	{
		return takePunchKnockpackSupplier.get();
	}
	
	@Override
	public void SetTakePunchKnockbackSupplier(Supplier<Boolean> supplier)
	{
		takePunchKnockpackSupplier = supplier;
	}
	
	@Override
	public void addRecoil(float recoil)
	{
		this.recoil += recoil;
	}
	
	@Override
	public float getRecoil()
	{
		return recoil;
	}
}
