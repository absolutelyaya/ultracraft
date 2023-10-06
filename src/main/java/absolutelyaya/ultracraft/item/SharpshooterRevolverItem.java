package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.SharpshooterRevolverRenderer;
import absolutelyaya.ultracraft.damage.DamageSources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Vector2i;
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

public class SharpshooterRevolverItem extends AbstractRevolverItem
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	protected int approxUseTime = -1;
	
	public SharpshooterRevolverItem(Settings settings)
	{
		super(settings, 15f, 25f);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	public ItemStack getDefaultStack()
	{
		ItemStack stack = new ItemStack(this);
		setNbt(stack, "charges", 3);
		return stack;
	}
	
	public ItemStack getStackedSharpshooter()
	{
		ItemStack stack = getDefaultStack();
		setNbt(stack, "charges", 64);
		return stack;
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(hand.equals(Hand.OFF_HAND) || getNbt(itemStack, "charges") == 0)
			return TypedActionResult.fail(itemStack);
		user.setCurrentHand(hand);
		itemStack.getOrCreateNbt().putBoolean("charging", true);
		return TypedActionResult.pass(itemStack);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		if(world.isClient && approxUseTime > 5)
		{
			float f = Math.min((approxUseTime - 5) / 35f, 1f);
			float pitch = MathHelper.lerp(f, 0.1f, 1.4f);
			int frequency = MathHelper.lerp(f, 8, 3);
			if((approxUseTime - 2) % frequency == 0)
				entity.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, pitch);
		}
		if(stack.hasNbt() && stack.getNbt().contains("charging"))
		{
			if(!selected)
			{
				stack.getNbt().remove("charging");
				if(!world.isClient)
					triggerAnim(entity, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "stop");
				if(world.isClient && entity instanceof ClientPlayerEntity player && player.equals(MinecraftClient.getInstance().player))
					approxUseTime = -1;
				return;
			}
			if(world.isClient && entity instanceof ClientPlayerEntity player && player.equals(MinecraftClient.getInstance().player))
				approxUseTime++;
			else if(entity instanceof PlayerEntity player)
				triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "spin");
		}
		super.inventoryTick(stack, world, entity, slot, selected);
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return new Vector2i(2, 0);
	}
	
	@Override
	String getControllerName()
	{
		return "sharpshooterRevolverController";
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.NONE;
	}
	
	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		int charges = getNbt(stack, "charges");
		if(remainingUseTicks <= 0)
		{
			if(user instanceof PlayerEntity player)
			{
				if(!world.isClient)
				{
					if(charges == 3)
						cdm.setCooldown(this, 200, GunCooldownManager.TRITARY);
					setNbt(stack, "charges", charges - 1);
					triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "discharge");
					world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS, 1f,
							0.85f + (user.getRandom().nextFloat() - 0.5f) * 0.2f);
				}
				player.getItemCooldownManager().set(this, 10);
				onAltFire(world, player);
			}
			if(!world.isClient)
			{
				byte type = ServerHitscanHandler.SHARPSHOOTER;
				int bounces = (int)Math.ceil(Math.min(Math.abs(remainingUseTicks) / 20f, 1f) * 3), maxHits = Integer.MAX_VALUE;
				float autoAim = 45f;
				ServerHitscanHandler.performBouncingHitscan(user, type, 3, DamageSources.SHARPSHOOTER, maxHits,
						bounces, new ServerHitscanHandler.HitscanExplosionData(1.5f, 0f, 0f, true), autoAim);
			}
		}
		else if(!world.isClient && user instanceof PlayerEntity)
			triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "stop");
		if(stack.hasNbt() && stack.getNbt().contains("charging"))
			stack.getNbt().remove("charging");
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
		return 20;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, getControllerName(), 1, state -> PlayState.STOP)
										.triggerableAnim("spin", AnimationSpin)
										.triggerableAnim("discharge", AnimationDischarge)
										.triggerableAnim("shot", AnimationShot)
										.triggerableAnim("shot2", AnimationShot2) //this animation purely exists to cancel shot animations.
										.triggerableAnim("stop", AnimationStop));
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
			private SharpshooterRevolverRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new SharpshooterRevolverRenderer();
				
				return renderer;
			}
		});
	}
	
	@Override
	public boolean isItemBarVisible(ItemStack stack)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		return !cdm.isUsable(getCooldownClass(stack), GunCooldownManager.PRIMARY) || getNbt(stack, "charges") < 3;
	}
	
	@Override
	public int getItemBarStep(ItemStack stack)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		if(!cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return (int)(cdm.getCooldownPercent(getCooldownClass(stack), GunCooldownManager.PRIMARY) * 14);
		else
			return (int)((1f - cdm.getCooldownPercent(getCooldownClass(stack), GunCooldownManager.TRITARY)) * 14);
	}
	
	@Override
	public int getItemBarColor(ItemStack stack)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		if(cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return 0xdfb728;
		return 0xdf2828;
	}
	
	@Override
	public Supplier<Object> getRenderProvider()
	{
		return renderProvider;
	}
	
	@Override
	public String getTopOverlayString(ItemStack stack)
	{
		if(stack.hasNbt() && stack.getNbt().contains("charges"))
			return Formatting.GOLD + String.valueOf(getNbt(stack, "charges"));
		return null;
	}
}
