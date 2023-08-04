package absolutelyaya.ultracraft.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class DroneChargeParticle extends BillboardStripeParticle
{
	protected DroneChargeParticle(ClientWorld clientWorld, double x, double y, double z)
	{
		super(clientWorld, x, y, z);
	}
	
	@Override
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public void setSprite(SpriteProvider spriteProvider) {
		this.setSprite(spriteProvider.getSprite(this.random));
	}
	
	public record Factory(SpriteProvider spriteProvider) implements ParticleFactory<DefaultParticleType>
	{
		@Override
		public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			DroneChargeParticle particle = new DroneChargeParticle(world, x, y, z);
			particle.setSprite(spriteProvider);
			particle.width = 0.1f;
			particle.maxLength = 0.2f;
			particle.maxAge = 20;
			particle.setColor(1f, 1f, 1f);
			particle.setVelocity(velocityX, velocityY, velocityZ);
			return particle;
		}
	}
}
