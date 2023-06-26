package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.SharpshooterRevolverRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
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
	
	public SharpshooterRevolverItem(Settings settings)
	{
		super(settings, 15f, 25f);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	public ItemStack getDefaultStack()
	{
		ItemStack stack = new ItemStack(this);
		setCharges(stack, 3);
		return stack;
	}
	
	public ItemStack getStackedSharpshooter()
	{
		ItemStack stack = getDefaultStack();
		setCharges(stack, 64);
		return stack;
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(hand.equals(Hand.OFF_HAND) || getCharges(itemStack) == 0)
			return TypedActionResult.fail(itemStack);
		user.setCurrentHand(hand);
		itemStack.getOrCreateNbt().putBoolean("charging", true);
		return TypedActionResult.pass(itemStack);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		if(stack.hasNbt() && stack.getNbt().contains("charging"))
		{
			if(!selected)
			{
				stack.getNbt().remove("charging");
				if(!world.isClient)
					triggerAnim(entity, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "stop");
				return;
			}
			if(!world.isClient && entity instanceof PlayerEntity player)
				triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "spin");
		}
		if(!(entity instanceof PlayerEntity player))
			return;
		GunCooldownManager cdm = ((WingedPlayerEntity)player).getGunCooldownManager();
		super.inventoryTick(stack, world, entity, slot, selected);
		int charges = getCharges(stack);
		if(charges < 3 && cdm.isUsable(this, GunCooldownManager.SECONDARY))
		{
			setCharges(stack, charges + 1);
			cdm.setCooldown(this, 200, GunCooldownManager.SECONDARY);
			player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.1f, 1.5f);
		}
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
		GunCooldownManager cdm = ((WingedPlayerEntity)user).getGunCooldownManager();
		int charges = getCharges(stack);
		if(remainingUseTicks <= 0)
		{
			if(user instanceof PlayerEntity player)
			{
				if(!world.isClient)
				{
					if(charges == 3)
						cdm.setCooldown(this, 200, GunCooldownManager.SECONDARY);
					setCharges(stack, charges - 1);
					triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "discharge");
					world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS, 1f,
							0.85f + (user.getRandom().nextFloat() - 0.5f) * 0.2f);
				}
				player.getItemCooldownManager().set(this, 10);
				onAltFire(world, player);
			}
			if(!world.isClient)
				ServerHitscanHandler.performBouncingHitscan(user, ServerHitscanHandler.SHARPSHOOTER, 3, Integer.MAX_VALUE,
						(int)Math.ceil(Math.min(Math.abs(remainingUseTicks) / 20f, 1f) * 3));
		}
		else if(!world.isClient && user instanceof PlayerEntity)
			triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "stop");
		if(stack.hasNbt() && stack.getNbt().contains("charging"))
			stack.getNbt().remove("charging");
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
		GunCooldownManager cdm = ((WingedPlayerEntity) MinecraftClient.getInstance().player).getGunCooldownManager();
		return !cdm.isUsable(stack.getItem(), GunCooldownManager.PRIMARY) || getCharges(stack) < 3;
	}
	
	@Override
	public int getItemBarStep(ItemStack stack)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)MinecraftClient.getInstance().player).getGunCooldownManager();
		if(!cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return (int)(cdm.getCooldownPercent(stack.getItem(), GunCooldownManager.PRIMARY) * 14);
		else
			return (int)((1f - cdm.getCooldownPercent(stack.getItem(), GunCooldownManager.SECONDARY)) * 14);
	}
	
	@Override
	public int getItemBarColor(ItemStack stack)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)MinecraftClient.getInstance().player).getGunCooldownManager();
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
	public String getCountString(ItemStack stack)
	{
		if(stack.hasNbt() && stack.getNbt().contains("charges"))
			return Formatting.GOLD + String.valueOf(getCharges(stack));
		return null;
	}
	
	public int getCharges(ItemStack stack)
	{
		if(!stack.hasNbt() || !stack.getNbt().contains("charges", NbtElement.INT_TYPE))
			stack.getOrCreateNbt().putInt("charges", 3);
		return stack.getNbt().getInt("charges");
	}
	
	public void setCharges(ItemStack stack, int i)
	{
		stack.getOrCreateNbt().putInt("charges", i);
	}
}
