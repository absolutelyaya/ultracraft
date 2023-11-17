package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.rendering.item.HellSpawnerItemRenderer;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.BlockItem;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class HellSpawnerItem extends BlockItem implements GeoItem
{
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	final RawAnimation AnimationIdle = RawAnimation.begin().thenLoop("idle");
	
	public HellSpawnerItem(Settings settings)
	{
		super(BlockRegistry.HELL_SPAWNER, settings);
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private HellSpawnerItemRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new HellSpawnerItemRenderer();
				return renderer;
			}
		});
	}
	
	@Override
	public Supplier<Object> getRenderProvider()
	{
		return renderProvider;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		controllers.add(new AnimationController<>(this, "controller", 0, state -> {
			state.setAnimation(AnimationIdle);
			return PlayState.CONTINUE;
		}));
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
