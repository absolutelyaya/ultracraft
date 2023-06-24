package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemOverlayMixin
{
	@Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "HEAD"))
	void onRenderOverlay(MatrixStack matrices, TextRenderer textRenderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci)
	{
		if(stack.getItem() instanceof AbstractWeaponItem weapon)
		{
			matrices.push();
			RenderSystem.enableBlend();
			RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
			RenderSystem.setShaderTexture(0, new Identifier(Ultracraft.MOD_ID, "textures/gui/weapon_border.png"));
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			int i = weapon.getHUDTexture().x;
			DrawableHelper.drawTexture(matrices, x, y, 0, 16 * (i % 2), 16 * (int)Math.floor(i / 2f), 16, 16, 32, 32);
			matrices.pop();
		}
	}
	
	@Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "TAIL"))
	void onAfterRenderOverlay(MatrixStack matrices, TextRenderer textRenderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci)
	{
		if (stack.getItem() instanceof AbstractWeaponItem weapon)
			countLabel = weapon.getCountString(stack);
		if(stack.getItem() instanceof AbstractWeaponItem && countLabel != null)
		{
			RenderSystem.disableDepthTest();
			matrices.push();
			matrices.translate(0, 0, 500);
			VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
			textRenderer.draw(countLabel, x, y, 16777215, true, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
			immediate.draw();
			matrices.pop();
			RenderSystem.enableDepthTest();
		}
	}
}
