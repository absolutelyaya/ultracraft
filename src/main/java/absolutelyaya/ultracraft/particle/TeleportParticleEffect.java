package absolutelyaya.ultracraft.particle;

import absolutelyaya.ultracraft.registry.ParticleRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class TeleportParticleEffect implements ParticleEffect
{
	double size;
	
	public TeleportParticleEffect(double size)
	{
		this.size = size;
	}
	
	@Override
	public ParticleType<?> getType()
	{
		return ParticleRegistry.TELEPORT;
	}
	
	@Override
	public void write(PacketByteBuf buf)
	{
		buf.writeDouble(size);
	}
	
	@Override
	public String asString()
	{
		return null;
	}
	
	public static class Factory implements ParticleEffect.Factory<TeleportParticleEffect>
	{
		@Override
		public TeleportParticleEffect read(ParticleType<TeleportParticleEffect> type, StringReader reader) throws CommandSyntaxException
		{
			reader.expect(' ');
			return new TeleportParticleEffect(reader.readDouble());
		}
		
		@Override
		public TeleportParticleEffect read(ParticleType<TeleportParticleEffect> type, PacketByteBuf buf)
		{
			return new TeleportParticleEffect(buf.readDouble());
		}
	}
}
