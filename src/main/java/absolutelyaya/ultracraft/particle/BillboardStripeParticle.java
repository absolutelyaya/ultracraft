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
import org.joml.Vector3fc;

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
		
		Vector3f[] vec3fs = new Vector3f[]{new Vector3f((Vector3fc)right.multiply(-width / 2)),
				new Vector3f((Vector3fc)right.multiply(width / 2)),
				new Vector3f((Vector3fc)right.multiply(width / 2).subtract(dir.multiply(length))),
				new Vector3f((Vector3fc)right.multiply(-width / 2).subtract(dir.multiply(length)))};
		
		for(int k = 0; k < 4; ++k) {
			Vector3f vec3f2 = vec3fs[k];
			if(debugging)
				world.addParticle(ParticleTypes.FLAME, vec3f2.x() + this.x, vec3f2.y() + this.y, vec3f2.z() + this.z, 0f, 0f, 0f);
			vec3f2.add(x, y, z);
		}
		
		vertexConsumer.vertex(vec3fs[0].x(), vec3fs[0].y(), vec3fs[0].z()).texture(sprite.getMinU(), sprite.getMinV()).color(red, green, blue, alpha).light(15728880).next();
		vertexConsumer.vertex(vec3fs[1].x(), vec3fs[1].y(), vec3fs[1].z()).texture(sprite.getMaxU(), sprite.getMinV()).color(red, green, blue, alpha).light(15728880).next();
		vertexConsumer.vertex(vec3fs[2].x(), vec3fs[2].y(), vec3fs[2].z()).texture(sprite.getMaxU(), sprite.getMaxV()).color(red, green, blue, alpha).light(15728880).next();
		vertexConsumer.vertex(vec3fs[3].x(), vec3fs[3].y(), vec3fs[3].z()).texture(sprite.getMinU(), sprite.getMaxV()).color(red, green, blue, alpha).light(15728880).next();
		
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
