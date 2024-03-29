package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.PumpShotgunRenderer;
import absolutelyaya.ultracraft.components.player.IWingedPlayerComponent;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PumpShotgunItem extends AbstractShotgunItem
{
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	final RawAnimation AnimationShot = RawAnimation.begin().thenPlay("shot_pump");
	final RawAnimation AnimationShot2 = RawAnimation.begin().thenPlay("shot_pump2");
	final RawAnimation AnimationPump = RawAnimation.begin().thenPlay("pump");
	final RawAnimation AnimationPump2 = RawAnimation.begin().thenPlay("pump2");
	boolean b; //toggled on every pump; decides purely which pump animation should be used to allow for rapid... pumping
	
	public PumpShotgunItem(Settings settings)
	{
		super(settings, 45f, 25f);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	String getControllerName()
	{
		return "PumpShotgun";
	}
	
	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks)
	{
		pump(world, user, stack, 12);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		user.setCurrentHand(hand);
		ItemStack itemStack = user.getStackInHand(hand);
		pump(world, user, itemStack, 7);
		return TypedActionResult.pass(itemStack);
	}
	
	void pump(World world, LivingEntity user, ItemStack stack, int cooldown)
	{
		Hand hand = user.getActiveHand();
		if(hand.equals(Hand.OFF_HAND))
			return;
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		if(!cdm.isUsable(this, GunCooldownManager.SECONDARY))
			return;
		user.setCurrentHand(hand);
		if(!stack.hasNbt())
			stack.getOrCreateNbt();
		if(!world.isClient)
		{
			setNbt(stack, "charge", Math.min(getNbt(stack, "charge") + 1, 3));
			triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), b ? "pump" : "pump2");
			b = !b;
		}
		else
		{
			int charge = getNbt(stack, "charge");
			user.playSound(SoundRegistry.SHOTGUN_PUMP, 0.5f, 0.8f + 0.1f * Math.min(charge + 1, 3));
		}
		cdm.setCooldown(this, cooldown, GunCooldownManager.SECONDARY);
	}
	
	@Override
	public boolean isUsedOnRelease(ItemStack stack)
	{
		return true;
	}
	
	@Override
	public int getMaxUseTime(ItemStack stack)
	{
		return Integer.MAX_VALUE;
	}
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		ItemStack itemStack = user.getMainHandStack();
		boolean overcharge = getPelletCount(itemStack) == 0;
		boolean b = super.onPrimaryFire(world, user, userVelocity);
		IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(user);
		if(!b)
			return false;
		setNbt(itemStack, "charge", 0);
		if(overcharge && !world.isClient)
		{
			winged.setBloodHealCooldown(10);
			ExplosionHandler.explosion(user, world, user.getPos().add(user.getRotationVector()),
					DamageSources.get(world, DamageSources.OVERCHARGE, user), 10, 0, 3, true, true);
			user.damage(DamageSources.get(world, DamageSources.OVERCHARGE_SELF), 4);
		}
		return true;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if(!selected && stack.hasNbt() && stack.getNbt().contains("charge"))
			stack.getNbt().remove("charge");
		else if(stack.hasNbt() && getNbt(stack, "charge") == 3 && entity.age % 6 == 4)
			entity.playSound(SoundRegistry.SHOTGUN_OVERPUMP_BEEP, 0.3f, 0.78f);
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return new Vector2i(1, 1);
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, getControllerName(), 1, state -> PlayState.STOP)
										.triggerableAnim("switch", AnimationSwitch)
										.triggerableAnim("switch2", AnimationSwitch2)
										.triggerableAnim("shot_pump", AnimationShot)
										.triggerableAnim("shot_pump2", AnimationShot2)
										.triggerableAnim("pump", AnimationPump)
										.triggerableAnim("pump2", AnimationPump2));
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
			private PumpShotgunRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new PumpShotgunRenderer();
				
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
	public String getShotAnimationName()
	{
		return "shot_pump";
	}
	
	@Override
	public int getPelletCount(ItemStack stack)
	{
		int charge = getNbt(stack, "charge");
		if(charge == 0)
			return 10;
		else if(charge == 1)
			return 15;
		else if(charge == 2)
			return 20;
		else
			return 0;
	}
	
	@Override
	public int getItemBarColor(ItemStack stack)
	{
		return 0x28df53;
	}
	
	public int getPrimaryCooldown()
	{
		return 16;
	}
	
	@Override
	protected int getNbtDefault(String nbt)
	{
		if(nbt.equals("charge"))
			return 0;
		return super.getNbtDefault(nbt);
	}
	
	@Override
	protected void onSwitch(PlayerEntity user, World world)
	{
		setNbt(user.getMainHandStack(), "charge", getNbtDefault("charge"));
		super.onSwitch(user, world);
	}
}
