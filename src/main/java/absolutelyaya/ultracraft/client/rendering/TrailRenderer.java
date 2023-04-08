package absolutelyaya.ultracraft.client.rendering;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Supplier;

public class TrailRenderer
{
	static Map<UUID, Queue<Pair<Long, Vector3f>>> trails = new HashMap<>();
	static Map<UUID, Queue<Pair<Long, Vector3f>>> newTrails = new HashMap<>();
	static Map<UUID, Long> lastRendered = new HashMap<>();
	static Map<UUID, Supplier<Vector3f>> getNextPoint = new HashMap<>();
	static List<UUID> deletionQueue = new ArrayList<>();
	static MinecraftClient client;
	
	public void render(MatrixStack matrixStack, Camera cam)
	{
		if(client.world == null)
			return;
		long time = client.world.getTime();
		
		matrixStack.push();
		Vec3d camPos = cam.getPos();
		matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		for (UUID id : trails.keySet())
		{
			if(trails.get(id).size() == 0)
				continue;
			//still updating transformation and other preparations
			Queue<Pair<Long, Vector3f>> trail = trails.get(id);
			VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();
			VertexConsumer consumer = immediate.getBuffer(RenderLayer.LINES);
			consumer.fixedColor(255, 225, 125, 255);
			Vector3f last = null;
			Vec3d normal = client.getCameraEntity().getRotationVector().multiply(-1f);
			//actually render now
			List<Pair<Long, Vector3f>> trailCopy = trail.stream().toList();
			Vector3f pos = getNextPoint.get(id).get();
			if(deletionQueue.contains(id))
			{
				pos = trailCopy.get(trailCopy.size() - 1).getRight();
				consumer.vertex(matrix, pos.x, pos.y, pos.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
			}
			else
				consumer.vertex(matrix, pos.x, pos.y, pos.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
			for (int i = trail.size() - 1; i >= 0; i--)
			{
				Vector3f vec = trailCopy.get(i).getRight();
				if(last != null)
					consumer.vertex(matrix, last.x, last.y, last.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
				consumer.vertex(matrix, vec.x, vec.y, vec.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
				last = new Vector3f(vec);
			}
			consumer.unfixColor();
			lastRendered.put(id, time);
		}
		for (int i = 0; i < newTrails.size(); i++)
		{
			UUID id = (UUID)newTrails.keySet().toArray()[i];
			trails.put(id, newTrails.get(id));
			lastRendered.put(id, time);
		}
		newTrails.clear();
		matrixStack.pop();
	}
	
	public void createTrail(UUID id, Supplier<Vector3f> nextPointFunc)
	{
		Queue<Pair<Long, Vector3f>> trail = new ArrayDeque<>();
		newTrails.put(id, trail);
		getNextPoint.put(id, nextPointFunc);
		trail.add(new Pair<>(client.world.getTime(), nextPointFunc.get()));
	}
	
	public void tick()
	{
		if(client.world == null)
		{
			if(trails.size() > 0)
			{
				trails.clear();
				lastRendered.clear();
				deletionQueue.clear();
			}
			return;
		}
		long time = client.world.getTime();
		discardOldTrails(time);
		
		for (UUID id : trails.keySet())
			updateTrail(id, time);
	}
	
	void updateTrail(UUID id, long time)
	{
		Queue<Pair<Long, Vector3f>> trail = trails.get(id);
		int removal = 0;
		boolean invalid = false;
		for (Pair<Long, Vector3f> pair : trail)
		{
			long age = time - pair.getLeft();
			if(age < 0)
			{
				invalid = true;
				break;
			}
			else if(age > 20)
				removal++;
		}
		if(invalid)
		{
			trails.get(id).clear();
			return;
		}
		else if(removal > 0)
			for (int i = 0; i < removal; i++)
				trail.remove();
		if(!deletionQueue.contains(id))
			trail.add(new Pair<>(time, getNextPoint.get(id).get()));
	}
	
	void discardOldTrails(long time)
	{
		List<UUID> discard = new ArrayList<>();
		for (UUID id : lastRendered.keySet())
		{
			long age = time - lastRendered.get(id);
			if(age < 0 || age > 1)
				discard.add(id);
		}
		for (UUID id : discard)
		{
			trails.remove(id);
			lastRendered.remove(id);
			deletionQueue.remove(id);
		}
	}
	
	public void removeTrail(UUID id)
	{
		if(trails.containsKey(id))
			deletionQueue.add(id);
	}
	
	static
	{
		client = MinecraftClient.getInstance();
	}
}
