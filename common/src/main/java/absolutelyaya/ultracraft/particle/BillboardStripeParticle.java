package absolutelyaya.ultracraft.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public abstract class BillboardStripeParticle extends Particle
{
	float length, width, maxLength = 2f;
	Sprite sprite;
	boolean debugging = false;
	
	protected BillboardStripeParticle(ClientWorld world, double x, double y, double z)
	{
		super(world, x, y, z);
		alpha = 1f;
	}
	
	Vec3d getVelocity()
	{
		return new Vec3d(velocityX, velocityY, velocityZ);
	}
	
	public void setSprite(Sprite sprite)
	{
		this.sprite = sprite;
	}
	
	public void setWidth(float width)
	{
		this.width = width;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(age - maxAge + 20 > 0)
		{
			alpha = (maxAge - age) / 20f;
		}
	}
	
	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
	{
		RenderSystem.enableBlend();
		Vec3d vec3d = camera.getPos();
		float x = (float)(MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
		float y = (float)(MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
		float z = (float)(MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());
		length = Math.min(maxLength, (float)MathHelper.lerp(tickDelta, length , length + getVelocity().length()));
		Vec3d dir = getVelocity().normalize().multiply(-1f);
		
		Vec3d camForward = new Vec3d(camera.getHorizontalPlane());
		Vec3d right = dir.crossProduct(camForward).normalize();
		
		Vec3f[] vec3fs = new Vec3f[]{new Vec3f(right.multiply(-width / 2)),
				new Vec3f(right.multiply(width / 2)),
				new Vec3f(right.multiply(width / 2).subtract(dir.multiply(length))),
				new Vec3f(right.multiply(-width / 2).subtract(dir.multiply(length)))};
		
		for(int k = 0; k < 4; ++k) {
			Vec3f vec3f2 = vec3fs[k];
			if(debugging)
				world.addParticle(ParticleTypes.FLAME, vec3f2.getX() + this.x, vec3f2.getY() + this.y, vec3f2.getZ() + this.z, 0f, 0f, 0f);
			vec3f2.add(x, y, z);
		}
		
		vertexConsumer.vertex(vec3fs[0].getX(), vec3fs[0].getY(), vec3fs[0].getZ()).texture(sprite.getMinU(), sprite.getMinV()).color(1f, 1f, 1f, alpha).light(15728880).next();
		vertexConsumer.vertex(vec3fs[1].getX(), vec3fs[1].getY(), vec3fs[1].getZ()).texture(sprite.getMaxU(), sprite.getMinV()).color(1f, 1f, 1f, alpha).light(15728880).next();
		vertexConsumer.vertex(vec3fs[2].getX(), vec3fs[2].getY(), vec3fs[2].getZ()).texture(sprite.getMaxU(), sprite.getMaxV()).color(1f, 1f, 1f, alpha).light(15728880).next();
		vertexConsumer.vertex(vec3fs[3].getX(), vec3fs[3].getY(), vec3fs[3].getZ()).texture(sprite.getMinU(), sprite.getMaxV()).color(1f, 1f, 1f, alpha).light(15728880).next();
		
		if(debugging)
		{
			for (int i = 0; i < 5; i++)
			{
				float f = i / 5f;
				world.addParticle(new DustParticleEffect(new Vec3f(1f, 0f, 0f), 1f), this.x + dir.x * f, this.y + dir.y * f, this.z + dir.z * f, 0f, 0f, 0f);
				world.addParticle(new DustParticleEffect(new Vec3f(0f, 1f, 0f), 1f), this.x + right.x * f, this.y + right.y * f, this.z + right.z * f, 0f, 0f, 0f);
			}
		}
	}
}
