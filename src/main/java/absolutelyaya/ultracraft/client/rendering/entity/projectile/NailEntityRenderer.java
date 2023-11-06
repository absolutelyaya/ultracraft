package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.projectile.NailEntity;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class NailEntityRenderer extends EntityRenderer<NailEntity>
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/item/nail.png");
	
	public NailEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context);
	}
	
	@Override
	public Identifier getTexture(NailEntity entity)
	{
		return TEXTURE;
	}
	
	@Override
	public void render(NailEntity nail, float yaw, float delta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		matrices.push();
		matrices.multiply(new Quaternionf(new AxisAngle4f((-nail.getYaw()) * MathHelper.RADIANS_PER_DEGREE, 0f, 1f, 0f)));
		matrices.multiply(new Quaternionf(new AxisAngle4f((-nail.getPitch(delta) + 90f) * MathHelper.RADIANS_PER_DEGREE, 1f, 0f, 0f)));
		matrices.translate(0f, 0f, -0.05f);
		MinecraftClient client = MinecraftClient.getInstance();
		ItemRenderer renderer = client.getItemRenderer();
		ItemStack stack = ItemRegistry.NAIL.getDefaultStack();
		renderer.renderItem(stack, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, false, matrices, vertexConsumerProvider,
				getLight(nail, delta), OverlayTexture.DEFAULT_UV, renderer.getModel(stack, nail.getWorld(), null, 0));
		matrices.pop();
	}
}
