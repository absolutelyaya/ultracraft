package absolutelyaya.ultracraft.particle.goop;

import absolutelyaya.ultracraft.registry.ParticleRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public class GoopStringParticleEffect extends AbstractGoopParticleEffect
{
	public GoopStringParticleEffect(Vec3d color, float scale)
	{
		super(color, scale);
	}
	
	@Override
	public ParticleType<?> getType()
	{
		return ParticleRegistry.GOOP_STRING;
	}
	
	public static class Factory implements ParticleEffect.Factory<GoopStringParticleEffect>
	{
		@Override
		public GoopStringParticleEffect read(ParticleType type, StringReader reader) throws CommandSyntaxException
		{
			Vec3d Vec3d = AbstractGoopParticleEffect.readVec3(reader);
			reader.expect(' ');
			float f = reader.readFloat();
			return new GoopStringParticleEffect(Vec3d, f);
		}
		
		@Override
		public GoopStringParticleEffect read(ParticleType type, PacketByteBuf buf)
		{
			return new GoopStringParticleEffect(readVec3(buf), buf.readFloat());
		}
	}
}
