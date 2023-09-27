package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.FlamethrowerRenderer;
import absolutelyaya.ultracraft.components.IWingedPlayerComponent;
import absolutelyaya.ultracraft.entity.projectile.FlameProjectileEntity;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
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

public class FlamethrowerItem extends AbstractWeaponItem implements GeoItem
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	final RawAnimation AnimationStart = RawAnimation.begin().thenPlay("start");
	final RawAnimation AnimationStop = RawAnimation.begin().thenPlay("stop");
	final RawAnimation AnimationOverdrive = RawAnimation.begin().thenPlay("start-overdrive");
	
	public FlamethrowerItem(Settings settings, float recoil, float altRecoil)
	{
		super(settings, recoil, altRecoil);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return new Vector2i(3, 1);
	}
	
	@Override
	String getControllerName()
	{
		return "flamethrower";
	}
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		if(!(user instanceof WingedPlayerEntity winged))
			return false;
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(winged).getGunCooldownManager();
		if(!cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return false;
		ItemStack stack = user.getMainHandStack();
		if(!stack.isOf(this))
			return false;
		int heat = getNbt(stack, "heat");
		boolean overdrive = heat > 200;
		cdm.setCooldown(this, overdrive ? 2 : 4, GunCooldownManager.PRIMARY);
		if(!world.isClient)
		{
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(), overdrive ? "overdrive" : "start");
			for (int i = 0; i < (overdrive ? 6 : 3); i++)
			{
				FlameProjectileEntity fireball = FlameProjectileEntity.spawn(user, world);
				fireball.setPosition(user.getBoundingBox().getCenter());
				Vec3d dir = user.getRotationVector();
				fireball.setVelocity(dir.x, dir.y, dir.z, (overdrive ? 1.25f : 0.75f) + user.getRandom().nextFloat() * 0.5f, overdrive ? 30 : 15);
				world.spawnEntity(fireball);
			}
			world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.75f,
					(overdrive ? 1f : 0.8f) + user.getRandom().nextFloat() * 0.4f);
			if(heat > 300)
			{
				cdm.setCooldown(this, 600, GunCooldownManager.PRIMARY);
				world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1f,
						0.25f + user.getRandom().nextFloat() * 0.1f);
			}
			setNbt(stack, "heat", heat + 4);
		}
		if(heat > 300)
		{
			for (int i = 0; i < 16; i++)
			{
				Vec3d pos = user.getEyePos();
				Vec3d vel = Vec3d.ZERO.addRandom(user.getRandom(), 1f);
				world.addParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
			}
			user.sendMessage(Text.translatable("message.ultracraft.flamethrower.overheat"), true);
		}
		return super.onPrimaryFire(world, user, userVelocity);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		int heat = getNbt(stack, "heat");
		IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(entity);
		if(heat > 0 && entity.age % 2 == 0 && (!winged.isPrimaryFiring() || winged.getGunCooldownManager().getCooldown(this, GunCooldownManager.PRIMARY) > 5))
			setNbt(stack, "heat", heat - 1);
	}
	
	@Override
	public void onPrimaryFireStop(World world, PlayerEntity user)
	{
		if(!(user instanceof WingedPlayerEntity))
			return;
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		if(cdm.getCooldown(this, GunCooldownManager.PRIMARY) < 5)
			cdm.setCooldown(this, getNbt(user.getMainHandStack(), "heat") > 200 ? 25 : 50, GunCooldownManager.PRIMARY);
		if(!world.isClient)
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(), "stop");
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private FlamethrowerRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new FlamethrowerRenderer();
				
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
										.triggerableAnim("start", AnimationStart)
										.triggerableAnim("stop", AnimationStop)
										.triggerableAnim("overdrive", AnimationOverdrive));
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
	public String getTopOverlayString(ItemStack stack)
	{
		int heat = getNbt(stack, "heat");
		if(heat == 0)
			return "";
		String c = heat <= 200 ? "§6" : "§4";
		return c + heat + "°";
	}
	
	@Override
	public int getItemBarColor(ItemStack stack)
	{
		int heat = getNbt(stack, "heat");
		int c = ((255 - Math.max(heat - 200, 0)) << 8) + Math.round(Math.max(1f - heat / 200f, 0.1f) * 255);
		c = (c << 8);
		return c;
	}
	
	@Override
	public int getNbtDefault(String nbt)
	{
		return 0;
	}
}
