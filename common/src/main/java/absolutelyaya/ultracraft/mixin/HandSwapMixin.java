package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.ClientPlayerAccessor;
import absolutelyaya.ultracraft.registry.BlockTagRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class HandSwapMixin
{
	@Shadow @Nullable public ClientPlayerEntity player;
	
	@Shadow @Nullable public HitResult crosshairTarget;
	
	@Redirect(method = "handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
	void OnHandSwap(ClientPlayNetworkHandler instance, Packet<?> packet)
	{
		if(player != null)
		{
			if(!((ClientPlayerAccessor)player).Punch())
				return;
			
			if(crosshairTarget != null)
			{
				if(crosshairTarget.getType().equals(HitResult.Type.ENTITY))
				{
					Entity entity = ((EntityHitResult)crosshairTarget).getEntity();
					PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
					buf.writeInt(entity.getId());
					buf.writeBoolean(!player.getStackInHand(Hand.OFF_HAND).isEmpty());
					NetworkManager.sendToServer(PacketRegistry.PUNCH_ENTITY_PACKET_ID, buf);
				}
				else if(crosshairTarget.getType().equals(HitResult.Type.BLOCK))
				{
					BlockHitResult hit = ((BlockHitResult)crosshairTarget);
					BlockState state = player.world.getBlockState(hit.getBlockPos());
					Vec3d pos = hit.getPos();
					for (int i = 0; i < 6; i++)
					{
						player.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.x, pos.y, pos.z, 0f, 0f, 0f);
					}
					player.playSound(state.getSoundGroup().getHitSound(), 1f, 1f);
					if(state.isIn(BlockTagRegistry.PUNCH_BREAKABLE))
						player.world.breakBlock(hit.getBlockPos(), true, player);
				}
			}
		}
	}
}
