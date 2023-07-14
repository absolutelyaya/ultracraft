package absolutelyaya.ultracraft.client.rendering.entity.other;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ChainParryAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;

public class ParryAuraRenderer
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/misc/parry_auras.png");
	static final Vec3i[] colors = new Vec3i[] {
			new Vec3i(160, 19, 4), new Vec3i(198, 81, 18), new Vec3i(231, 143, 12), new Vec3i(239, 221, 57), new Vec3i(144, 239, 134),
			new Vec3i(100, 233, 180), new Vec3i(76, 244, 227), new Vec3i(158, 255, 249), new Vec3i(130, 172, 255), new Vec3i(175, 161, 255),
			new Vec3i(250, 181, 255), new Vec3i(201, 30, 107), new Vec3i(228, 83, 34), new Vec3i(255, 157, 0), new Vec3i(180, 255, 148),
			new Vec3i(255, 0, 0), new Vec3i(0, 0, 0), new Vec3i(255, 184, 211), new Vec3i(76, 43, 128), new Vec3i(255, 168, 200)
	};
	
	public static void render(ProjectileEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers)
	{
		int parryCount = Math.min(((ChainParryAccessor)entity).getParryCount() - 1, 19);
		Vector2f uv = new Vector2f(parryCount % 5, (float)Math.floor(parryCount / 5f)).mul(0.1875f);
		matrices.push();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.enableBlend();
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
		matrices.translate(0f, 0.125f, 0f);
		matrices.scale(7.5f, 7.5f, 7.5f);
		Quaternionf camRot = new Quaternionf(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
		matrices.multiply(camRot.rotateY((float)Math.toRadians(180)));
		Matrix4f matrix = new Matrix4f(matrices.peek().getPositionMatrix());
		Matrix3f normalMatrix = new Matrix3f(matrices.peek().getNormalMatrix());
		Vec3i color = colors[parryCount];
		consumer.vertex(matrix, -0.1f, -0.1f, 0f).color(color.getX(), color.getY(), color.getZ(), 255).texture(uv.x, uv.y + 0.1875f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, 0.1f, -0.1f, 0f).color(color.getX(), color.getY(), color.getZ(), 255).texture(uv.x + 0.1875f, uv.y + 0.1875f)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, 0.1f, 0.1f, 0f).color(color.getX(), color.getY(), color.getZ(), 255).texture(uv.x + 0.1875f, uv.y)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		consumer.vertex(matrix, -0.1f, 0.1f, 0f).color(color.getX(), color.getY(), color.getZ(), 255).texture(uv.x, uv.y)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0f, 1f, 0f).next();
		matrices.pop();
	}
}
