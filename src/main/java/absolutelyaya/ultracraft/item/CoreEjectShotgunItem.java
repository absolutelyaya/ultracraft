package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.CoreEjectShotgunRenderer;
import absolutelyaya.ultracraft.entity.projectile.ShotgunPelletEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

public class CoreEjectShotgunItem extends AbstractWeaponItem implements GeoItem
{
	private final String controllerName = "CoreEjectShotgun";
	protected int approxUseTime = -1;
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	RawAnimation AnimationShot = RawAnimation.begin().thenPlay("shot");
	
	public CoreEjectShotgunItem(Settings settings)
	{
		super(settings);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	//TODO: secondary fire
	
	@Override
	public void onPrimaryFire(World world, PlayerEntity user)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)user).getGunCooldownManager();
		if(!world.isClient && cdm.isUsable(this, 0))
		{
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), controllerName, "shot");
			cdm.setCooldown(this, 70, GunCooldownManager.PRIMARY);
			Vec3d dir = new Vec3d(0f, 0f, 1f);
			dir = dir.rotateX((float)Math.toRadians(-user.getPitch()));
			dir = dir.rotateY((float)Math.toRadians(-user.getHeadYaw()));
			for (int i = 0; i < 16; i++)
			{
				ShotgunPelletEntity bullet = ShotgunPelletEntity.spawn(user, world);
				bullet.setVelocity(dir.x, dir.y, dir.z, 1f, 20f);
				Vec3d vel = bullet.getVelocity();
				world.addParticle(ParticleTypes.SMOKE, bullet.getX(), bullet.getY(), bullet.getZ(), vel.x, vel.y, vel.z);
				bullet.setNoGravity(true);
				world.spawnEntity(bullet);
			}
			world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS,
					1.0f, 0.2f / (user.getRandom().nextFloat() * 0.2f + 0.6f));
		}
	}
	
	@Override
	public int getItemBarStep(ItemStack stack)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity) MinecraftClient.getInstance().player).getGunCooldownManager();
		return (int)((float)(70 - cdm.getCooldown(stack.getItem(), GunCooldownManager.PRIMARY)) / 70f * 14f);
	}
	
	@Override
	public boolean isItemBarVisible(ItemStack stack)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)MinecraftClient.getInstance().player).getGunCooldownManager();
		return cdm.getCooldown(stack.getItem(), GunCooldownManager.PRIMARY) > 0;
	}
	
	@Override
	public int getItemBarColor(ItemStack stack)
	{
		return 0x28ccdf;
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return new Vector2i(0, 1);
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, controllerName, 1, state -> PlayState.STOP)
										.triggerableAnim("shot", AnimationShot));
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
	public Supplier<Object> getRenderProvider()
	{
		return renderProvider;
	}
}
