package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements WingedPlayerEntity
{
	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile)
	{
		super(world, profile);
	}
	
	@Shadow public abstract boolean isSneaking();
	
	@Shadow private double lastX;
	
	@Shadow private double lastZ;
	
	@Shadow public abstract float getPitch(float tickDelta);
	
	@Shadow private boolean lastSneaking;
	
	@Shadow public abstract void setSprinting(boolean sprinting);
	
	@Shadow @Final public ClientPlayNetworkHandler networkHandler;
	@Shadow private double lastBaseY;
	@Shadow private int ticksSinceLastPositionPacketSent;
	@Shadow private float lastYaw;
	@Shadow private boolean lastOnGround;
	@Shadow private boolean autoJumpEnabled;
	@Shadow @Final protected MinecraftClient client;
	@Shadow public int ticksSinceSprintingChanged;
	
	@Shadow protected abstract void sendSprintingPacket();
	
	@Shadow private boolean lastSprinting;
	@Shadow public Input input;
	
	@Shadow public abstract boolean isSubmergedInWater();
	
	Vec3d dashDir = Vec3d.ZERO;
	Vec3d slideDir = Vec3d.ZERO;
	boolean groundPounding, lastGroundPounding, lastJumping, lastSprintPressed, lastTouchedWater, wasHiVel;
	int groundPoundTicks, ticksSinceLastGroundPound = -1, slideTicks;
	float slideVelocity, baseJumpVel = 0.42f;
	
	@Inject(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 0), cancellable = true)
	public void onSendSneakChangedPacket(CallbackInfo ci)
	{
		WingedPlayerEntity winged = this;
		if(tryDash(winged))
		{
			lastSneaking = isSneaking();
			ci.cancel();
		}
	}
	
	boolean tryDash(WingedPlayerEntity winged)
	{
		if(UltracraftClient.isHiVelEnabled() && !getAbilities().flying)
		{
			if(isSneaking() && !lastSneaking && !winged.isDashing())
			{
				if(!winged.consumeStamina())
					return true;
				if(groundPounding)
					groundPounding = false;
				Vec3d dir = new Vec3d(input.movementSideways, 0f, input.movementForward).rotateY(-(float)Math.toRadians(getYaw())).normalize();
				if(dir.lengthSquared() < 0.9f)
					dir = Vec3d.fromPolar(0f, getYaw()).normalize();
				
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeDouble(dir.x);
				buf.writeDouble(dir.y);
				buf.writeDouble(dir.z);
				ClientPlayNetworking.send(PacketRegistry.DASH_C2S_PACKET_ID, buf);
				setVelocity(dir);
				dashDir = dir;
				winged.onDash();
				return true;
			}
		}
		return false;
	}
	
	@Inject(method = "sendMovementPackets", at = @At(value = "HEAD"), cancellable = true)
	public void onSendMovementPackets(CallbackInfo ci)
	{
		WingedPlayerEntity winged = this;
		if(UltracraftClient.isHiVelEnabled() && !getAbilities().flying)
		{
			if(wasHiVel != UltracraftClient.isHiVelEnabled() && lastSprintPressed)
				setSliding(true, false); //when switching into HiVelMode while sprinting, reinitiate sliding
			if(ticksSinceLastGroundPound > -1 && ticksSinceLastGroundPound < 4)
				ticksSinceLastGroundPound++;
			//start slide or groundpound
			if(client.options.sprintKey.isPressed() && !lastSprintPressed && !groundPounding)
			{
				//start ground pound
				if(isUnSolid(world.getBlockState(new BlockPos(getPos().subtract(0f, 0.99f, 0f)))) && !verticalCollision && !getAbilities().flying)
				{
					groundPoundTicks = 0;
					groundPounding = true;
					setSprinting(false);
				}
				//start slide
				else if(!horizontalCollision && !jumping && !isDashing() && !wasDashing(2))
				{
					BlockPos pos = new BlockPos(getPos().add(Vec3d.fromPolar(0f, getYaw()).normalize()));
					setSprinting((!isUnSolid(world.getBlockState(new BlockPos(getPos().subtract(0f, 0.79f, 0f)))) || verticalCollision) &&
										 !world.getBlockState(pos).isSolidBlock(world, pos));
				}
				//cancel slide because it shouldn't be possible rn anyways
				else if(isSprinting())
					setSprinting(false);
				ci.cancel();
			}
			//stop sliding once conditions aren't met anymore
			else if(isSprinting())
			{
				if(jumping)
				{
					setVelocity(slideDir.multiply(slideVelocity * 1.5));
					addVelocity(0, baseJumpVel, 0);
				}
				setSprinting(client.options.sprintKey.isPressed() && !groundPounding && !horizontalCollision && !jumping);
				slideTicks++;
				if(isUnSolid(world.getBlockState(new BlockPos(getPos().subtract(0f, 0.25f, 0f)))))
					slideTicks = 0;
				ci.cancel();
			}
			//slide velocity
			if(isSprinting())
			{
				if(isSprinting() != lastSprinting)
					sendSprintingPacket();
				setVelocity(slideDir.multiply(slideVelocity).add(0f, getVelocity().y, 0f));
				ci.cancel();
			}
			//skip on liquids
			if(isSprinting() && !lastTouchedWater && world.getBlockState(new BlockPos(getPos().subtract(0f, 0.1, 0f))).getBlock() instanceof FluidBlock)
			{
				Vec3d vel = getVelocity();
				setVelocity(new Vec3d(vel.x, Math.max(baseJumpVel / 2f, vel.y * -0.75), vel.z));
			}
			//ground pound velocity
			if(groundPounding)
			{
				groundPoundTicks++;
				setVelocity(0, -2, 0);
				//landing
				if(verticalCollision)
				{
					groundPounding = false;
					ticksSinceLastGroundPound = 0;
				}
				ci.cancel();
			}
			//high jump after ground pound
			if(ticksSinceLastGroundPound > -1 && ticksSinceLastGroundPound < 4 && jumping && !lastJumping)
			{
				ticksSinceLastGroundPound = -1;
				setVelocity(0, groundPoundTicks / 20f + 0.42f * 1.5f, 0);
				groundPoundTicks = 0;
				ci.cancel();
			}
			//dash velocity
			if(winged.isDashing())
			{
				setVelocity(dashDir);
				//dash jump (preserves velocity)
				if(jumping && !lastJumping && !isUnSolid(world.getBlockState(new BlockPos(getPos().subtract(0f, 0.49f, 0f)))))
				{
					winged.onDashJump();
					if(!winged.consumeStamina())
						setVelocity(dashDir.multiply(0.3));
					addVelocity(0f, baseJumpVel, 0f);
				}
				ci.cancel();
			}
			//stop dashing
			if(winged.wasDashing())
			{
				if(onGround)
					setVelocity(Vec3d.ZERO);
				else
					setVelocity(dashDir.multiply(0.3));
				dashDir = Vec3d.ZERO;
				ci.cancel();
			}
			//update movement data
			if(ci.isCancelled())
			{
				networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(getX(), getY(), getZ(), getYaw(), getPitch(), onGround));
				lastX = getX();
				lastBaseY = getY();
				lastZ = getZ();
				ticksSinceLastPositionPacketSent = 0;
				lastYaw = getYaw();
				lastOnGround = onGround;
				autoJumpEnabled = client.options.getAutoJump().getValue();
				lastJumping = jumping;
				lastTouchedWater = world.getBlockState(new BlockPos(getPos().subtract(0f, 0.1, 0f))).getBlock() instanceof FluidBlock;
				if(lastGroundPounding != groundPounding)
				{
					boolean strong = client.options.sprintKey.isPressed() && consumeStamina();
					PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
					buf.writeBoolean(groundPounding);
					buf.writeBoolean(strong);
					ClientPlayNetworking.send(PacketRegistry.GROUND_POUND_PACKET_ID, buf);
					if(groundPounding)
						startGroundPound();
					else
						completeGroundPound(strong);
				}
				lastGroundPounding = groundPounding;
			}
			if(isSprinting() && slideVelocity > 0.33f && slideTicks > 50)
				slideVelocity = Math.max(0.33f, slideVelocity * 0.995f);
			lastSprintPressed = client.options.sprintKey.isPressed() && !isDashing() && !wasDashing(2);
		}
		wasHiVel = UltracraftClient.isHiVelEnabled();
	}
	
	void setSliding(boolean sliding, boolean last)
	{
		this.setFlag(3, sliding); //sprinting flag
		if(sliding && !last)
			slideDir = Vec3d.fromPolar(0f, getYaw()).normalize();
		ticksSinceSprintingChanged = 0;
		slideVelocity = Math.max(0.33f, (float)getVelocity().multiply(1f, 0f, 1f).length());
		slideTicks = 0;
	}
	
	boolean isUnSolid(BlockState state)
	{
		return state.isAir() || state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA);
	}
	
	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z"))
	boolean onTickMovement(Input instance)
	{
		if(UltracraftClient.isHiVelEnabled())
			return true;
		else
			return input.hasForwardMovement();
	}
	
	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V"))
	void onTickMovement(ClientPlayerEntity instance, boolean sprinting)
	{
		//cancel normal sprint triggers when in HiVelMode
		if(!UltracraftClient.isHiVelEnabled())
			setSprinting(sprinting);
	}
	
	@Inject(method = "tick", at = @At(value = "HEAD"))
	void onTick(CallbackInfo ci)
	{
		if(getWingAnimTime() < 1f)
			setWingAnimTime(getWingAnimTime() + MinecraftClient.getInstance().getTickDelta());
	}
	
	@Inject(method = "setSprinting", at = @At(value = "HEAD"), cancellable = true)
	public void onSetSprinting(boolean sprinting, CallbackInfo ci)
	{
		if(UltracraftClient.isHiVelEnabled() && isSprinting() != sprinting)
		{
			setSliding(sprinting, lastSprinting);
			ci.cancel();
		}
	}
	
	@Inject(method = "canSprint", at = @At(value = "HEAD"), cancellable = true)
	void onCanSprint(CallbackInfoReturnable<Boolean> cir)
	{
		WingedPlayerEntity winged = this;
		if((UltracraftClient.isHiVelEnabled() && !isSprinting() && !onGround) || winged.isDashing())
			cir.setReturnValue(false);
	}
	
	@Override
	public boolean isWingsVisible()
	{
		return UltracraftClient.isHiVelEnabled();
	}
}
