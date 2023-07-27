package absolutelyaya.ultracraft.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import org.jetbrains.annotations.Nullable;

public class BigCircleParticle extends SpriteBillboardParticle
{
	protected BigCircleParticle(ClientWorld world, double x, double y, double z)
	{
		super(world, x, y, z);
		maxAge = 50;
		scale = 0;
	}
	
	@Override
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		prevAngle = angle;
		scale += 1.75f / maxAge;
		alpha = Math.max(1f - Math.abs(30f - age) / 15f, 0f);
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
			BigCircleParticle p = new BigCircleParticle(world, x, y, z);
			p.setSprite(sprite);
			return p;
		}
	}
}
