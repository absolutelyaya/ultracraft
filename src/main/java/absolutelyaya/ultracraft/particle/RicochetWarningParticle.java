package absolutelyaya.ultracraft.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RicochetWarningParticle extends SpriteBillboardParticle
{
	static Random random = new Random();
	float rotSpeed;
	
	protected RicochetWarningParticle(ClientWorld world, double x, double y, double z)
	{
		super(world, x, y, z);
		angle = random.nextFloat() * 3;
		rotSpeed = (random.nextFloat() - 0.5f) * 0.5f;
		maxAge = 13;
		scale = 0f;
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
		if(age < 4)
			scale += 0.25;
		else if(age > 10)
			scale -= 0.25;
		rotSpeed = MathHelper.lerp(0.05f, rotSpeed, 0f);
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
			RicochetWarningParticle p = new RicochetWarningParticle(world, x, y, z);
			p.setSprite(sprite);
			p.setVelocity(velocityX, velocityY, velocityZ);
			return p;
		}
	}
}
