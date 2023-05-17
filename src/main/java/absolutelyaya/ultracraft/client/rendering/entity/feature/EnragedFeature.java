package absolutelyaya.ultracraft.client.rendering.entity.feature;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.Enrageable;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;

public class EnragedFeature<T extends LivingEntity> extends FeatureRenderer<T, EntityModel<T>>
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/entity/enraged.png");
	static final MinecraftClient client;
	private final EnragedModel<T> enrage;
	
	public EnragedFeature(EntityModelLoader loader)
	{
		super(null);
		enrage = new EnragedModel<>(loader.getModelPart(UltracraftClient.ENRAGE_LAYER));
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
	{
		Enrageable gamer = ((Enrageable)entity);
		if(!gamer.isEnraged() || !entity.isAlive())
			return;
		matrices = new MatrixStack();
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
		Camera cam = MinecraftClient.getInstance().gameRenderer.getCamera();
		Vec3d pos = entity.getPos().subtract(cam.getPos());
		Vec3d scale = gamer.getEnrageFeatureSize();
		Vec3d off = gamer.getEnragedFeatureOffset();
		matrices.multiply(new Quaternionf(new AxisAngle4d(Math.toRadians(cam.getPitch()), 1f, 0f, 0f)));
		matrices.multiply(new Quaternionf(new AxisAngle4d(Math.toRadians(cam.getYaw()), 0f, 1f, 0f)));
		matrices.translate(-pos.x, -(cam.getPos().y - entity.getPos().y), -pos.z);
		matrices.scale((float)scale.x, (float)scale.y, (float)scale.z);
		matrices.translate(off.x, off.y, off.z);
		matrices.multiply(new Quaternionf(new AxisAngle4d(Math.toRadians(cam.getYaw()), 0f, 1f, 0f)));
		matrices.multiply(new Quaternionf(new AxisAngle4d(Math.toRadians(cam.getPitch()), -1f, 0f, 0f)));
		enrage.render(matrices, consumer, 15728880, 0, 1f, 1f, 1f, 1f);
	}
	
	static {
		client = MinecraftClient.getInstance();
	}
}
