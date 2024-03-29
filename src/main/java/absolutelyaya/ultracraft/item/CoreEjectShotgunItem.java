package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.CoreEjectShotgunRenderer;
import absolutelyaya.ultracraft.entity.projectile.EjectedCoreEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
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

public class CoreEjectShotgunItem extends AbstractShotgunItem
{
	protected int approxUseTime = -1;
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	final RawAnimation AnimationShot = RawAnimation.begin().thenPlay("shot_core");
	final RawAnimation AnimationShot2 = RawAnimation.begin().thenPlay("shot_core2");
	final RawAnimation AnimationAltShot = RawAnimation.begin().thenPlay("altShot");
	
	public CoreEjectShotgunItem(Settings settings)
	{
		super(settings, 45f, 25f);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	String getControllerName()
	{
		return "CoreEjectShotgun";
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(hand.equals(Hand.OFF_HAND))
			return TypedActionResult.fail(itemStack);
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		if(!cdm.isUsable(this, 0))
			return TypedActionResult.fail(itemStack);
		user.setCurrentHand(hand);
		if(!world.isClient)
			itemStack.getOrCreateNbt().putBoolean("charging", true);
		return TypedActionResult.pass(itemStack);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if(!selected && stack.hasNbt() && stack.getNbt().contains("charging"))
		{
			stack.getNbt().remove("charging");
			if(world.isClient)
				approxUseTime = -1;
		}
		if(world.isClient && stack.hasNbt() && stack.getNbt().contains("charging") &&
				   entity instanceof ClientPlayerEntity player && player.equals(MinecraftClient.getInstance().player))
			approxUseTime++;
	}
	
	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks)
	{
		if(remainingUseTicks == 999)
			return; //pretty much definitely a variant swap - don't core eject
		NbtCompound nbt = stack.getNbt();
		if(!world.isClient && user instanceof PlayerEntity player)
		{
			float useTime = 1f - MathHelper.clamp(Math.max(remainingUseTicks, 0) / 30f, 0f, 1f);
			player.getItemCooldownManager().set(this, 50);
			EjectedCoreEntity bullet = EjectedCoreEntity.spawn(user, world);
			Vec3d dir = new Vec3d(0f, 0f, 1f);
			dir = dir.rotateX((float)Math.toRadians(-user.getPitch()));
			dir = dir.rotateY((float)Math.toRadians(-user.getHeadYaw()));
			bullet.setVelocity(dir.x, dir.y + ((1f - useTime) * 0.5 + 0.05f), dir.z, Math.max(useTime * 1.25f, 0.25f), 0f);
			Vec3d vel = bullet.getVelocity();
			world.addParticle(ParticleTypes.SMOKE, bullet.getX(), bullet.getY(), bullet.getZ(), vel.x, vel.y, vel.z);
			bullet.setNoGravity(true);
			world.spawnEntity(bullet);
			triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerWorld)world), getControllerName(), "altShot");
			((LivingEntityAccessor)user).addRecoil(altRecoil * useTime);
		}
		if(nbt != null)
			nbt.remove("charging");
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
		return 30;
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return new Vector2i(0, 1);
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, getControllerName(), 1, state -> PlayState.STOP)
										.triggerableAnim("switch", AnimationSwitch)
										.triggerableAnim("switch2", AnimationSwitch2)
										.triggerableAnim("shot_core", AnimationShot)
										.triggerableAnim("shot_core2", AnimationShot2)
										.triggerableAnim("altShot", AnimationAltShot));
	}
	
	public int getApproxUseTime()
	{
		return approxUseTime;
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
			private CoreEjectShotgunRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new CoreEjectShotgunRenderer();
				
				return renderer;
			}
		});
	}
	
	@Override
	public int getPrimaryCooldown()
	{
		return 24;
	}
	
	@Override
	public Supplier<Object> getRenderProvider()
	{
		return renderProvider;
	}
	
	@Override
	public int getPelletCount(ItemStack stack)
	{
		return 12;
	}
	
	@Override
	protected void onSwitch(PlayerEntity user, World world)
	{
		super.onSwitch(user, world);
		approxUseTime = -1;
	}
}
