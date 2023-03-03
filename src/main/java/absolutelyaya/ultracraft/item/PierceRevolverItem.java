package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.client.rendering.item.PierceRevolverRenderer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PierceRevolverItem extends AbstractWeaponItem implements GeoItem
{
	private static final String controllerName = "pierceRevolverController";
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	protected int approxUseTime = -1, primaryCooldown;
	RawAnimation AnimationCharge = RawAnimation.begin().thenLoop("charged");
	RawAnimation AnimationDischarge = RawAnimation.begin().then("discharge", Animation.LoopType.DEFAULT);
	RawAnimation AnimationShot = RawAnimation.begin().then("shot", Animation.LoopType.DEFAULT);
	
	public PierceRevolverItem(Settings settings)
	{
		super(settings);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		user.setCurrentHand(hand);
		if(world.isClient)
			approxUseTime = 0;
		return TypedActionResult.consume(itemStack);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if(world.isClient)
			return;
		if(approxUseTime >= 0)
		{
			approxUseTime++;
			if(entity instanceof PlayerEntity player)
				triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerWorld)world), controllerName, "charging");
		}
		if(primaryCooldown > 0)
			primaryCooldown--;
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.NONE;
	}
	
	@Override
	public void onPrimaryFire(World world, PlayerEntity user)
	{
		if(!world.isClient && primaryCooldown <= 0)
		{
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), controllerName, "shot");
			ServerHitscanHandler.performHitscan(user, (byte)0, 4);
			primaryCooldown = 10;
		}
	}
	
	@Override
	public int getItemBarStep(ItemStack stack)
	{
		return (int)((float)(10 - primaryCooldown) / 10f * 14f);
	}
	
	@Override
	public boolean isItemBarVisible(ItemStack stack)
	{
		return primaryCooldown > 0;
	}
	
	@Override
	public int getItemBarColor(ItemStack stack)
	{
		return 0x28ccdf;
	}
	
	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks)
	{
		if(remainingUseTicks <= 0)
		{
			if(user instanceof PlayerEntity player)
			{
				if(world.isClient)
					approxCooldown = 50;
				else
				{
					triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerWorld)world), controllerName, "discharge");
					approxUseTime = -1;
				}
				player.getItemCooldownManager().set(this, 50);
			}
			if(!world.isClient)
			{
				ServerHitscanHandler.performHitscan(user, (byte)1, 10, 3);
			}
		}
		else if(!world.isClient && user instanceof PlayerEntity)
		{
			approxUseTime = -1;
			triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerWorld)world), controllerName, "charge");
		}
		approxUseTime = -1;
	}
	
	@Override
	public boolean isUsedOnRelease(ItemStack stack)
	{
		return true;
	}
	
	@Override
	public int getMaxUseTime(ItemStack stack)
	{
		return 15;
	}
	
	public int getApproxUseTime()
	{
		return approxUseTime;
	}
	
	private <P extends Item & GeoItem> PlayState predicate(AnimationState<P> event)
	{
		AnimationController<P> controller = event.getController();
		AnimationProcessor.QueuedAnimation anim = controller.getCurrentAnimation();
		if (anim != null && anim.animation().name().equals("charged"))
		{
			float rotSpeed = 1f - (getMaxUseTime(null) - getApproxUseTime()) / (float)(getMaxUseTime(null));
			event.getController().setAnimationSpeed(MathHelper.clamp(rotSpeed, 0f, 1f));
		}
		else
			controller.setAnimationSpeed(1f);
		
		if(controller.isPlayingTriggeredAnimation())
		{
			System.out.println("ayaya");
			controller.stop();
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, controllerName, 1, this::predicate)
										.triggerableAnim("charging", AnimationCharge)
										.triggerableAnim("discharge", AnimationDischarge)
										.triggerableAnim("shot", AnimationShot));
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private PierceRevolverRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new PierceRevolverRenderer();
				
				return renderer;
			}
		});
	}
	
	@Override
	public Supplier<Object> getRenderProvider()
	{
		return renderProvider;
	}
	
	///TODO: fix animations
	///TODO: fix lighting
}