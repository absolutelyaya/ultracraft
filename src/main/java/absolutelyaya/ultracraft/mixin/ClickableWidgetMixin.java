package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WidgetAccessor;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClickableWidget.class)
public class ClickableWidgetMixin implements WidgetAccessor
{
	@Shadow private int y;
	@Shadow private int x;
	@Shadow protected int width;
	@Shadow public boolean active;
	@Shadow protected float alpha;
	int offsetX, offsetY;
	
	//@Inject(method = "isMouseOver", at = @At("HEAD"), cancellable = true)
	//void onIsMouseOver(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir)
	//{
	//	cir.setReturnValue(active && visible &&
	//				   mouseX >= (double)x + offsetX &&
	//				   mouseY >= (double)y + offsetY &&
	//				   mouseX < (double)(x + width + offsetX) &&
	//				   mouseY < (double)(y + height + offsetY));
	//}
	//
	//@Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
	//void onClicked(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir)
	//{
	//	cir.setReturnValue(active && visible &&
	//							   mouseX >= (double)x + offsetX &&
	//							   mouseY >= (double)y + offsetY &&
	//							   mouseX < (double)(x + width + offsetX) &&
	//							   mouseY < (double)(y + height + offsetY));
	//}
	
	@Inject(method = "getX", at = @At("HEAD"), cancellable = true)
	void onGetX(CallbackInfoReturnable<Integer> cir)
	{
		cir.setReturnValue(x + offsetX);
	}
	
	@Inject(method = "getY", at = @At("HEAD"), cancellable = true)
	void onGetY(CallbackInfoReturnable<Integer> cir)
	{
		cir.setReturnValue(y + offsetY);
	}
	
	@Override
	public void setOffset(Vector2i pos)
	{
		offsetX = pos.x;
		offsetY = pos.y;
	}
	
	@Override
	public Vector2i getOffset()
	{
		return new Vector2i(offsetX, offsetY);
	}
	
	@Override
	public void setActive(boolean b)
	{
		active = b;
	}
	
	@Override
	public void setAlpha(float alpha)
	{
		this.alpha = alpha;
	}
}
