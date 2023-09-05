package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.entity.projectile.HarpoonEntity;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class HarpoonEntityRenderer extends ProjectileEntityRenderer<HarpoonEntity>
{
	public HarpoonEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context);
	}
	
	@Override
	public Identifier getTexture(HarpoonEntity entity)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/item/harpoon_long.png");
	}
	
	@Override
	public void render(HarpoonEntity harpoon, float yaw, float delta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		matrices.push();
		matrices.multiply(new Quaternionf(new AxisAngle4f((-harpoon.getPitch(delta) - 20f) * MathHelper.RADIANS_PER_DEGREE, 0f, 0f, 1f)));
		matrices.multiply(new Quaternionf(new AxisAngle4f((harpoon.getYaw() + 182.5f) * MathHelper.RADIANS_PER_DEGREE, 0f, 1f, 0f)));
		matrices.translate(0f, -0.2f, 0.5f);
		MinecraftClient client = MinecraftClient.getInstance();
		ItemRenderer renderer = client.getItemRenderer();
		ItemStack stack = ItemRegistry.HARPOON.getDefaultStack();
		renderer.renderItem(stack, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, false, matrices, vertexConsumerProvider,
				getLight(harpoon, delta), OverlayTexture.DEFAULT_UV, renderer.getModel(stack, harpoon.getWorld(), null, 0));
		matrices.pop();
		matrices.push();
		UltracraftClient.HITSCAN_HANDLER.addEntry(harpoon.getPos(), new Vec3d(harpoon.getStartPosition()), (byte)0);
		//HitscanRenderer.renderRay(matrices, new Vec3d(harpoon.getStartPosition()), harpoon.getPos(), client.gameRenderer.getCamera().getPos(), 0.3f,
		//		Color.BLACK, RenderLayer.getGui(), 1);
		matrices.pop();
	}
}
