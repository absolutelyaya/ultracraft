package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.util.List;

public class TerminalBlockEntityRenderer extends GeoBlockRenderer<TerminalBlockEntity>
{
	public TerminalBlockEntityRenderer()
	{
		super(new TerminalBlockEntityModel());
	}
	
	@Override
	public void actuallyRender(MatrixStack poseStack, TerminalBlockEntity animatable, BakedGeoModel model, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		poseStack.push();
		poseStack.translate(0, -1, 0);
		super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.pop();
	}
	
	@Override
	public void postRender(MatrixStack poseStack, TerminalBlockEntity animatable, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		//Draw interface
		float playerDist = (float) MinecraftClient.getInstance().player.getPos().distanceTo(animatable.getPos().toCenterPos());
		float displayVisibility = animatable.getDisplayVisibility();
		if(playerDist < 4f && displayVisibility < 1f)
			displayVisibility += partialTick / 5f;
		else if(playerDist > 4f && displayVisibility > 0f)
			displayVisibility -= partialTick / 3f;
		displayVisibility = MathHelper.clamp(displayVisibility, 0f, 1f);
		animatable.setDisplayVisibility(displayVisibility);
		if(displayVisibility > 0f)
			renderDisplay(poseStack, animatable, bufferSource);
		
		//TODO: Draw Paintables
	}
	
	void renderDisplay(MatrixStack matrices, TerminalBlockEntity terminal, VertexConsumerProvider buffers)
	{
		matrices.push(); //POSITION_COLOR
		matrices.translate(0.5f, 0.5f, 0.5f);
		matrices.multiply(new Quaternionf(new AxisAngle4f(animatable.getRotation() * -MathHelper.RADIANS_PER_DEGREE, 0f, 1f, 0f)));
		matrices.translate(0f, 0f, -0.1f);
		float displayVisibility = terminal.getDisplayVisibility();
		matrices.scale(Math.min(displayVisibility / 0.5f, 1f), displayVisibility, Math.min(displayVisibility / 0.5f, 1f));
		VertexConsumer consumer = buffers.getBuffer(RenderLayer.getGui());
		Matrix4f poseMatrix = new Matrix4f(matrices.peek().getPositionMatrix());
		Matrix3f normalMatrix = new Matrix3f(matrices.peek().getNormalMatrix());
		int l = 15728880;
		int BGcolor = 0xaa000000;
		consumer.vertex(poseMatrix, -0.5f, -0.5f, 0f).color(BGcolor).texture(0, 0).light(l).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, -0.5f, 0.5f, 0f).color(BGcolor).texture(0, 1).light(l).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0.5f, 0.5f, 0f).color(BGcolor).texture(1, 1).light(l).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0.5f, -0.5f, 0f).color(BGcolor).texture(1, 0).light(l).normal(normalMatrix, 0f, 0f, -1f).next();
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		matrices.translate(0.5f, -0.5f, -0.005f);
		matrices.scale(-0.01f, -0.01f, 0.01f);
		List<String> lines = animatable.getLines();
		for (int i = 0; i < lines.size(); i++)
			renderer.draw(lines.get(i), 2, -renderer.fontHeight * (i + 1), animatable.getTextColor(), false,
					new Matrix4f(matrices.peek().getPositionMatrix()), buffers, TextRenderer.TextLayerType.NORMAL, 0x00000000, l, false);
		matrices.pop();
	}
}
