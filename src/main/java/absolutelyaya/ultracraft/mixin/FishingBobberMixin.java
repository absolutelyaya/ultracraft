package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberMixin extends ProjectileEntity
{
	ItemStack lastCatch;
	
	public FishingBobberMixin(EntityType<? extends ProjectileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
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
	
	@Redirect(method = "tickFishingLogic", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
	boolean onFishLogic(BlockState instance, Block block)
	{
		return instance.isOf(Blocks.WATER) || instance.isOf(BlockRegistry.BLOOD);
	}
	
	@ModifyArg(method = "tickFishingLogic", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"))
	ParticleEffect onSwimmingStart(ParticleEffect effect)
	{
		if(world.getBlockState(getBlockPos()).isOf(Blocks.WATER))
			return effect;
		if(effect.equals(ParticleTypes.SPLASH) || effect.equals(ParticleTypes.FISHING))
			return ParticleRegistry.BLOOD_SPLASH;
		else if(effect.equals(ParticleTypes.BUBBLE))
			return ParticleRegistry.BLOOD_BUBBLE;
		else return effect;
	}
}
