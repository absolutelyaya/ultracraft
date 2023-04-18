package absolutelyaya.ultracraft.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import org.jetbrains.annotations.Nullable;

public class EjectedCoreFlashParticle extends SpriteBillboardParticle
{
	float rotSpeed;
	
	protected EjectedCoreFlashParticle(ClientWorld world, double x, double y, double z)
	{
		super(world, x, y, z);
		rotSpeed = (random.nextFloat() - 0.5f) * 1.25f;
		angle = random.nextFloat() * 90f;
		prevAngle = angle;
		maxAge = 3;
		scale = random.nextFloat() * 0.2f + 0.2f;
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
		angle += rotSpeed;
	}
	
	@Override
	protected int getBrightness(float tint)
	{
		return 15728880;
	}
	
	public record EjectedCoreFlashParticleFactory(SpriteProvider sprite) implements ParticleFactory<DefaultParticleType>
	{
		@Nullable
		@Override
		public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			EjectedCoreFlashParticle p = new EjectedCoreFlashParticle(world, x, y, z);
			p.setSprite(sprite);
			p.setVelocity(velocityX, velocityY, velocityZ);
			p.setAlpha(0.75f);
			return p;
		}
	}
}
