package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.client.rendering.HitscanRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ClientHitscanHandler
{
	final HitscanRenderer renderer = new HitscanRenderer();
	final Set<Hitscan> hitscans = new HashSet<>();
	
	public void addEntry(Vec3d from, Vec3d to, byte type)
	{
		hitscans.add(new Hitscan(from, to, type));
	}
	
	public void tick()
	{
		if(hitscans.size() == 0)
			return;
		for (Object o : hitscans.toArray())
		{
			Hitscan hitscan = (Hitscan)o;
			if(!hitscan.tick())
				hitscans.remove(hitscan);
		}
	}
	
	public void render(MatrixStack matrices, Camera camera)
	{
		new ArrayList<>(hitscans).iterator().forEachRemaining((shot) -> renderer.render(shot, matrices, camera));
	}
	
	public static class Hitscan
	{
		public final Vec3d from, to;
		public final HitscanType type;
		int age;
		
		public Hitscan(Vec3d from, Vec3d to, byte type)
		{
			this.from = from;
			this.to = to;
			this.type = HitscanType.values()[type];
		}
		
		public boolean tick()
		{
			return age++ <= type.maxAge;
		}
		
		public float getGirth()
		{
			return MathHelper.lerp((float)age / (float)type.maxAge, type.startGirth, 0f);
		}
		
		public enum HitscanType
		{
			REVOLVER_SHOT(0xdff6f5, 5, 0.1f),
			REVOLVER_PIERCE(0x8aebf1, 20, 0.2f),
			RAILGUN_ELEC(0x2ee9ff, 60, 0.3f),
			RAILGUN_DRILL(0x30ff72, 60, 0.3f),
			RAILGUN_MALICIOUS(0xff4530, 60, 0.3f),
			MALICIOUS(0xf4d81b, 60, 0.3f),
			RICOCHET(0xf4d81b, 5, 0.1f);
			
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
}
