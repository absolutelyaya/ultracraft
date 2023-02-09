package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ServerHitscanHandler;
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
	protected int approxUseTime;
	float lastRotSpeed;
	
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
			approxUseTime++;
		else
		{
			final int id = GeckoLibUtil.guaranteeIDForStack(user.getStackInHand(hand), (ServerWorld) world);
			GeckoLibNetwork.syncAnimation(user, this, id, ANIM_CHARGE);
		}
		return TypedActionResult.consume(itemStack);
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.NONE;
	}
	
	@Override
	public void onPrimaryFire(World world, PlayerEntity user)
	{
		if(!world.isClient)
		{
			ServerHitscanHandler.performHitscan(user, (byte)0, 4, false);
		}
	}
	
	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks)
	{
		if(remainingUseTicks <= 0)
		{
			if(user instanceof PlayerEntity player)
			{
				if(world.isClient)
				{
					approxCooldown = 50;
					approxUseTime = 0;
				}
				else
				{
					final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) world);
					GeckoLibNetwork.syncAnimation(player, this, id, ANIM_DISCHARGE);
				}
				player.getItemCooldownManager().set(this, 50);
			}
			if(!world.isClient)
			{
				ServerHitscanHandler.performHitscan(user, (byte)1, 10, true);
			}
		}
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
		data.addAnimationController(new AnimationController<>(this, "controller", 20, this::predicate));
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
		
		switch (state)
		{
			case ANIM_CHARGE -> {
				controller.markNeedsReload();
				float rotSpeed = 1f - (getMaxUseTime(null) - getApproxUseTime()) / (float)(getMaxUseTime(null));
				if(rotSpeed == 0f)
				{
					rotSpeed = MathHelper.lerp(0.05f, lastRotSpeed, 0f);
				}
				lastRotSpeed = rotSpeed;
				controller.setAnimationSpeed(rotSpeed);
				controller.setAnimation(new AnimationBuilder().addAnimation("Soaryn_chest_popup", false));
			}
			case ANIM_DISCHARGE -> {
				controller.setAnimationSpeed(1);
				controller.markNeedsReload();
				controller.setAnimation(new AnimationBuilder().addAnimation("Soaryn_chest_popup", false));
			}
		}
	}
}
