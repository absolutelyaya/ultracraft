package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.MeleeInterruptable;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.CoreEjectShotgunRenderer;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import absolutelyaya.ultracraft.entity.projectile.EjectedCoreEntity;
import absolutelyaya.ultracraft.entity.projectile.ShotgunPelletEntity;
import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
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
	final RawAnimation AnimationShot = RawAnimation.begin().thenPlay("shot");
	final RawAnimation AnimationAltShot = RawAnimation.begin().thenPlay("altShot");
	
	public CoreEjectShotgunItem(Settings settings)
	{
		super(settings, 45f);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)user).getGunCooldownManager();
		Vec3d dir = new Vec3d(0f, 0f, 1f);
		dir = dir.rotateX((float)Math.toRadians(-user.getPitch()));
		dir = dir.rotateY((float)Math.toRadians(-user.getHeadYaw()));
		if(!cdm.isUsable(this, 0) || user.getItemCooldownManager().isCoolingDown(this))
			return false;
		super.onPrimaryFire(world, user, userVelocity);
		if(!world.isClient)
		{
			boolean parry = false, trueParry = false;
			HitResult hit = ProjectileUtil.raycast(user, user.getEyePos(), user.getEyePos().add(user.getRotationVector().multiply(1.5f)),
					user.getBoundingBox().expand(3f),
					e -> e instanceof MaliciousFaceEntity || (e instanceof MeleeInterruptable mp && (!(mp instanceof MobEntity me) || me.isAttacking())),
					1.5 * 1.5);
			if(hit instanceof EntityHitResult eHit)
			{
				if(eHit.getEntity() instanceof MeleeInterruptable mp && mp instanceof MobEntity me && me.isAttacking())
				{
					mp.onInterrupt(user);
					trueParry = true;
				}
				parry = true;
			}
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), controllerName, "shot");
			cdm.setCooldown(this, 35, GunCooldownManager.PRIMARY);
			for (int i = 0; i < 12; i++)
			{
				//guarantees that the first bullet goes straight and only that one is actually boostable (if this isn't a shotgun parry)
				ShotgunPelletEntity bullet = ShotgunPelletEntity.spawn(user, world, i == 0 && !parry);
				bullet.setVelocity(dir.x, dir.y, dir.z, i == 0 ? 1f : 1.5f, i == 0 && !parry ? 1f : 15f);
				if(parry && i == 0)
					bullet.increaseDamage(2f);
				bullet.addVelocity(userVelocity);
				bullet.setNoGravity(true);
				world.spawnEntity(bullet);
			}
			world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS,
					1.0f, 0.2f / (user.getRandom().nextFloat() * 0.2f + 0.6f));
			if(parry)
				world.playSound(null, ((EntityHitResult)hit).getEntity().getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS,
						0.75f, 0.3f / (user.getRandom().nextFloat() * 0.2f + 0.6f));
			if(trueParry)
				Ultracraft.freeze((ServerWorld)world, 5);
		}
		if(world.isClient)
		{
			Vec3d eyePos = user.getEyePos();
			Random rand = user.getRandom();
			for (int i = 0; i < 12; i++)
			{
				world.addParticle(ParticleTypes.SMOKE, eyePos.x, eyePos.y - 0.2, eyePos.z,
						dir.x * 0.5 + (rand.nextFloat() - 0.5f) * 0.2,
						dir.y * 0.5 + (rand.nextFloat() - 0.5f) * 0.2,
						dir.z * 0.5 + (rand.nextFloat() - 0.5f) * 0.2);
			}
			
			Vec3d pos = eyePos.add(
					dir.multiply(0.2f).add(new Vec3d(-0.3f * (user.getMainArm().equals(Arm.LEFT) ? -1.5 : 1), -0.3f, 0.4f)
												   .rotateY(-(float)Math.toRadians(user.getYaw()))));
			world.addParticle(new ParryIndicatorParticleEffect(false), pos.x, pos.y, pos.z, 0f, 0f, 0f);
		}
		return true;
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)user).getGunCooldownManager();
		ItemStack itemStack = user.getStackInHand(hand);
		if(hand.equals(Hand.OFF_HAND))
			return TypedActionResult.fail(itemStack);
		if(!cdm.isUsable(this, 0))
			return TypedActionResult.fail(itemStack);
		user.setCurrentHand(hand);
		if(!world.isClient)
			itemStack.getOrCreateNbt().putBoolean("charging", true);
		return TypedActionResult.consume(itemStack);
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
			triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerWorld)world), controllerName, "altShot");
		}
		if(nbt != null)
			nbt.remove("charging");
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
		controllerRegistrar.add(new AnimationController<>(this, controllerName, 1, state -> PlayState.STOP)
										.triggerableAnim("shot", AnimationShot)
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
	public Supplier<Object> getRenderProvider()
	{
		return renderProvider;
	}
}
