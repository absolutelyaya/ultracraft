package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.MeleeInterruptable;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import absolutelyaya.ultracraft.entity.projectile.ShotgunPelletEntity;
import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.core.animation.RawAnimation;

public abstract class AbstractShotgunItem extends AbstractWeaponItem implements GeoItem
{
	boolean b; //toggled on every shot; decides purely which shot animation should be used to allow for rapid firing
	public AbstractShotgunItem(Settings settings, float recoil, float altRecoil)
	{
		super(settings, recoil, altRecoil);
	}
	protected final RawAnimation AnimationSwitch = RawAnimation.begin().thenPlay("switch");
	protected final RawAnimation AnimationSwitch2 = RawAnimation.begin().thenPlay("switch2");
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		Vec3d dir = new Vec3d(0f, 0f, 1f);
		dir = dir.rotateX((float)Math.toRadians(-user.getPitch()));
		dir = dir.rotateY((float)Math.toRadians(-user.getHeadYaw()));
		if(!isCanFirePrimary(user) || user.getItemCooldownManager().isCoolingDown(this))
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
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(),
					getShotAnimationName() + (b ? "2" : ""));
			cdm.setCooldown(this, getPrimaryCooldown(), GunCooldownManager.PRIMARY);
			b = !b;
			for (int i = 0; i < getPelletCount(user.getMainHandStack()); i++)
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
	Item[] getVariants()
	{
		return new Item[] {ItemRegistry.CORE_SHOTGUN, ItemRegistry.PUMP_SHOTGUN};
	}
	
	@Override
	int getSwitchCooldown()
	{
		return 8;
	}
	
	public String getShotAnimationName()
	{
		return "shot_core";
	}
	
	public int getPelletCount(ItemStack stack)
	{
		return 0;
	}
	
	public int getPrimaryCooldown()
	{
		return 27;
	}
	
	@Override
	protected void onSwitch(PlayerEntity user, World world)
	{
		if(!world.isClient)
		{
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(), "switch" + (b ? "2" : ""));
			b = !b;
		}
	}
}
