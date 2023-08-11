package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class ItemOverlayMixin
{
	
	@Shadow public abstract void drawTexture(Identifier texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight);
	
	@Shadow public abstract MatrixStack getMatrices();
	
	@Shadow @Final private MatrixStack matrices;
	
	@Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "HEAD"))
	void onRenderOverlay(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci)
	{
		if(stack.getItem() instanceof AbstractWeaponItem weapon && weapon.hasVariantBG())
		{
			matrices.push();
			RenderSystem.enableBlend();
			RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			int i = weapon.getHUDTexture().x;
			drawTexture(new Identifier(Ultracraft.MOD_ID, "textures/gui/weapon_border.png"), x, y, 0, 16 * (i % 2), 16 * (int)Math.floor(i / 2f), 16, 16, 32, 32);
			matrices.pop();
		}
	}
	
	@Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "TAIL"))
	void onAfterRenderOverlay(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci)
	{
		if (stack.getItem() instanceof AbstractWeaponItem weapon)
			countOverride = weapon.getCountString(stack);
		if(stack.getItem() instanceof AbstractWeaponItem && countOverride != null)
		{
			RenderSystem.disableDepthTest();
			matrices.push();
			matrices.translate(0, 0, 200);
			VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
			textRenderer.draw(countOverride, x, y, 16777215, true, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
			immediate.draw();
			matrices.pop();
			RenderSystem.enableDepthTest();
		}
	}
}
