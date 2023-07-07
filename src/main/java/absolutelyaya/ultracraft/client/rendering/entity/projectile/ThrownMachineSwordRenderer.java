package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.entity.projectile.ThrownMachineSwordEntity;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

public class ThrownMachineSwordRenderer extends EntityRenderer<ThrownMachineSwordEntity>
{
	private final ItemRenderer itemRenderer;
	
	public ThrownMachineSwordRenderer(EntityRendererFactory.Context ctx)
	{
		super(ctx);
		itemRenderer = ctx.getItemRenderer();
	}
	
	@Override
	public void render(ThrownMachineSwordEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		matrices.push();
		matrices.scale(1.5f, 1.5f, 1.5f);
		float rot = entity.age * 60;
		matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(yaw), 0f, 1f, 0f)));
		matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(entity.lastRot = MathHelper.lerp(tickDelta, entity.lastRot, rot)), 1f, 0f, 0f)));
		matrices.translate(0f, -0.5f, 0f);
		itemRenderer.renderItem(entity.getStack().isEmpty() ? ItemRegistry.MACHINE_SWORD.getDefaultStack() : entity.getStack(),
				ModelTransformationMode.NONE, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId());
		matrices.pop();
	}
	
	@Override
	public Identifier getTexture(ThrownMachineSwordEntity entity)
	{
		return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
	}
}
