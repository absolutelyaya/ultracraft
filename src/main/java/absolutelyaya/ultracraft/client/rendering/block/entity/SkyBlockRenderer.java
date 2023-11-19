package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.SkyBlockEntity;
import absolutelyaya.ultracraft.client.RenderLayers;
import absolutelyaya.ultracraft.client.UltracraftClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class SkyBlockRenderer implements BlockEntityRenderer<SkyBlockEntity>
{
	@Override
	public void render(SkyBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay)
	{
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayers.getSky());
		for (int i = 0; i < 6; i++)
			RenderSystem.setShaderTexture(i, new Identifier(Ultracraft.MOD_ID, "textures/sky/" + entity.getSkyType() + i + ".png"));
		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		Matrix4f matrix = new Matrix4f(new MatrixStack().peek().getPositionMatrix());
		Vec3d offset = camera.getPos().subtract(entity.getPos().getX(), entity.getPos().getY(), entity.getPos().getZ());
		matrix.translate(offset.toVector3f());
		matrix.rotate(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrix.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw()));
		
		ShaderProgram shader = UltracraftClient.getDaySkyProgram();
		if (shader.getUniform("RotMat") != null)
			shader.getUniform("RotMat").set(matrix);
		
		matrix = matrices.peek().getPositionMatrix();
		if(Block.shouldDrawSide(entity.getCachedState(), entity.getWorld(), entity.getPos(), Direction.DOWN, entity.getPos().down()))
		{
			consumer.vertex(matrix, 0, 0, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 1, 0, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 1, 0, 1).normal(0, -1, 0).next();
			consumer.vertex(matrix, 0, 0, 1).normal(0, -1, 0).next();
		}
		if(Block.shouldDrawSide(entity.getCachedState(), entity.getWorld(), entity.getPos(), Direction.UP, entity.getPos().up()))
		{
			consumer.vertex(matrix, 1, 1, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 0, 1, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 0, 1, 1).normal(0, -1, 0).next();
			consumer.vertex(matrix, 1, 1, 1).normal(0, -1, 0).next();
		}
		if(Block.shouldDrawSide(entity.getCachedState(), entity.getWorld(), entity.getPos(), Direction.NORTH, entity.getPos().north()))
		{
			consumer.vertex(matrix, 1, 0, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 0, 0, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 0, 1, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 1, 1, 0).normal(0, -1, 0).next();
		}
		if(Block.shouldDrawSide(entity.getCachedState(), entity.getWorld(), entity.getPos(), Direction.SOUTH, entity.getPos().south()))
		{
			consumer.vertex(matrix, 0, 0, 1).normal(0, -1, 0).next();
			consumer.vertex(matrix, 1, 0, 1).normal(0, -1, 0).next();
			consumer.vertex(matrix, 1, 1, 1).normal(0, -1, 0).next();
			consumer.vertex(matrix, 0, 1, 1).normal(0, -1, 0).next();
		}
		if(Block.shouldDrawSide(entity.getCachedState(), entity.getWorld(), entity.getPos(), Direction.WEST, entity.getPos().west()))
		{
			consumer.vertex(matrix, 0, 1, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 0, 0, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 0, 0, 1).normal(0, -1, 0).next();
			consumer.vertex(matrix, 0, 1, 1).normal(0, -1, 0).next();
		}
		if(Block.shouldDrawSide(entity.getCachedState(), entity.getWorld(), entity.getPos(), Direction.EAST, entity.getPos().east()))
		{
			consumer.vertex(matrix, 1, 0, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 1, 1, 0).normal(0, -1, 0).next();
			consumer.vertex(matrix, 1, 1, 1).normal(0, -1, 0).next();
			consumer.vertex(matrix, 1, 0, 1).normal(0, -1, 0).next();
		}
	}
}
