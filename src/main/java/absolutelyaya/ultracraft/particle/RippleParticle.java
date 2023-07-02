package absolutelyaya.ultracraft.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.DefaultParticleType;
import org.jetbrains.annotations.Nullable;

public class RippleParticle extends AnimatedFloorEffectParticle
{
	protected RippleParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider)
	{
		super(world, x, y, z, spriteProvider);
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
		Entity camera = MinecraftClient.getInstance().getCameraEntity();
		if(camera != null)
		{
			setSpriteForAge(spriteProvider);
			if (age > maxAge / 4 * 3)
				setAlpha(Math.max(alpha - ((float)age - (float)(maxAge / 4)) / (float)maxAge, 0f));
		}
	}
	
	public static class Factory implements ParticleFactory<DefaultParticleType>
	{
		protected final SpriteProvider spriteProvider;
		
		public Factory(SpriteProvider spriteProvider)
		{
			this.spriteProvider = spriteProvider;
		}
		
		@Nullable
		@Override
		public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			RippleParticle ripple = new RippleParticle(world, x, y, z, spriteProvider);
			ripple.setSpriteForAge(spriteProvider);
			ripple.setMaxAge((int)((10f)));
			ripple.setAlpha(1);
			return ripple;
		}
	}
}
