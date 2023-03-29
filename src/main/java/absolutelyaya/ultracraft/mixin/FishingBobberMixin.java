package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingBobberEntity.class)
public class FishingBobberMixin
{
	ItemStack lastCatch;
	
	@Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z", ordinal = 0))
	public boolean onItemCaught(World world, Entity entity)
	{
		lastCatch = ((ItemEntity)entity).getStack();
		return world.spawnEntity(entity);
	}
	
	@Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;increaseStat(Lnet/minecraft/util/Identifier;I)V"))
	public void onFishCaught(PlayerEntity player, Identifier stat, int amount)
	{
		player.increaseStat(Stats.FISH_CAUGHT, 1);
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeItemStack(lastCatch);
		ServerPlayNetworking.send((ServerPlayerEntity)player, PacketRegistry.CATCH_FISH_ID, buf);
	}
}
