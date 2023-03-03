package absolutelyaya.ultracraft.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public abstract class BillboardStripeParticle extends Particle
{
	float length, width, maxLength = 2f;
	Sprite sprite;
	Vec3d acceleration = Vec3d.ZERO;
	boolean debugging = false;
	
	protected BillboardStripeParticle(ClientWorld world, double x, double y, double z)
	{
		super(world, x, y, z);
		alpha = 0f;
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
	
	public void setAcceleration(Vec3d acceleration)
	{
		this.acceleration = acceleration;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(age <= 3)
			alpha = age / 3f;
		if(age - maxAge + 20 > 0)
			alpha = (maxAge - age) / 20f;
		if(acceleration.lengthSquared() > 0)
			setVelocity(velocityX + acceleration.x, velocityY + acceleration.y, velocityZ + acceleration.z);
	}
	
	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
	{
		RenderSystem.enableBlend();
		Vec3d vec3d = camera.getPos();
		float x = (float)(MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
		float y = (float)(MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
		float z = (float)(MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());
		if(acceleration.lengthSquared() > 0)
			length = maxLength * (float)getVelocity().length();
		else
			length = Math.min(maxLength, (float)MathHelper.lerp(tickDelta, length , length + getVelocity().length()));
		Vec3d dir = getVelocity().normalize().multiply(-1f);
		
		Vec3d camForward = new Vec3d(camera.getHorizontalPlane());
		Vec3d right = dir.crossProduct(camForward).normalize();
		
		Vec3d[] vec3ds = new Vec3d[]{right.multiply(-width / 2),
				right.multiply(width / 2),
				right.multiply(width / 2).subtract(dir.multiply(length)),
				right.multiply(-width / 2).subtract(dir.multiply(length))};
		
		for(int k = 0; k < 4; ++k) {
			Vec3d vec3d2 = vec3ds[k];
			if(debugging)
				world.addParticle(ParticleTypes.FLAME, vec3d2.x + this.x, vec3d2.y + this.y, vec3d2.z + this.z, 0f, 0f, 0f);
			vec3ds[k] = vec3d2.add(x, y, z);
		}
		
		vertexConsumer.vertex(vec3ds[0].x, vec3ds[0].y, vec3ds[0].z).texture(sprite.getMinU(), sprite.getMinV()).color(red, green, blue, alpha).light(15728880).next();
		vertexConsumer.vertex(vec3ds[1].x, vec3ds[1].y, vec3ds[1].z).texture(sprite.getMaxU(), sprite.getMinV()).color(red, green, blue, alpha).light(15728880).next();
		vertexConsumer.vertex(vec3ds[2].x, vec3ds[2].y, vec3ds[2].z).texture(sprite.getMaxU(), sprite.getMaxV()).color(red, green, blue, alpha).light(15728880).next();
		vertexConsumer.vertex(vec3ds[3].x, vec3ds[3].y, vec3ds[3].z).texture(sprite.getMinU(), sprite.getMaxV()).color(red, green, blue, alpha).light(15728880).next();
		
		if(debugging)
		{
			for (int i = 0; i < 5; i++)
			{
				float f = i / 5f;
				world.addParticle(new DustParticleEffect(new Vector3f(1f, 0f, 0f), 1f), this.x + dir.x * f, this.y + dir.y * f, this.z + dir.z * f, 0f, 0f, 0f);
				world.addParticle(new DustParticleEffect(new Vector3f(0f, 1f, 0f), 1f), this.x + right.x * f, this.y + right.y * f, this.z + right.z * f, 0f, 0f, 0f);
			}
		}
	}
}
