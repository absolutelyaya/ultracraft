package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity
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
	
	Vec3d dashDir = Vec3d.ZERO;
	
	@Inject(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 1), cancellable = true)
	public void onSendSneakChangedPacket(CallbackInfo ci)
	{
		WingedPlayerEntity winged = ((WingedPlayerEntity)this);
		if(UltracraftClient.isHiVelEnabled() && !getAbilities().flying)
		{
			if(isSneaking() && !winged.isDashing())
			{
				if(!winged.consumeStamina())
				{
					ci.cancel();
					return;
				}
				Vec3d dir = new Vec3d(getX() - lastX, 0f, getZ() - lastZ).normalize();
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
				lastSneaking = isSneaking();
				ci.cancel();
			}
		}
	}
	
	@Inject(method = "sendMovementPackets", at = @At(value = "HEAD"), cancellable = true)
	public void onSendMovementPackets(CallbackInfo ci)
	{
		WingedPlayerEntity winged = ((WingedPlayerEntity)this);
		if(UltracraftClient.isHiVelEnabled())
		{
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
		}
	}
}
