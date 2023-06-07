package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.entity.projectile.ShotgunPelletEntity;
import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;

public abstract class AbstractShotgunItem extends AbstractWeaponItem implements GeoItem
{
	public AbstractShotgunItem(Settings settings, float recoil, float altRecoil)
	{
		super(settings, recoil, altRecoil);
	}
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)user).getGunCooldownManager();
		Vec3d dir = new Vec3d(0f, 0f, 1f);
		dir = dir.rotateX((float)Math.toRadians(-user.getPitch()));
		dir = dir.rotateY((float)Math.toRadians(-user.getHeadYaw()));
		if(!cdm.isUsable(this, 0) || user.getItemCooldownManager().isCoolingDown(this))
			return false;
		super.onPrimaryFire(world, user);
		if(!world.isClient)
		{
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(), "shot");
			cdm.setCooldown(this, 35, GunCooldownManager.PRIMARY);
			for (int i = 0; i < 12; i++)
			{
				ShotgunPelletEntity bullet = ShotgunPelletEntity.spawn(user, world);
				bullet.setVelocity(dir.x, dir.y, dir.z, 1.5f, 15f);
				bullet.setNoGravity(true);
				world.spawnEntity(bullet);
			}
			world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS,
					1.0f, 0.2f / (user.getRandom().nextFloat() * 0.2f + 0.6f));
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
			
			Vec3d pos = eyePos.add(dir.multiply(0.2f).add(new Vec3d(-0.3f, -0.3f, 0.4f).rotateY(-(float)Math.toRadians(user.getYaw()))));
			world.addParticle(new ParryIndicatorParticleEffect(false), pos.x, pos.y, pos.z, 0f, 0f, 0f);
		}
		return true;
	}
}
