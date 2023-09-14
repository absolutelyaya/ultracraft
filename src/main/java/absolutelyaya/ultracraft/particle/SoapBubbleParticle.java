package absolutelyaya.ultracraft.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import org.jetbrains.annotations.Nullable;

public class SoapBubbleParticle extends SpriteBillboardParticle
{
	protected SoapBubbleParticle(ClientWorld clientWorld, double d, double e, double f)
	{
		super(clientWorld, d, e, f);
		maxAge = 24 + (int)(Math.random() * 16);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		velocityY += 0.0025f;
	}
	
	@Override
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public record Factory(SpriteProvider sprite) implements ParticleFactory<DefaultParticleType>
	{
		@Nullable
		@Override
		public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			SoapBubbleParticle p = new SoapBubbleParticle(world, x, y, z);
			p.setVelocity(velocityX, velocityY, velocityZ);
			p.setSprite(sprite);
			return p;
		}
	}
}
