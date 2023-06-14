package absolutelyaya.ultracraft.particle;

import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public abstract class AnimatedFloorEffectParticle extends SpriteBillboardParticle
{
	protected final SpriteProvider spriteProvider;
	
	protected AnimatedFloorEffectParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider)
	{
		super(world, x, y, z);
		this.scale = 0.5f * (random.nextFloat() * 0.5F + 0.5F) * 2.0F;
		this.spriteProvider = spriteProvider;
	}
	
	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
	{
		Vec3d camPos = camera.getPos();
		float f = (float)(MathHelper.lerp(tickDelta, prevPosX, x) - camPos.getX());
		float g = (float)(MathHelper.lerp(tickDelta, prevPosY, y) - camPos.getY());
		float h = (float)(MathHelper.lerp(tickDelta, prevPosZ, z) - camPos.getZ());
		
		Vector3f[] Vector3fs = new Vector3f[]{
				new Vector3f(-1.0F, 0.0F, -1.0F),
				new Vector3f(-1.0F, 0.0F, 1.0F),
				new Vector3f(1.0F, 0.0F, 1.0F),
				new Vector3f(1.0F, 0.0F, -1.0F)};
		
		for(int k = 0; k < 4; ++k)
		{
			Vector3f Vector3f = Vector3fs[k];
			Vector3f.mul(scale);
			Vector3f.add(f, g, h);
		}
		
		int n = getBrightness(tickDelta);
		vertexConsumer.vertex(Vector3fs[0].x(), Vector3fs[0].y(), Vector3fs[0].z()).texture(getMaxU(), getMaxV()).color(red, green, blue, alpha).light(n).next();
		vertexConsumer.vertex(Vector3fs[1].x(), Vector3fs[1].y(), Vector3fs[1].z()).texture(getMaxU(), getMinV()).color(red, green, blue, alpha).light(n).next();
		vertexConsumer.vertex(Vector3fs[2].x(), Vector3fs[2].y(), Vector3fs[2].z()).texture(getMinU(), getMinV()).color(red, green, blue, alpha).light(n).next();
		vertexConsumer.vertex(Vector3fs[3].x(), Vector3fs[3].y(), Vector3fs[3].z()).texture(getMinU(), getMaxV()).color(red, green, blue, alpha).light(n).next();
		vertexConsumer.vertex(Vector3fs[3].x(), Vector3fs[3].y(), Vector3fs[3].z()).texture(getMinU(), getMaxV()).color(red, green, blue, alpha).light(n).next();
		vertexConsumer.vertex(Vector3fs[2].x(), Vector3fs[2].y(), Vector3fs[2].z()).texture(getMinU(), getMinV()).color(red, green, blue, alpha).light(n).next();
		vertexConsumer.vertex(Vector3fs[1].x(), Vector3fs[1].y(), Vector3fs[1].z()).texture(getMaxU(), getMinV()).color(red, green, blue, alpha).light(n).next();
		vertexConsumer.vertex(Vector3fs[0].x(), Vector3fs[0].y(), Vector3fs[0].z()).texture(getMaxU(), getMaxV()).color(red, green, blue, alpha).light(n).next();
	}
}
