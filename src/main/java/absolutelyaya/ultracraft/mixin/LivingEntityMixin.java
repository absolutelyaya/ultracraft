package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ClientPlayerAccessor;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ClientPlayerAccessor
{
	int punchTicks, punchDuration = 6;
	boolean punching;
	float punchProgress, prevPunchProgress;
	
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
	}
	
	@Inject(method = "damage", at = @At("RETURN"))
	void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		if(!cir.getReturnValue() || world.isClient)
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
			ServerPlayNetworking.send((ServerPlayerEntity)player, PacketRegistry.BLEED_PACKET_ID, buf);
		}
		for (PlayerEntity player : heal)
		{
			player.heal(amount / 1.5f);
			player.getHungerManager().add((int)(amount / 1.5f), 5f);
		}
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
	public boolean Punch()
	{
		if(!punching)
		{
			punchTicks = 0;
			punching = true;
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
}
