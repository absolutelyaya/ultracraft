package absolutelyaya.ultracraft.client.rendering.block.entity;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

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
		poseStack.push(); //POSITION_COLOR
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.multiply(new Quaternionf(new AxisAngle4f(animatable.getRotation() * -MathHelper.RADIANS_PER_DEGREE, 0f, 1f, 0f)));
		poseStack.translate(0f, 0f, -0.1f);
		VertexConsumer consumer = bufferSource.getBuffer(RenderLayer.getGui());
		Matrix4f poseMatrix = new Matrix4f(poseStack.peek().getPositionMatrix());
		Matrix3f normalMatrix = new Matrix3f(poseStack.peek().getNormalMatrix());
		int l = 15728880;
		int BGcolor = 0xaa000000;
		consumer.vertex(poseMatrix, -0.5f, -0.5f, 0f).color(BGcolor).texture(0, 0).light(l).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, -0.5f, 0.5f, 0f).color(BGcolor).texture(0, 1).light(l).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0.5f, 0.5f, 0f).color(BGcolor).texture(1, 1).light(l).normal(normalMatrix, 0f, 0f, -1f).next();
		consumer.vertex(poseMatrix, 0.5f, -0.5f, 0f).color(BGcolor).texture(1, 0).light(l).normal(normalMatrix, 0f, 0f, -1f).next();
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		poseStack.translate(0.5f, -0.5f, -0.005f);
		poseStack.scale(-0.01f, -0.01f, 0.01f);
		int lines = 12;
		for (int i = 1; i < lines; i++)
		{
			if(i == 1 || i == 11)
				renderer.draw("+------TT------+", 2, -renderer.fontHeight * i, 0xffffffff, false, new Matrix4f(poseStack.peek().getPositionMatrix()), bufferSource, TextRenderer.TextLayerType.NORMAL, 0x00000000, l, false);
			else
				renderer.draw("|                      |", 4, -renderer.fontHeight * i, 0xffffffff, false, new Matrix4f(poseStack.peek().getPositionMatrix()), bufferSource, TextRenderer.TextLayerType.NORMAL, 0x00000000, l, false);
		}
		poseStack.pop();
		
		//TODO: Draw Paintables
	}
}
