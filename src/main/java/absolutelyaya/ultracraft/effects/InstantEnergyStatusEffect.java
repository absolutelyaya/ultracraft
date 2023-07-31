package absolutelyaya.ultracraft.effects;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class InstantEnergyStatusEffect extends InstantStatusEffect
{
	public InstantEnergyStatusEffect(StatusEffectCategory statusEffectCategory, int i)
	{
		super(statusEffectCategory, i);
	}
	
	@Override
	public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier)
	{
		super.onApplied(entity, attributes, amplifier);
		if(entity instanceof ServerPlayerEntity player)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeInt(amplifier + 1);
			ServerPlayNetworking.send(player, PacketRegistry.REPLENISH_STAMINA, buf);
		}
	}
}
