package absolutelyaya.ultracraft.particle;

import absolutelyaya.ultracraft.registry.ParticleRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class ExplosionParticleEffect implements ParticleEffect
{
	double size;
	
	public ExplosionParticleEffect(double size)
	{
		this.size = size;
	}
	
	@Override
	public ParticleType<?> getType()
	{
		return ParticleRegistry.EXPLOSION;
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
	
	public static class Factory implements ParticleEffect.Factory<ExplosionParticleEffect>
	{
		@Override
		public ExplosionParticleEffect read(ParticleType<ExplosionParticleEffect> type, StringReader reader) throws CommandSyntaxException
		{
			reader.expect(' ');
			return new ExplosionParticleEffect(reader.readDouble());
		}
		
		@Override
		public ExplosionParticleEffect read(ParticleType<ExplosionParticleEffect> type, PacketByteBuf buf)
		{
			return new ExplosionParticleEffect(buf.readDouble());
		}
	}
}
