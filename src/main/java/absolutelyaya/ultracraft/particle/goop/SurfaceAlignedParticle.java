package absolutelyaya.ultracraft.particle.goop;

import absolutelyaya.ultracraft.client.Ultraconfig;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SurfaceAlignedParticle extends SpriteBillboardParticle
{
	static final Ultraconfig config;
	
	private final List<Boolean> faceShouldRender = new ArrayList<>();
	private final List<Vec3d> verts = new ArrayList<>();
	private final List<Vec2f> uvs = new ArrayList<>();
	private final List<Float> maxDeform = new ArrayList<>();
	protected final SpriteProvider spriteProvider;
	protected final Vec3d dir;
	
	protected float deformation;
	float targetSize;
	boolean isFancy;
	
	protected SurfaceAlignedParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider,
									 Vec3d color, float scale, Vec3d dir)
	{
		super(world, x, y, z);
		this.targetSize = scale;
		this.scale = this.random.nextFloat() * scale * 0.5f + scale * 0.25f;
		this.spriteProvider = spriteProvider;
		sprite = spriteProvider.getSprite(random);
		gravityStrength = 0;
		angle = random.nextFloat() * 360;
		setColor((float)color.getX(), (float)color.getY(), (float)color.getZ());
		
		isFancy = config.fancyGoop;
		
		//System.out.println(dir);
		
		this.dir = new Vec3d((float)Math.round(dir.x), (float)Math.round(dir.y), (float)Math.round(dir.z));
		boolean b = dir.x != 0;
		if(dir.y != 0)
		{
			if(b)
				markDead();
			b = true;
		}
		if(dir.z != 0 && b)
			markDead();
		
		if(dead)
		{
			this.scale = 0;
			return;
		}
		
		Vec3d modDir = new Vec3d(dir.getX() == 0 ? 1 : 0, dir.getY() == 0 ? 1 : 0, dir.getZ() == 0 ? 1 : 0);
		float s = isFancy ? Math.max(targetSize, 1) : 1;
		for(int vy = 0; vy <= s; vy++)
			for (int vx = 0; vx <= s; vx++)
			{
				Vec3d vert;
				if(dir.y != 0)
					vert = new Vec3d(modDir.getX() * vx / s, modDir.getY(), modDir.getZ() * vy / s);
				else
					vert = new Vec3d(modDir.getX() * vx / s, modDir.getY() * vy / s, modDir.getZ() * vx / s);
				vert.add(dir.multiply(random.nextFloat() / 100)); //fight Z-Fighting
				verts.add(vert);
				faceShouldRender.add(true);
				uvs.add(new Vec2f(MathHelper.lerp(vx / s, getMinU(), getMaxU()), MathHelper.lerp(vy / s, getMinV(), getMaxV())));
				if(dir.y == 0)
					maxDeform.add(random.nextFloat());
				else
					maxDeform.add(random.nextBoolean() ? random.nextFloat() * 0.25f * targetSize : 0);
			}
	}
	
	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
	{
		boolean debug = false;
		if(verts.size() == 0)
			return;
		
		Vec3d camPos = camera.getPos();
		float f = (float)(MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camPos.getX());
		float g = (float)(MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camPos.getY());
		float h = (float)(MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camPos.getZ());
		
		Vec3d dir = this.dir;
		
		List<Vec3d> verts = new ArrayList<>();
		Vec3d modDir = new Vec3d(dir.getX() == 0 ? 1 : 0, dir.getY() == 0 ? 1 : 0, dir.getZ() == 0 ? 1 : 0);
		this.verts.forEach(i ->
		{
			i = i.subtract(new Vec3d((float)(modDir.getX() * 0.5), (float)(modDir.getY() * 0.5), (float)(modDir.getZ() * 0.5)));
			verts.add(i);
		});
		
		AtomicInteger atomicInt = new AtomicInteger();
		for (int i = 0; i < verts.size(); i++)
		{
			Vec3d v = verts.get(i);
			//random rotation
			v = v.rotateX((float)(dir.getX() * angle));
			v = v.rotateY((float)(dir.getY() * angle));
			v = v.rotateZ((float)(dir.getZ() * angle));
			//deformation
			if(!(this.dir.getY() > 0) && isFancy)
				v = v.subtract(new Vec3d(0, deformation * maxDeform.get(atomicInt.get()), 0));
			v = v.multiply(scale);
			verts.set(i, v.add(f, g, h));
			atomicInt.getAndIncrement();
		}
		
		int n = this.getBrightness(tickDelta);
		float ts = isFancy ? Math.max(targetSize, 1) : 1;
		
		for (int y = 1, vi = 0; y < (int)ts + 1; y++, vi++)
		{
			for (int x = 1; x < (int)ts + 1; x++, vi++)
			{
				Vec3d[] modVerts = new Vec3d[] {verts.get(vi), verts.get((int)(vi + ts + 1)),
						verts.get((int)(vi + ts + 2)), verts.get(vi + 1)};
				
				boolean render = !isFancy || faceShouldRender.get(vi);
				
				if(isFancy && dir.getY() > 0 && faceShouldRender.get(vi))
				{
					Vec3d faceCenter = modVerts[0];
					faceCenter.add(modVerts[1]);
					faceCenter.add(modVerts[2]);
					faceCenter.add(modVerts[3]);
					faceCenter.multiply(0.25f);
					
					Vec3d v = camPos.add(faceCenter);
					BlockPos pos = new BlockPos((int)v.x, (int)v.y, (int)v.z);
					render = !world.isAir(pos.down()) && !world.getBlockState(pos).isSolidBlock(world, pos);
					if(!render)
						faceShouldRender.set(vi, false); //so faces don't reappear after being removed
					
					if(debug)
					{
						world.addParticle(ParticleTypes.FLAME,
								camPos.x + faceCenter.getX(), camPos.y + faceCenter.getY() + 0.1, camPos.z + faceCenter.getZ(),
								0, 0.05, 0);
					}
					
					for (int i = 0; i < modVerts.length; i++)
					{
						Vec3d mv = modVerts[i];
						mv = mv.add(camPos);
						BlockPos vpos = new BlockPos((int)mv.x, (int)mv.y, (int)mv.z);
						if((world.isAir(vpos.down()) || world.getBlockState(pos).isSolidBlock(world, pos)) && targetSize >= 2)
							mv = moveToBlockEdge(mv);
						modVerts[i] = mv.subtract(camPos);
					}
				}
				
				if(render)
				{
					boolean up = this.y < 0;
					//top
					vertexConsumer.vertex(modVerts[0].getX(), modVerts[0].getY() + (up ? 1.01 : 0), modVerts[0].getZ()).texture(uvs.get(vi).x, uvs.get(vi).y).color(this.red, this.green, this.blue, this.alpha).light(n).next();
					vertexConsumer.vertex(modVerts[1].getX(), modVerts[1].getY() + (up ? 1.01 : 0), modVerts[1].getZ()).texture(uvs.get((int)(vi + ts + 1)).x, uvs.get((int)(vi + ts + 1)).y).color(this.red, this.green, this.blue, this.alpha).light(n).next();
					vertexConsumer.vertex(modVerts[2].getX(), modVerts[2].getY() + (up ? 1.01 : 0), modVerts[2].getZ()).texture(uvs.get((int)(vi + ts + 2)).x, uvs.get((int)(vi + ts + 2)).y).color(this.red, this.green, this.blue, this.alpha).light(n).next();
					vertexConsumer.vertex(modVerts[3].getX(), modVerts[3].getY() + (up ? 1.01 : 0), modVerts[3].getZ()).texture(uvs.get(vi + 1).x, uvs.get(vi + 1).y).color(this.red, this.green, this.blue, this.alpha).light(n).next();
					//bottom
					vertexConsumer.vertex(modVerts[3].getX(), modVerts[3].getY() + (up ? 1.01 : 0), modVerts[3].getZ()).texture(uvs.get(vi + 1).x, uvs.get(vi + 1).y).color(this.red, this.green, this.blue, this.alpha).light(n).next();
					vertexConsumer.vertex(modVerts[2].getX(), modVerts[2].getY() + (up ? 1.01 : 0), modVerts[2].getZ()).texture(uvs.get((int)(vi + ts + 2)).x, uvs.get((int)(vi + ts + 2)).y).color(this.red, this.green, this.blue, this.alpha).light(n).next();
					vertexConsumer.vertex(modVerts[1].getX(), modVerts[1].getY() + (up ? 1.01 : 0), modVerts[1].getZ()).texture(uvs.get((int)(vi + ts + 1)).x, uvs.get((int)(vi + ts + 1)).y).color(this.red, this.green, this.blue, this.alpha).light(n).next();
					vertexConsumer.vertex(modVerts[0].getX(), modVerts[0].getY() + (up ? 1.01 : 0), modVerts[0].getZ()).texture(uvs.get(vi).x, uvs.get(vi).y).color(this.red, this.green, this.blue, this.alpha).light(n).next();
				}
				
				verts.set(vi, modVerts[0]);
				verts.set((int)(vi + ts + 1), modVerts[1]);
				verts.set((int)(vi + ts + 2), modVerts[2]);
				verts.set(vi + 1, modVerts[3]);
			}
		}
		
		if(debug)
		{
			int x = 0, y = 0;
			for (Vec3d v : verts)
			{
				world.addParticle(new DustParticleEffect(new Vector3f(x / ts, y / ts, 0f), 0.5f),
						camPos.x + v.getX(), camPos.y + v.getY() + 0.1, camPos.z + v.getZ(),
						0, 0.05, 0);
				x++;
				if(x > (int)ts)
				{
					x = 0;
					y++;
				}
			}
			
			world.addParticle(new DustParticleEffect(new Vector3f(1f, 1f, 1f), 1f), this.x, this.y, this.z,
					0, 0.25, 0);
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(world.getBlockState(new BlockPos((int)(x - dir.getX() + (x < 0 ? -1 : 0)), (int)(y - dir.getY()), (int)(z - dir.getZ() + (z < 0 ? -1 : 0)))).isAir() ||
				   !world.getBlockState(new BlockPos((int)x + (x < 0 ? -1 : 0), (int)y, (int)z + (z < 0 ? -1 : 0))).isAir())
			markDead();
		deformation = (float)age / maxAge;
	}
	
	private Vec3d moveToBlockEdge(Vec3d vert)
	{
		if(!config.wrapToEdges)
			return vert;
		Vec3d dir = vert.subtract(x, y, z).normalize().multiply(0.33);
		return new Vec3d(Math.round(vert.getX() - dir.x), vert.getY(), Math.round(vert.getZ() - dir.z));
	}
	
	@Override
	protected int getBrightness(float tint)
	{
		BlockPos blockPos = BlockPos.ofFloored(this.x, this.y, this.z);
		return WorldRenderer.getLightmapCoordinates(this.world, blockPos.add(0, (this.y < 0 && dir.y > 0) ? 1 : 0, 0));
	}
	
	static {
		config = UltracraftClient.getConfigHolder().getConfig();
	}
}
