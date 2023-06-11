package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.client.rendering.item.MachineSwordRenderer;
import absolutelyaya.ultracraft.entity.projectile.ThrownMachineSwordEntity;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MachineSwordItem extends SwordItem implements GeoItem
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	
	public MachineSwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings)
	{
		super(toolMaterial, attackDamage, attackSpeed, settings);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	public ItemStack getSwordInstance(ServerWorld world)
	{
		ItemStack stack = getDefaultStack();
		GeoItem.getOrAssignId(stack, world);
		return stack;
	}
	
	@Override
	public int getMaxUseTime(ItemStack stack)
	{
		return 2530;
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.BOW;
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(hand.equals(Hand.OFF_HAND))
			return TypedActionResult.pass(itemStack);
		user.setCurrentHand(hand);
		return TypedActionResult.consume(itemStack);
	}
	
	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks)
	{
		if(remainingUseTicks - 2500 <= 30)
		{
			float distance = MathHelper.lerp(Math.max(remainingUseTicks - 2500, 0) / 30f, 40f, 10f);
			if(!world.isClient())
				GeoItem.getOrAssignId(stack, (ServerWorld)world);
			ThrownMachineSwordEntity thrown = ThrownMachineSwordEntity.spawn(world, user, stack.copy(), distance);
			Vec3d dir = new Vec3d(0f, 0f, 1f);
			dir = dir.rotateX((float)Math.toRadians(-user.getPitch()));
			dir = dir.rotateY((float)Math.toRadians(-user.getYaw()));
			thrown.setVelocity(dir.x, dir.y, dir.z, 1f, 0.0f);
			if(user instanceof PlayerEntity p && !p.isCreative())
				stack.decrement(1);
		}
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
