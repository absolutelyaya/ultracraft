package absolutelyaya.ultracraft.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;

import java.util.List;

//The Pitr Plushie is its own item type because it's the only one that moves; the animation is interfered by other plushies otherwise.
public class PitrItem extends PlushieItem
{
	public PitrItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
		if(context.isAdvanced())
			tooltip.add(Text.translatable("item.ultracraft.plushie.pitr-hiddenlore"));
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "pitr", 0, this::predicate));
	}
}
