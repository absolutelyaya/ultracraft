package absolutelyaya.ultracraft.particle;

import absolutelyaya.ultracraft.registry.ParticleRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

public class ParryIndicatorParticleEffect implements ParticleEffect
{
	final boolean unparriable;
	
	public ParryIndicatorParticleEffect(boolean unparriable)
	{
		this.unparriable = unparriable;
	}
	
	@Override
	public ParticleType<?> getType()
	{
		return ParticleRegistry.PARRY_INDICATOR;
	}
	
	@Override
	public void write(PacketByteBuf buf)
	{
		buf.writeBoolean(unparriable);
	}
	
	@Override
	public String asString()
	{
		return String.format("%s %s", Registries.PARTICLE_TYPE.getId(this.getType()), unparriable);
	}
	
	public static class Factory implements ParticleEffect.Factory<ParryIndicatorParticleEffect>
	{
		@Override
		public ParryIndicatorParticleEffect read(ParticleType<ParryIndicatorParticleEffect> type, StringReader reader) throws CommandSyntaxException
		{
			reader.expect(' ');
			return new ParryIndicatorParticleEffect(reader.readBoolean());
		}
		
		@Override
		public ParryIndicatorParticleEffect read(ParticleType<ParryIndicatorParticleEffect> type, PacketByteBuf buf)
		{
			return new ParryIndicatorParticleEffect(buf.readBoolean());
		}
	}
}
