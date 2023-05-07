package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.rendering.item.PlushieRenderer;
import absolutelyaya.ultracraft.client.rendering.item.PoinPlushieRenderer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;

import java.util.List;
import java.util.function.Consumer;

//The Pitr Plushie is its own item type because it's the only one that moves; the animation is interfered by other plushies otherwise.
public class PitrPoinItem extends PitrItem
{
	public PitrPoinItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private PoinPlushieRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new PoinPlushieRenderer();
				
				return renderer;
			}
		});
	}
}
