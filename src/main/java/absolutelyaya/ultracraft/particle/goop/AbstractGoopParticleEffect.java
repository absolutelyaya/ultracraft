package absolutelyaya.ultracraft.particle.goop;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;

public abstract class AbstractGoopParticleEffect implements ParticleEffect
{
	protected final Vec3d color;
	protected final float scale;
	
	public AbstractGoopParticleEffect(Vec3d color, float scale)
	{
		this.color = color;
		this.scale = MathHelper.clamp(scale, 0.01f, 4f);
	}
	
	public static Vec3d readVec3(StringReader reader) throws CommandSyntaxException
	{
		reader.expect(' ');
		float f = reader.readFloat();
		reader.expect(' ');
		float g = reader.readFloat();
		reader.expect(' ');
		float h = reader.readFloat();
		return new Vec3d(f, g, h);
	}
	
	public static Vec3d readVec3(PacketByteBuf buf)
	{
		return new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
	}
	
	@Override
	public void write(PacketByteBuf buf)
	{
		buf.writeFloat((float)this.color.getX());
		buf.writeFloat((float)this.color.getY());
		buf.writeFloat((float)this.color.getZ());
		buf.writeFloat(this.scale);
	}
	
	public String asString()
	{
		return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f",
				Registries.PARTICLE_TYPE.getId(this.getType()), this.color.getX(), this.color.getY(), this.color.getZ(), this.scale);
	}
	
	public Vec3d getColor() {
		return this.color;
	}
	
	public float getScale() {
		return this.scale;
	}
}
