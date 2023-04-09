package absolutelyaya.ultracraft.client.rendering;

import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.client.UltracraftClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;
import java.util.function.Supplier;

public class TrailRenderer
{
	static Map<UUID, Trail> trails = new HashMap<>();
	static Map<UUID, Trail> newTrails = new HashMap<>();
	static List<UUID> deletionQueue = new ArrayList<>();
	static MinecraftClient client;
	static int maxAge = 40;
	
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
			Trail trail = trails.get(id);
			VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();
			//actually render now
			List<Pair<Long, Pair<Vector3f, Vector3f>>> trailCopy = trail.points.stream().toList();
			if(UltracraftClient.getConfigHolder().getConfig().trailLines)
				renderAsLines(id, trailCopy, matrix, immediate);
			else
				renderAsQuads(trailCopy, matrix, immediate);
			trail.lastRendered = time;
		}
		for (int i = 0; i < newTrails.size(); i++)
		{
			UUID id = (UUID)newTrails.keySet().toArray()[i];
			trails.put(id, newTrails.get(id));
		}
		newTrails.clear();
		matrixStack.pop();
	}
	
	public void renderAsLines(UUID id, List<Pair<Long, Pair<Vector3f, Vector3f>>> trail, Matrix4f matrix, VertexConsumerProvider.Immediate immediate)
	{
		VertexConsumer consumer = immediate.getBuffer(RenderLayer.LINES);
		consumer.fixedColor(255, 225, 125, 255);
		Vector3f last = null;
		Vec3d normal = client.getCameraEntity().getRotationVector().multiply(-1f);
		Vector3f pos = trails.get(id).pointSupplier.get().getLeft();
		if(deletionQueue.contains(id))
		{
			pos = trail.get(trail.size() - 1).getRight().getLeft();
			consumer.vertex(matrix, pos.x, pos.y, pos.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
		}
		else
			consumer.vertex(matrix, pos.x, pos.y, pos.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
		for (int i = trail.size() - 1; i >= 0; i--)
		{
			Vector3f vec = trail.get(i).getRight().getLeft();
			if(last != null)
				consumer.vertex(matrix, last.x, last.y, last.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
			consumer.vertex(matrix, vec.x, vec.y, vec.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
			last = new Vector3f(vec);
		}
		consumer.vertex(matrix, last.x, last.y, last.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
		consumer.fixedColor(255, 120, 75, 255);
		if(deletionQueue.contains(id))
		{
			pos = trail.get(0).getRight().getRight();
			consumer.vertex(matrix, pos.x, pos.y, pos.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
		}
		else
			consumer.vertex(matrix, last.x, last.y, last.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
		for (Pair<Long, Pair<Vector3f, Vector3f>> longPairPair : trail)
		{
			Vector3f vec = longPairPair.getRight().getRight();
			if (last != null)
				consumer.vertex(matrix, last.x, last.y, last.z).normal((float) normal.x, (float) normal.y, (float) normal.z).next();
			consumer.vertex(matrix, vec.x, vec.y, vec.z).normal((float) normal.x, (float) normal.y, (float) normal.z).next();
			last = new Vector3f(vec);
		}
		consumer.unfixColor();
	}
	
	public void renderAsQuads(List<Pair<Long, Pair<Vector3f, Vector3f>>> trail, Matrix4f matrix, VertexConsumerProvider.Immediate immediate)
	{
		//POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
		VertexConsumer consumer = immediate.getBuffer(RenderLayers.getLightTrail());
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableTexture();
		Pair<Vector3f, Vector3f> last = null;
		Vector3f normal = client.getCameraEntity().getRotationVector().multiply(-1f).toVector3f();
		Vector4f col = new Vector4f(1f, 0.5f, 0f, 0.6f);
		for (int i = trail.size() - 1; i >= 0; i--)
		{
			Vector3f vec = trail.get(i).getRight().getLeft();
			Vector3f vec2 = trail.get(i).getRight().getRight();
			long age = client.world.getTime() - trail.get(i).getLeft();
			float fade = Math.max((1f - (float)age / maxAge), 0f);
			consumer.vertex(matrix, vec2.x, vec2.y, vec2.z).color(col.x, col.y, col.z, col.w * fade).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal.x, normal.y, normal.z).next();
			if(last != null)
				consumer.vertex(matrix, last.getRight().x, last.getRight().y, last.getRight().z).color(col.x, col.y, col.z, col.w * fade).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal.x, normal.y, normal.z).next();
			if(last != null)
				consumer.vertex(matrix, last.getLeft().x, last.getLeft().y, last.getLeft().z).color(col.x, col.y, col.z, col.w * fade).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal.x, normal.y, normal.z).next();
			consumer.vertex(matrix, vec.x, vec.y, vec.z).color(col.x, col.y, col.z, col.w * fade).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal.x, normal.y, normal.z).next();
			last = new Pair<>(new Vector3f(vec), new Vector3f(vec2));
		}
		consumer.vertex(matrix, last.getRight().x, last.getRight().y, last.getRight().z).color(col.x, col.y, col.z, 0f).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal.x, normal.y, normal.z).next();
		consumer.vertex(matrix, last.getLeft().x, last.getLeft().y, last.getLeft().z).color(col.x, col.y, col.z, 0f).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal.x, normal.y, normal.z).next();
	}
	
	public void createTrail(UUID id, Supplier<Pair<Vector3f, Vector3f>> nextPointFunc)
	{
		Trail trail = new Trail(nextPointFunc);
		newTrails.put(id, trail);
		trail.add(nextPointFunc.get());
	}
	
	public void tick()
	{
		if(client.world == null)
		{
			if(trails.size() > 0)
			{
				trails.clear();
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
		Trail trail = trails.get(id);
		int removal = 0;
		boolean invalid = false;
		for (Pair<Long, Pair<Vector3f, Vector3f>> pair : trail.points)
		{
			long age = time - pair.getLeft();
			if(age < 0)
			{
				invalid = true;
				break;
			}
			else if(age > maxAge)
				removal++;
		}
		if(invalid)
		{
			trails.get(id).points.clear();
			return;
		}
		else if(removal > 0)
			for (int i = 0; i < removal; i++)
				trail.remove();
		if(!deletionQueue.contains(id))
			trail.add(trail.pointSupplier.get());
	}
	
	void discardOldTrails(long time)
	{
		List<UUID> discard = new ArrayList<>();
		for (UUID id : trails.keySet())
		{
			long age = time - trails.get(id).lastRendered;
			if(age < 0 || age > 1)
				discard.add(id);
		}
		for (UUID id : discard)
		{
			trails.remove(id);
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
	
	public static class Trail
	{
		Queue<Pair<Long, Pair<Vector3f, Vector3f>>> points;
		Long lastRendered;
		Supplier<Pair<Vector3f, Vector3f>> pointSupplier;
		
		public Trail(Supplier<Pair<Vector3f, Vector3f>> pointSupplier)
		{
			this.points = new ArrayDeque<>();
			this.lastRendered = client.world.getTime();
			this.pointSupplier = pointSupplier;
		}
		
		public int size()
		{
			return points.size();
		}
		
		public void add(Pair<Vector3f, Vector3f> point)
		{
			points.add(new Pair<>(client.world.getTime(), point));
		}
		
		public void remove()
		{
			points.remove();
		}
	}
}
