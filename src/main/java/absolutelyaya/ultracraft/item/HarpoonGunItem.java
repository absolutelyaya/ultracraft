package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.api.HeavyEntities;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.HarpoonGunRenderer;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.projectile.HarpoonEntity;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HarpoonGunItem extends AbstractWeaponItem implements GeoItem
{
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	final RawAnimation AnimationFire = RawAnimation.begin().thenPlay("fire");
	final RawAnimation AnimationAltFire = RawAnimation.begin().thenPlay("alt_fire");
	
	public int texture;
	
	public HarpoonGunItem(Settings settings, float recoil, float altRecoil)
	{
		super(settings, recoil, altRecoil);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if(world.getTime() % 5 == 0 && world.random.nextFloat() < (texture == 1 ? 0.33 : 0.01))
			texture = world.random.nextInt(4);
	}
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		if(!cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return false;
		ItemStack ammoStack = null;
		for (ItemStack s : user.getInventory().main)
		{
			if(s.isOf(ItemRegistry.HARPOON))
			{
				ammoStack = s;
				break;
			}
		}
		if(ammoStack == null && !user.isCreative())
			return false;
		if(!world.isClient)
		{
			HarpoonEntity harpoon = HarpoonEntity.spawn(user, user.getEyePos(), user.getRotationVector().multiply(3f));
			if(!user.isCreative())
			{
				harpoon.setStack(ammoStack.copy());
				ammoStack.decrement(1);
			}
			else
				harpoon.setStack(ItemRegistry.HARPOON.getDefaultStack());
			world.spawnEntity(harpoon);
			cdm.setCooldown(this, 15, GunCooldownManager.PRIMARY);
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(), "fire");
		}
		user.playSound(SoundRegistry.REPULSIVE_SKEWER_SHOOT, 1f, 1f);
		return super.onPrimaryFire(world, user, userVelocity);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		if(!UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager().isUsable(this, GunCooldownManager.SECONDARY))
			return TypedActionResult.fail(user.getStackInHand(hand));
		onAltFire(world, user);
		return TypedActionResult.consume(user.getStackInHand(hand));
	}
	
	@Override
	public void onAltFire(World world, PlayerEntity user)
	{
		List<HarpoonEntity> harpoons;
		harpoons = world.getEntitiesByType(TypeFilter.instanceOf(HarpoonEntity.class), user.getBoundingBox().expand(64f),
				i -> i.getOwner() != null && i.getOwner().equals(user));
		if(harpoons.size() == 0)
			return;
		Vec3d ownerVelocity = Vec3d.ZERO;
		for (HarpoonEntity harpoon : harpoons)
		{
			boolean hasVictim = harpoon.getVictim() != null;
			if(harpoon.isInGround() || (hasVictim && HeavyEntities.isHeavy(harpoon.getVictim().getType())))
				ownerVelocity = ownerVelocity.add(harpoon.getPos().subtract(user.getPos()).normalize());
			else if(hasVictim)
			{
				harpoon.getVictim().addVelocity(user.getPos().subtract(harpoon.getPos()).normalize().add(0f, 0.2f, 0f).multiply(2f));
				harpoon.getVictim().damage(DamageSources.get(world, DamageSources.HARPOON_RIP), 2f);
			}
			harpoon.setReturning(true);
		}
		user.setVelocity(ownerVelocity.normalize().multiply(Math.min(harpoons.size(), 3)));
		UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager().setCooldown(this, 100, GunCooldownManager.SECONDARY);
		if(!world.isClient)
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(), "altFire");
		user.playSound(SoundRegistry.REPULSIVE_SKEWER_REEL, 1f, 1f);
		super.onAltFire(world, user);
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return new Vector2i(3, 2);
	}
	
	@Override
	String getControllerName()
	{
		return "harpoon";
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private HarpoonGunRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new HarpoonGunRenderer();
				
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
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, getControllerName(), 0, ignored -> PlayState.STOP)
										.triggerableAnim("fire", AnimationFire)
										.triggerableAnim("altFire", AnimationAltFire));
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public boolean hasVariantBG()
	{
		return false;
	}
	
	@Override
	Item[] getVariants()
	{
		return new Item[0];
	}
	
	@Override
	int getSwitchCooldown()
	{
		return 0;
	}
	
	
	@Override
	public int getItemBarStep(ItemStack stack)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		if(!cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return (int)(cdm.getCooldownPercent(getCooldownClass(stack), GunCooldownManager.PRIMARY) * 14);
		else
			return (int)((1f - cdm.getCooldownPercent(getCooldownClass(stack), GunCooldownManager.SECONDARY)) * 14);
	}
	
	@Override
	public int getItemBarColor(ItemStack stack)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		if(cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return 0xdfb728;
		return 0xdc8f00;
	}
	
	@Override
	public boolean isItemBarVisible(ItemStack stack)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		return super.isItemBarVisible(stack) || !cdm.isUsable(this, GunCooldownManager.SECONDARY);
	}
}
