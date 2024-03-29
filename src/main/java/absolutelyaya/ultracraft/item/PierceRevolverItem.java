package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.PierceRevolverRenderer;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.registry.SoundRegistry;
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
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.joml.Vector2i;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PierceRevolverItem extends AbstractRevolverItem
{
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	protected int approxUseTime = -1;
	
	public PierceRevolverItem(Settings settings)
	{
		super(settings, 15f, 25f);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(hand.equals(Hand.OFF_HAND))
			return TypedActionResult.fail(itemStack);
		user.setCurrentHand(hand);
		itemStack.getOrCreateNbt().putBoolean("charging", true);
		return TypedActionResult.pass(itemStack);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
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
				triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "charging");
		}
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return new Vector2i(0, 0);
	}
	
	@Override
	String getControllerName()
	{
		return "pierceRevolverController";
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
		if(remainingUseTicks <= 0)
		{
			if(user instanceof PlayerEntity player)
			{
				if(!world.isClient)
				{
					cdm.setCooldown(this, 50, GunCooldownManager.SECONDARY);
					triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "discharge");
					world.playSound(null, user.getBlockPos(), SoundRegistry.PIERCER_FIRE, SoundCategory.PLAYERS, 1f,
							0.85f + (user.getRandom().nextFloat() - 0.5f) * 0.2f);
				}
				player.getItemCooldownManager().set(this, 50);
				onAltFire(world, player);
			}
			if(!world.isClient)
				ServerHitscanHandler.performHitscan(user, ServerHitscanHandler.REVOLVER_PIERCE, 2, 3, true, DamageSources.PIERCER);
		}
		else if(!world.isClient && user instanceof PlayerEntity)
			triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "stop");
		if(stack.hasNbt() && stack.getNbt().contains("charging"))
			stack.getNbt().remove("charging");
		approxUseTime = -1;
	}
	
	@Override
	protected boolean isCanFirePrimary(PlayerEntity user)
	{
		ItemStack stack = user.getMainHandStack();
		if(stack.hasNbt() && stack.getNbt().contains("charging"))
			return false;
		return super.isCanFirePrimary(user);
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
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, getControllerName(), 1, state -> PlayState.STOP)
										.triggerableAnim("charging", AnimationCharge)
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
	
	public int getApproxUseTime()
	{
		return approxUseTime;
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
	
	@Override
	protected void onSwitch(PlayerEntity user, World world)
	{
		super.onSwitch(user, world);
		approxUseTime = -1;
	}
}
