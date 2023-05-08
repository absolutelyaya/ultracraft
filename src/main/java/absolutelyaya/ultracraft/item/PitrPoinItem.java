package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.rendering.item.PoinPlushieRenderer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import software.bernie.geckolib.animatable.client.RenderProvider;

import java.util.function.Consumer;

//Woah! Two Pitr items! And just because I wanted a stupid alternate easter egg model after playing "Poinies Poin" and seeing a certain resemblance.
public class PitrPoinItem extends PitrItem
{
	public PitrPoinItem(Settings settings)
	{
		super(settings);
		defaultType = Type.PITRPOIN;
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
