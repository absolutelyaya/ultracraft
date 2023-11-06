package absolutelyaya.ultracraft.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import org.jetbrains.annotations.Nullable;

public class ShockParticle extends SpriteBillboardParticle
{
	protected ShockParticle(ClientWorld world, double x, double y, double z)
	{
		super(world, x, y, z);
		maxAge = 20;
		gravityStrength = 1f;
		scale = 0.3f;
	}
	
	@Override
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	@Override
	protected int getBrightness(float tint)
	{
		return 15728880;
	}
	
	public record Factory(SpriteProvider sprite) implements ParticleFactory<DefaultParticleType>
	{
		@Nullable
		@Override
		public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			ShockParticle p = new ShockParticle(world, x, y, z);
			p.setVelocity(velocityX, velocityY, velocityZ);
			p.setSprite(sprite);
			return p;
		}
	}
}
