package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ServerHitscanHandler;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.network.GeckoLibNetwork;
import software.bernie.geckolib3.network.ISyncable;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class PierceRevolverItem extends AbstractWeaponItem implements IAnimatable, ISyncable
{
	public AnimationFactory factory = new AnimationFactory(this);
	private static final String controllerName = "pierceRevolverController";
	private static final int ANIM_CHARGE = 0;
	private static final int ANIM_DISCHARGE = 1;
	private static final int ANIM_SHOT = 2;
	protected int approxUseTime = -1, primaryCooldown;
	
	public PierceRevolverItem(Settings settings)
	{
		super(settings);
		GeckoLibNetwork.registerSyncable(this);
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
			{
				final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) world);
				GeckoLibNetwork.syncAnimation(player, this, id, ANIM_CHARGE);
			}
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
			final int id = GeckoLibUtil.guaranteeIDForStack(user.getMainHandStack(), (ServerWorld)world);
			GeckoLibNetwork.syncAnimation(user, this, id, ANIM_SHOT);
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
					final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) world);
					GeckoLibNetwork.syncAnimation(player, this, id, ANIM_DISCHARGE);
					approxUseTime = -1;
				}
				player.getItemCooldownManager().set(this, 50);
			}
			if(!world.isClient)
			{
				ServerHitscanHandler.performHitscan(user, (byte)1, 10, 3);
			}
		}
		else if(!world.isClient && user instanceof PlayerEntity player)
		{
			approxUseTime = -1;
			final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) world);
			GeckoLibNetwork.syncAnimation(player, this, id, ANIM_CHARGE);
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
	
	private <P extends Item & IAnimatable> PlayState predicate(AnimationEvent<P> event)
	{
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimationData data)
	{
		data.addAnimationController(new AnimationController<>(this, controllerName, 1, this::predicate));
	}
	
	@Override
	public AnimationFactory getFactory()
	{
		return factory;
	}
	
	@Override
	public void onAnimationSync(int id, int state)
	{
		@SuppressWarnings("rawtypes")
		final AnimationController controller = GeckoLibUtil.getControllerForID(this.factory, id, controllerName);
		if(controller == null)
			return;
		
		switch (state)
		{
			case ANIM_CHARGE -> {
				float rotSpeed = 1f - (getMaxUseTime(null) - getApproxUseTime()) / (float)(getMaxUseTime(null));
				if(controller.getCurrentAnimation() == null || !controller.getCurrentAnimation().animationName.equals("charged"))
					controller.setAnimation(new AnimationBuilder().addAnimation("charged", true));
				controller.setAnimationSpeed(MathHelper.clamp(rotSpeed, 0f, 1f));
				controller.markNeedsReload();
			}
			case ANIM_DISCHARGE -> {
				controller.setAnimation(new AnimationBuilder().addAnimation("discharge", false));
				controller.setAnimationSpeed(1);
				controller.markNeedsReload();
			}
			case ANIM_SHOT -> {
				controller.setAnimation(new AnimationBuilder().addAnimation("shot", false));
				controller.setAnimationSpeed(1);
				controller.markNeedsReload();
			}
		}
	}
}
