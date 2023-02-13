package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.ClientPlayerAccessor;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.block.IPunchableBlock;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.registry.BlockTagRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.MusicSound;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{
	@Shadow @Nullable public ClientPlayerEntity player;
	
	@Shadow @Nullable public HitResult crosshairTarget;
	
	@Shadow @Nullable public ClientWorld world;
	
	@Redirect(method = "handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
	void OnHandSwap(ClientPlayNetworkHandler instance, Packet<?> packet)
	{
		if(player == null || !((ClientPlayerAccessor)player).Punch())
			return;
		
		Entity entity = null;
		if(crosshairTarget == null)
			return;
		if(crosshairTarget.getType().equals(HitResult.Type.ENTITY))
		{
			entity = ((EntityHitResult)crosshairTarget).getEntity();
		}
		else if(crosshairTarget.getType().equals(HitResult.Type.BLOCK))
		{
			BlockHitResult hit = ((BlockHitResult)crosshairTarget);
			BlockState state = player.world.getBlockState(hit.getBlockPos());
			if(state.getBlock() instanceof IPunchableBlock punchable)
			{
				if (punchable.onPunch(player, hit.getBlockPos()))
					return; //if punch interaction was successful, don't display break particles and stuff
			}
			Vec3d pos = hit.getPos();
			for (int i = 0; i < 6; i++)
			{
				player.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.x, pos.y, pos.z, 0f, 0f, 0f);
			}
			player.playSound(state.getSoundGroup().getHitSound(), 1f, 1f);
			if(state.isIn(BlockTagRegistry.PUNCH_BREAKABLE))
				player.world.breakBlock(hit.getBlockPos(), true, player);
			return;
		}
		
		Vec3d forward = player.getRotationVecClient();
		Vec3d pos = player.getCameraPosVec(0f).add(forward.normalize().multiply(1));
		List<ProjectileEntity> projectiles = player.world.getEntitiesByClass(ProjectileEntity.class,
				new Box(pos.x - 0.75f, pos.y - 0.75f, pos.z - 0.75f, pos.x + 0.75f, pos.y + 0.75f, pos.z + 0.75f),
				(e) -> !((ProjectileEntityAccessor)e).isParried());
		if(projectiles.size() > 0)
			entity = getNearestProjectile(projectiles, pos);
		
		if(entity != null)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeInt(entity.getId());
			buf.writeBoolean(!player.getStackInHand(Hand.OFF_HAND).isEmpty());
			NetworkManager.sendToServer(PacketRegistry.PUNCH_ENTITY_PACKET_ID, buf);
		}
	}
	
	ProjectileEntity getNearestProjectile(List<ProjectileEntity> projectiles, Vec3d to)
	{
		double nearestDistance = 100.0;
		ProjectileEntity nearest = null;
		
		for (ProjectileEntity e : projectiles)
		{
			double distance = e.squaredDistanceTo(to);
			if(distance < nearestDistance)
			{
				nearest = e;
				nearestDistance = distance;
			}
		}
		return nearest;
	}
	
	@Inject(method = "getMusicType", at = @At("RETURN"), cancellable = true)
	void onGetMusicType(CallbackInfoReturnable<MusicSound> cir)
	{
		if (cir.getReturnValue().equals(MusicType.MENU))
			cir.setReturnValue(new MusicSound(SoundRegistry.THE_FIRE_IS_GONE.get(), 20, 600, true));
	}
	
	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	void onDoAttack(CallbackInfoReturnable<Boolean> cir)
	{
		if(player.getInventory().getMainHandStack().getItem() instanceof AbstractWeaponItem)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			NetworkManager.sendToServer(PacketRegistry.PRIMARY_SHOT_PACKET_ID, buf);
			cir.setReturnValue(false);
		}
	}
	
	@Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
	void onHandleBlockBreaking(boolean bl, CallbackInfo ci)
	{
		if(player.getInventory().getMainHandStack().getItem() instanceof AbstractWeaponItem)
			ci.cancel();
	}
}
