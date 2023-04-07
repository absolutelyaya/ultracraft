package absolutelyaya.ultracraft.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;

public class ParryIndicatorParticle extends SpriteBillboardParticle
{
	float rotSpeed;
	
	protected ParryIndicatorParticle(ClientWorld world, double x, double y, double z)
	{
		super(world, x, y, z);
		scale = 0f;
		rotSpeed = (random.nextFloat() - 0.5f) * 0.4f;
		setVelocity((random.nextFloat() - 0.5f) * 0.05f, (random.nextFloat() - 0.5f) * 0.05f, (random.nextFloat() - 0.5f) * 0.05f);
		angle = random.nextFloat();
		maxAge = 10;
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
		if (age < 5)
			scale += 0.1;
		if (age > 5 && age < 10)
			scale -= 0.1;
		prevAngle = angle;
		angle += rotSpeed;
	}
	
	@Override
	protected int getBrightness(float tint)
	{
		return 15728880;
	}
	
	public record ParryIndicatorParticleFactory(SpriteProvider sprite) implements ParticleFactory<ParryIndicatorParticleEffect>
	{
		@Nullable
		@Override
		public Particle createParticle(ParryIndicatorParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			ParryIndicatorParticle p = new ParryIndicatorParticle(world, x, y, z);
			p.setSprite(sprite);
			if(parameters.unparriable)
				p.setColor(0.4f, 0.7f, 1f);
			else
				p.setColor(1f, 1f, 0f);
			return p;
		}
	}
}
