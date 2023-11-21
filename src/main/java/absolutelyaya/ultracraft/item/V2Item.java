package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.rendering.item.V2PlushieRenderer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import mod.azure.azurelib.animatable.client.RenderProvider;

import java.util.function.Consumer;

public class V2Item extends PlushieItem
{
	public V2Item(Settings settings)
	{
		super(settings);
		defaultType = Type.V2;
	}
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private V2PlushieRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new V2PlushieRenderer();
				
				return renderer;
			}
		});
	}
}