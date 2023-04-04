package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.rendering.item.MachineSwordRenderer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.joml.Vector2i;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MachineSwordItem extends AbstractWeaponItem implements GeoItem
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	
	public MachineSwordItem(Settings settings)
	{
		super(settings);
	}
	
	//TODO: throw action
	
	@Override
	public void onPrimaryFire(World world, PlayerEntity user)
	{
	
	}
	
	@Override
	public boolean shouldAim()
	{
		return false;
	}
	
	@Override
	public boolean shouldCancelHits()
	{
		return false;
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return new Vector2i(0, 2);
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private MachineSwordRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new MachineSwordRenderer();
				
				return renderer;
			}
		});
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "sword", 0, ignored -> PlayState.STOP));
	}
	
	@Override
	public Supplier<Object> getRenderProvider()
	{
		return renderProvider;
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
