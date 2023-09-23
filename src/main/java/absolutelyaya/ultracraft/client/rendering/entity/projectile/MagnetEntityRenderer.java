package absolutelyaya.ultracraft.client.rendering.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.projectile.MagnetEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MagnetEntityRenderer extends GeoEntityRenderer<MagnetEntity>
{
	public MagnetEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new MagnetEntityModel());
	}
	
	@Override
	public Identifier getTexture(MagnetEntity entity)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/magnet.png");
	}
	
	@Override
	public void render(MagnetEntity magnet, float yaw, float delta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		matrices.push();
		matrices.multiply(new Quaternionf(new AxisAngle4f((magnet.getYaw() + 182.5f) * MathHelper.RADIANS_PER_DEGREE, 0f, 1f, 0f)));
		matrices.multiply(new Quaternionf(new AxisAngle4f((magnet.getPitch(delta) + 90f) * MathHelper.RADIANS_PER_DEGREE, 1f, 0f, 0f)));
		matrices.translate(0f, 0f, 0f);
		super.render(magnet, yaw, delta, matrices, vertexConsumerProvider, i);
		matrices.pop();
		magnet.setFlash(Math.max(magnet.getFlash() - delta / 10f, 0));
	}
}
