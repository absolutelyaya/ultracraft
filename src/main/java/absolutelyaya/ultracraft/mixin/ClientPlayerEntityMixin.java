package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
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
	Vec3d dashDir = Vec3d.ZERO;
	Vec3d slideDir = Vec3d.ZERO;
	
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
				Vec3d dir = new Vec3d(getX() - lastX, 0f, getZ() - lastZ).normalize();
				if(dir.lengthSquared() < 0.9f)
					dir = Vec3d.fromPolar(0f, getYaw()).normalize();
				
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeUuid(((PlayerEntity)winged).getUuid());
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
		if(UltracraftClient.isHiVelEnabled())
		{
			if(client.options.sprintKey.isPressed() && !horizontalCollision && !jumping)
			{
				BlockPos pos = new BlockPos(getPos().add(Vec3d.fromPolar(0f, getYaw()).normalize()));
				if(!isSprinting())
					setSprinting(!world.getBlockState(new BlockPos(getPos().subtract(0f, 0.49f, 0f))).isAir() &&
										 !world.getBlockState(pos).isSolidBlock(world, pos));
			}
			else
				setSprinting(false);
			if(isSprinting())
			{
				if(isSprinting() != lastSprinting)
					sendSprintingPacket();
				setVelocity(slideDir.multiply(0.5).add(0f, getVelocity().y, 0f));
				ci.cancel();
			}
			if(winged.isDashing())
			{
				setVelocity(dashDir);
				if(jumping && !world.getBlockState(new BlockPos(getPos().subtract(0f, 0.49f, 0f))).isAir())
				{
					winged.onDashJump();
					if(!winged.consumeStamina())
						setVelocity(dashDir.multiply(0.3));
					addVelocity(0f, getJumpVelocity(), 0f);
				}
				ci.cancel();
			}
			if(winged.wasDashing())
			{
				if(onGround)
					setVelocity(Vec3d.ZERO);
				else
					setVelocity(dashDir.multiply(0.3));
				dashDir = Vec3d.ZERO;
				ci.cancel();
			}
			if(ci.isCancelled())
			{
				if(tryDash(winged))
					setSprinting(false);
				networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(getX(), getY(), getZ(), getYaw(), getPitch(), onGround));
				lastX = getX();
				lastBaseY = getY();
				lastZ = getZ();
				ticksSinceLastPositionPacketSent = 0;
				lastYaw = getYaw();
				lastOnGround = onGround;
				autoJumpEnabled = client.options.getAutoJump().getValue();
			}
		}
	}
	
	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z"))
	boolean onTickMovement(Input instance)
	{
		WingedPlayerEntity winged = this;
		if(winged.isWingsVisible())
			return true;
		else
			return input.hasForwardMovement();
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
		if(UltracraftClient.isHiVelEnabled())
		{
			this.setFlag(3, sprinting); //sprinting flag
			if(sprinting && !lastSprinting)
				slideDir = Vec3d.fromPolar(0f, getYaw()).normalize();
			ticksSinceSprintingChanged = 0;
			ci.cancel();
		}
	}
	
	@Inject(method = "canSprint", at = @At(value = "HEAD"), cancellable = true)
	void onCanSprint(CallbackInfoReturnable<Boolean> cir)
	{
		WingedPlayerEntity winged = this;
		if((winged.isWingsVisible() && !isSprinting() && !onGround) || winged.isDashing())
			cir.setReturnValue(false);
	}
}
