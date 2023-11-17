package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.client.rendering.HitscanRenderer;
import com.google.common.collect.Queues;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Queue;
import java.util.*;
import java.util.function.Function;

public class ClientHitscanHandler
{
	final HitscanRenderer renderer = new HitscanRenderer();
	final Set<Hitscan> hitscans = new HashSet<>();
	final Map<UUID, MovingHitscan> movingHitscans = new HashMap<>();
	final Queue<Hitscan> added = Queues.newArrayDeque();
	final Queue<UUID> removed = Queues.newArrayDeque();
	
	public void addEntry(Vec3d from, Vec3d to, byte type)
	{
		added.add(new Hitscan(from, to, type));
	}
	
	public void addEntry(Function<Float, Vec3d> from, Function<Float, Vec3d> to, UUID id, byte type)
	{
		added.add(new MovingHitscan(from, to, id, type));
	}
	
	public void addConnector(Function<Float, Vec3d> from, Function<Float, Vec3d> to, UUID id, Vec2f girth, float girthOverDistance, int color, int layers)
	{
		added.add(new Connector(from, to, id, girth, girthOverDistance, color, layers));
	}
	
	public void removeMoving(UUID id)
	{
		removed.add(id);
	}
	
	public void tick()
	{
		if(hitscans.size() == 0)
			if(hitscans.size() == 0 && added.size() == 0 && removed.size() == 0)
				return;
		for (Object o : hitscans.toArray())
		{
			Hitscan hitscan = (Hitscan)o;
			if(!hitscan.tick())
				hitscans.remove(hitscan);
		}
		for (MovingHitscan moving : movingHitscans.values())
			moving.tick();
		hitscans.removeIf(hitscan -> !hitscan.tick());
		while(added.size() > 0)
		{
			Hitscan scan = added.remove();
			if(scan instanceof MovingHitscan moving)
				movingHitscans.put(moving.id, moving);
			else
				hitscans.add(scan);
		}
		while(removed.size() > 0)
		{
			UUID id = removed.remove();
			movingHitscans.remove(id);
		}
	}
	
	public void render(MatrixStack matrices, Camera camera, float delta)
	{
		hitscans.iterator().forEachRemaining((shot) -> HitscanRenderer.render(shot, matrices, camera, delta));
		for (MovingHitscan moving : movingHitscans.values())
			HitscanRenderer.render(moving, matrices, camera, delta);
	}
	
	public static class Hitscan
	{
		final Vec3d from, to;
		final HitscanType type;
		int age;
		
		public Hitscan(Vec3d from, Vec3d to, byte type)
		{
			this.from = from;
			this.to = to;
			this.type = HitscanType.values()[type];
		}
		
		public boolean tick()
		{
			return age++ <= getMaxAge();
		}
		
		public int getMaxAge()
		{
			return type.maxAge;
		}
		
		public Vec3d getFrom(float delta)
		{
			return from;
		}
		
		public Vec3d getTo(float delta)
		{
			return to;
		}
		
		public Color getColor()
		{
			return new Color(type.color);
		}
		
		public float getGirth()
		{
			return MathHelper.lerp((float)age / (float)type.maxAge, type.startGirth, 0f);
		}
		
		public int getLayers()
		{
			return 3;
		}
		
		public enum HitscanType
		{
			REVOLVER_SHOT(0xdff6f5, 3, 0.1f),
			REVOLVER_PIERCE(0x8aebf1, 20, 0.2f),
			RAILGUN_ELEC(0x2ee9ff, 60, 0.3f),
			RAILGUN_DRILL(0x30ff72, 60, 0.3f),
			RAILGUN_MALICIOUS(0xff4530, 60, 0.3f),
			MALICIOUS(0xf4d81b, 60, 0.3f),
			RICOCHET(0xf4d81b, 5, 0.1f),
			SHARPSHOOTER(0xdf2828, 60, 0.1f);
			
			public final int color, maxAge;
			public final float startGirth;
			
			HitscanType(int color, int maxAge, float startGirth)
			{
				this.color = color;
				this.maxAge = maxAge;
				this.startGirth = startGirth;
			}
		}
	}
	
	public static class MovingHitscan extends Hitscan
	{
		final Function<Float, Vec3d> from, to;
		public final UUID id;
		
		public MovingHitscan(Function<Float, Vec3d> from, Function<Float, Vec3d> to, UUID id, byte type)
		{
			super(null, null, type);
			this.from = from;
			this.to = to;
			this.id = id;
		}
		
		@Override
		public boolean tick()
		{
			super.tick();
			return true;
		}
		
		@Override
		public Vec3d getFrom(float delta)
		{
			return from.apply(delta);
		}
		
		@Override
		public Vec3d getTo(float delta)
		{
			return to.apply(delta);
		}
	}
	
	public static class Connector extends MovingHitscan
	{
		final int layers;
		final Vec2f girth;
		final float girthOverDistance;
		final Color color;
		
		public Connector(Function<Float, Vec3d> from, Function<Float, Vec3d> to, UUID id, Vec2f girth, float girthOverDistance, int color, int layers)
		{
			super(from, to, id, (byte)0);
			this.girth = girth;
			this.girthOverDistance = girthOverDistance;
			this.color = new Color(color);
			this.layers = layers;
		}
		
		@Override
		public float getGirth()
		{
			return MathHelper.clampedLerp(girth.x, girth.y, (float)from.apply(0f).distanceTo(to.apply(0f)) * girthOverDistance);
		}
		
		@Override
		public Color getColor()
		{
			return color;
		}
		
		@Override
		public int getLayers()
		{
			return layers;
		}
	}
}
