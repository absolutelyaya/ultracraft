package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ChainParryAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.rendering.entity.other.ParryAuraRenderer;
import absolutelyaya.ultracraft.util.RenderingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity>
{
	private static final Identifier BLOCKED_ICON = new Identifier(Ultracraft.MOD_ID, "textures/gui/blocked_wings.png");
	
	@Shadow public abstract TextRenderer getTextRenderer();
	
	@Inject(method = "render", at = @At("HEAD"))
	void onRender(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
	{
		if(entity instanceof ProjectileEntity projectile && ((ChainParryAccessor)projectile).getParryCount() > 0)
			ParryAuraRenderer.render(projectile, matrices, vertexConsumers);
	}
	
	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", ordinal = 0))
	void onRenderLabel(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
	{
		if(entity instanceof WingedPlayerEntity winged && winged.isBlocked())
		{
			matrices.push();
			matrices.translate(getTextRenderer().getWidth(text) / 2f + 2, -1, 0);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
			RenderSystem.setShaderTexture(0, BLOCKED_ICON);
			RenderingUtil.drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(0f, 0f, 10f, 10f), new Vec2f(16, 16), new Vector4f(0, 16, 16, -16));
			matrices.pop();
		}
	}
}
