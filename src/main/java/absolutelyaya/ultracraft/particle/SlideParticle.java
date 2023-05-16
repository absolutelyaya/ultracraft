package absolutelyaya.ultracraft.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class SlideParticle extends BillboardStripeParticle
{
	protected SlideParticle(ClientWorld world, double x, double y, double z)
	{
		super(world, x, y, z);
	}
	
	@Override
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public void setSprite(SpriteProvider spriteProvider) {
		this.setSprite(spriteProvider.getSprite(this.random));
	}
	
	public record SlideParticleFactory(SpriteProvider spriteProvider) implements ParticleFactory<DefaultParticleType>
	{
		@Override
		public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			SlideParticle particle = new SlideParticle(world, x, y, z);
			Random rand = new Random();
			particle.setSprite(spriteProvider);
			particle.width = 0.20f;
			particle.maxLength = 0.8f + rand.nextFloat() * 0.5f;
			particle.maxAge = 6 + rand.nextInt(6);
			particle.setColor(0.8f, 0.75f, 0.3f);
			particle.setVelocity(rand.nextDouble() - 0.5, rand.nextDouble() * 0.3, rand.nextDouble() - 0.5);
			particle.setAcceleration(new Vec3d(velocityX, velocityY, velocityZ));
			return particle;
		}
	}
}
