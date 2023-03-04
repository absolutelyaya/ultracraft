package absolutelyaya.ultracraft.particle.goop;

import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class SpriteAAParticle extends SpriteBillboardParticle
{
	protected final SpriteProvider spriteProvider;
	
	protected Vec3d scale;
	
	protected SpriteAAParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider)
	{
		super(world, x, y, z);
		float s = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
		this.scale = new Vec3d(s, s, s);
		this.spriteProvider = spriteProvider;
		sprite = spriteProvider.getSprite(random);
	}
	
	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
	{
		Vec3d camPos = camera.getPos();
		Vec3d dir = new Vec3d(x, y, z).subtract(camPos).normalize();
		float f = (float)(MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camPos.getX());
		float g = (float)(MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camPos.getY());
		float h = (float)(MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camPos.getZ());
		
		Vec3d[] Vec3ds = new Vec3d[]{
				new Vec3d(-1.0F, -1.0F, 0.0F),
				new Vec3d(-1.0F, 1.0F, 0.0F),
				new Vec3d(1.0F, 1.0F, 0.0F),
				new Vec3d(1.0F, -1.0F, 0.0F)};
		
		for(int k = 0; k < 4; ++k)
		{
			Vec3d Vec3d = Vec3ds[k];
			Vec3d = Vec3d.rotateY((float)Math.atan2(dir.x, dir.z));
			Vec3d = Vec3d.multiply(scale.getX(), scale.getY(), scale.getZ());
			Vec3ds[k] = Vec3d.add(f, g, h);
		}
		
		int n = this.getBrightness(tickDelta);
		vertexConsumer.vertex(Vec3ds[0].getX(), Vec3ds[0].getY(), Vec3ds[0].getZ()).texture(getMaxU(), getMaxV()).color(this.red, this.green, this.blue, this.alpha).light(n).next();
		vertexConsumer.vertex(Vec3ds[1].getX(), Vec3ds[1].getY(), Vec3ds[1].getZ()).texture(getMaxU(), getMinV()).color(this.red, this.green, this.blue, this.alpha).light(n).next();
		vertexConsumer.vertex(Vec3ds[2].getX(), Vec3ds[2].getY(), Vec3ds[2].getZ()).texture(getMinU(), getMinV()).color(this.red, this.green, this.blue, this.alpha).light(n).next();
		vertexConsumer.vertex(Vec3ds[3].getX(), Vec3ds[3].getY(), Vec3ds[3].getZ()).texture(getMinU(), getMaxV()).color(this.red, this.green, this.blue, this.alpha).light(n).next();
	}
}
