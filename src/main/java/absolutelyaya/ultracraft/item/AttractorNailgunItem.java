package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.AttractorNailgunRenderer;
import absolutelyaya.ultracraft.components.IWingedPlayerComponent;
import absolutelyaya.ultracraft.entity.projectile.MagnetEntity;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.joml.Vector2i;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AttractorNailgunItem extends AbstractNailgunItem
{
	final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
	final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	
	public AttractorNailgunItem(Settings settings)
	{
		super(settings);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	public void onAltFire(World world, PlayerEntity user)
	{
		super.onAltFire(world, user);
		MagnetEntity magnet = MagnetEntity.spawn(user, user.getEyePos(), user.getRotationVector().multiply(1.5f));
		world.spawnEntity(magnet);
		ItemStack stack = user.getMainHandStack();
		setNbt(stack, "magnets", getNbt(stack, "magnets") - 1);
		IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(user);
		winged.setMagnets(winged.getMagnets() + 1);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		int magnets = UltraComponents.WINGED_ENTITY.get(user).getMagnets();
		ItemStack itemStack = user.getStackInHand(hand);
		if(magnets >= 3 || getNbt(itemStack, "magnets") <= 0)
			return TypedActionResult.fail(itemStack);
		return super.use(world, user, hand);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		if(entity instanceof PlayerEntity)
		{
			IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(entity);
			GunCooldownManager gcdm = winged.getGunCooldownManager();
			if(gcdm.isUsable(this, GunCooldownManager.SECONDARY) && getNbt(stack, "magnets") < 3 - winged.getMagnets())
			{
				setNbt(stack, "magnets", getNbt(stack, "magnets") + 1);
				gcdm.setCooldown(this, 10, GunCooldownManager.SECONDARY);
			}
		}
		super.inventoryTick(stack, world, entity, slot, selected);
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
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
