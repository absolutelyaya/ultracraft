package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.MachineSwordItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.*;

public class MachineSwordRenderer extends GeoItemRenderer<MachineSwordItem>
{
	MinecraftClient client;
	static Map<Long, Queue<Pair<Long, Vector3f>>> trails = new HashMap<>();
	static Map<Long, Matrix4f> lastTransform = new HashMap<>();
	static Map<Long, Long> lastRendered = new HashMap<>();
	long lastProcessTime;
	Vec3d lastCamPos;
	Vec3d lastCamRot;
	
	public MachineSwordRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "machinesword")));
		client = MinecraftClient.getInstance();
	}
	
	@Override
	public void render(ItemStack stack, ModelTransformation.Mode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay)
	{
		super.render(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
		long time = client.world.getTime();
		if(time != lastProcessTime)
			discardOldTrails(time);
		if(!transformType.equals(ModelTransformation.Mode.NONE))
			return;
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(client.gameRenderer.getBasicProjectionMatrix(100));
		long id = getInstanceId(animatable);
		if(trails.containsKey(id))
		{
			if(time != lastProcessTime)
				for (long iid : trails.keySet())
					updateTrail(iid, time, poseStack.peek().getPositionMatrix());
			if(!trails.containsKey(id))
			{
				RenderSystem.restoreProjectionMatrix();
				return;
			}
			//still updating transformation and other preparations
			Queue<Pair<Long, Vector3f>> trail = trails.get(id);
			VertexConsumer consumer = bufferSource.getBuffer(RenderLayer.LINES);
			consumer.fixedColor(255, 225, 125, 255);
			Vector3f last = null;
			Vec3d normal = client.getCameraEntity().getRotationVector().multiply(-1f);
			Vector3f pos = poseStack.peek().getPositionMatrix().transformPosition(new Vector3f(0.7f, 1.5f, 0f));
			Camera cam = client.gameRenderer.getCamera();
			Vec3d camPos = cam.getPos();
			consumer.vertex(pos.x, pos.y, pos.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
			if(lastCamPos == null)
				lastCamPos = camPos;
			Vec3d camDelta = camPos.subtract(lastCamPos).multiply(1f, -1f, 1f);
			Vec3d camRot = new Vec3d(cam.getPitch(), cam.getYaw(), 0f);
			if(lastCamRot == null)
				lastCamRot = camRot;
			Vec3d camRotDelta = camRot.subtract(lastCamRot);
			camDelta = camDelta.rotateX(-(float)Math.toRadians(camRot.x)).rotateY((float)Math.toRadians(camRot.y));
			//actually render now
			for (Pair<Long, Vector3f> pair : trail)
			{
				pair.setRight(pair.getRight().add((float)camDelta.x, (float)camDelta.y, (float)camDelta.z)
									  .rotateX((float)Math.toRadians(camRotDelta.x)).rotateY((float)Math.toRadians(camRotDelta.y)));
				Vector3f vec = pair.getRight();
				consumer.vertex(vec.x, vec.y, vec.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
				if(last != null)
					consumer.vertex(last.x, last.y, last.z).normal((float)normal.x, (float)normal.y, (float)normal.z).next();
				last = new Vector3f(vec);
			}
			consumer.unfixColor();
			lastRendered.put(id, time);
			lastCamPos = camPos;
			lastCamRot = camRot;
		}
		else
		{
			Queue<Pair<Long, Vector3f>> trail = new ArrayDeque<>();
			trails.put(id , trail);
			lastTransform.put(id, poseStack.peek().getPositionMatrix());
			lastRendered.put(id, time);
			trail.add(new Pair<>(time, new Vector3f(0.7f, 1.5f, 0f)));
			System.out.println("created trail");
		}
		lastProcessTime = time;
		RenderSystem.restoreProjectionMatrix();
	}
	
	void updateTrail(long id, long time, Matrix4f matrix)
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
			//pair.setRight(diff.transformPosition(pair.getRight()));
		}
		if(time != lastProcessTime && trail.peek() != null /*&& (time - trail.peek().getLeft()) % 5 == 0*/)
		{
			trail.add(new Pair<>(time, matrix.transformPosition(new Vector3f(0.7f, 1.5f, 0f))));
		}
		if(invalid)
		{
			trails.remove(id);
			lastTransform.remove(id);
			lastRendered.remove(id);
		}
		else if(removal > 0)
		{
			for (int i = 0; i < removal; i++)
			{
				trail.remove();
			}
		}
	}
	
	void discardOldTrails(long time)
	{
		List<Long> discard = new ArrayList<>();
		for (long id : lastRendered.keySet())
		{
			long age = time - lastRendered.get(id);
			if(age < 0 || age > 1)
				discard.add(id);
		}
		for (Long id : discard)
		{
			trails.remove(id);
			lastTransform.remove(id);
			lastRendered.remove(id);
			System.out.println("Trail [" + id + "] discarded");
		}
	}
}
