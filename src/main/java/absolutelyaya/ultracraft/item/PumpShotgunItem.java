package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.PumpShotgunRenderer;
import absolutelyaya.ultracraft.damage.DamageSources;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PumpShotgunItem extends AbstractShotgunItem
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	final RawAnimation AnimationShot = RawAnimation.begin().thenPlay("shot_pump");
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
		GunCooldownManager cdm = ((WingedPlayerEntity)user).getGunCooldownManager();
		if(!cdm.isUsable(this, GunCooldownManager.SECONDARY))
			return;
		user.setCurrentHand(hand);
		if(!stack.hasNbt())
			stack.getOrCreateNbt();
		if(!world.isClient)
		{
			int charge = 0;
			if(stack.getNbt().contains("charge", NbtElement.INT_TYPE))
				charge = stack.getNbt().getInt("charge");
			stack.getNbt().putInt("charge", Math.min(charge + 1, 3));
			triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), b ? "pump" : "pump2");
			b = !b;
		}
		else
		{
			int charge = 0;
			if(stack.getNbt().contains("charge", NbtElement.INT_TYPE))
				charge = stack.getNbt().getInt("charge");
			user.playSound(SoundEvents.BLOCK_PISTON_CONTRACT, 0.5f, 0.8f + 0.1f * Math.min(charge + 1, 3));
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
		GunCooldownManager cdm = ((WingedPlayerEntity)user).getGunCooldownManager();
		if(!b)
			return false;
		cdm.setCooldown(this, 30, GunCooldownManager.PRIMARY);
		if(itemStack.hasNbt() && itemStack.getNbt().contains("charge"))
			itemStack.getNbt().putInt("charge", 0);
		if(overcharge && !world.isClient)
		{
			((WingedPlayerEntity)user).blockBloodHeal(10);
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
		else if(stack.hasNbt() && stack.getNbt().getInt("charge") == 3 && entity.age % 6 == 4)
			entity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), 0.3f, 0.78f);
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
										.triggerableAnim("shot_pump", AnimationShot)
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
		if(!stack.hasNbt() || !stack.getNbt().contains("charge"))
			return 10;
		int charge = stack.getNbt().getInt("charge");
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
}
