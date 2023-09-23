package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.rendering.item.AttractorNailgunRenderer;
import absolutelyaya.ultracraft.entity.projectile.MagnetEntity;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.joml.Vector2i;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AttractorNailgunItem extends AbstractNailgunItem
{
	final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
	final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	
	public AttractorNailgunItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public void onAltFire(World world, PlayerEntity user)
	{
		super.onAltFire(world, user);
		MagnetEntity magnet = MagnetEntity.spawn(user, user.getEyePos(), user.getRotationVector().multiply(1.5f));
		world.spawnEntity(magnet);
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return new Vector2i(0, 2);
	}
	
	@Override
	String getControllerName()
	{
		return "attractor_nailgun";
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private AttractorNailgunRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new AttractorNailgunRenderer();
				
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
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
	
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
