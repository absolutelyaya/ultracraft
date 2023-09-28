package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.components.IWingedPlayerComponent;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.List;

public class MagnetEntity extends PersistentProjectileEntity implements GeoEntity, IIgnoreSharpshooter
{
	protected static final TrackedData<Float> GROUND_TIME = DataTracker.registerData(MagnetEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Float> IMPACT_YAW = DataTracker.registerData(MagnetEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Float> IMPACT_PITCH = DataTracker.registerData(MagnetEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Float> STRAIN = DataTracker.registerData(MagnetEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Integer> NAILS = DataTracker.registerData(MagnetEntity.class, TrackedDataHandlerRegistry.INTEGER);
	
	protected final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	
	LivingEntity victim;
	int unmovingTicks;
	float flash, flashTimer;
	
	public MagnetEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(GROUND_TIME, 0f);
		dataTracker.startTracking(IMPACT_YAW, 0f);
		dataTracker.startTracking(IMPACT_PITCH, 0f);
		dataTracker.startTracking(STRAIN, 0f);
		dataTracker.startTracking(NAILS, 0);
	}
	
	public static MagnetEntity spawn(LivingEntity owner, Vec3d pos, Vec3d vel)
	{
		MagnetEntity magnet = new MagnetEntity(EntityRegistry.MAGNET, owner.getWorld());
		magnet.setOwner(owner);
		magnet.setPosition(pos);
		magnet.setVelocity(vel);
		return magnet;
	}
	
	@Override
	protected ItemStack asItemStack()
	{
		return ItemRegistry.HARPOON.getDefaultStack();
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(dataTracker.get(GROUND_TIME) > 240)
			despawn();
		float groundTime = dataTracker.get(GROUND_TIME);
		flashTimer += 0.05f * Math.max(groundTime / 50f + getStrain(), 1f);
		if(groundTime > 0 && flashTimer > 0.5f)
		{
			flash = 1f;
			flashTimer = 0f;
			playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), 0.5f, 1.85f);
		}
		if(getVelocity().equals(Vec3d.ZERO) && !inGround && victim == null)
			unmovingTicks++;
		else if(unmovingTicks > 0)
			unmovingTicks = 0;
		if(unmovingTicks > 20)
			despawn();
		if(victim != null)
		{
			if(!victim.isAlive())
			{
				victim = null;
				return;
			}
			setVelocity(Vec3d.ZERO);
			setPosition(victim.getPos().add(0f, victim.getHeight() / 2, 0f));
			setYaw(dataTracker.get(IMPACT_YAW));
			setPitch(dataTracker.get(IMPACT_PITCH));
		}
		if(isRemoved() || !isInGround())
			return;
		List<NailEntity> nails = getWorld().getEntitiesByType(TypeFilter.instanceOf(NailEntity.class), getBoundingBox().expand(8), n -> true);
		List<MagnetEntity> magnets = getWorld().getEntitiesByType(TypeFilter.instanceOf(MagnetEntity.class), getBoundingBox().expand(8), MagnetEntity::isInGround);
		Vec3d pos = getPos();
		for (MagnetEntity magnet : magnets)
			pos = pos.add(magnet.getPos());
		pos = pos.multiply(1f / (magnets.size() + 1));
		for (NailEntity nail : nails)
			nail.setVelocity(nail.getVelocity().lerp(pos.add(0, 1, 0).subtract(nail.getPos()).normalize(),
					Math.max(1f - nail.distanceTo(this) / 6f, 0)));
		if(inGround || victim != null)
		{
			float strain = nails.size() / 42f / Math.max(magnets.size(), 1f);
			dataTracker.set(GROUND_TIME, dataTracker.get(GROUND_TIME) + 1f + strain);
			dataTracker.set(STRAIN, strain);
		}
	}
	
	void despawn()
	{
		getWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
		if(!getWorld().isClient())
		{
			List<MagnetEntity> magnets = getWorld().getEntitiesByType(TypeFilter.instanceOf(MagnetEntity.class), getBoundingBox().expand(8), n -> n != this);
			if(magnets.size() == 0)
				getWorld().getEntitiesByType(TypeFilter.instanceOf(NailEntity.class), getBoundingBox().expand(8), n -> true)
					 .forEach(n -> n.setVelocity(Vec3d.ZERO.addRandom(random, 1f).normalize().multiply((float)n.getVelocity().length())));
			if(getOwner() instanceof PlayerEntity player)
			{
				IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(player);
				winged.setMagnets(Math.max(winged.getMagnets() - 1, 0));
				GunCooldownManager gcdm = winged.getGunCooldownManager();
				if(gcdm.isUsable(ItemRegistry.ATTRACTOR_NAILGUN, GunCooldownManager.SECONDARY))
					gcdm.setCooldown(ItemRegistry.ATTRACTOR_NAILGUN, 10, GunCooldownManager.SECONDARY);
			}
			discard();
		}
	}
	
	@Override
	protected SoundEvent getHitSound()
	{
		return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		if(victim != null)
			return;
		if(entityHitResult.getEntity() instanceof LivingEntity living)
		{
			victim = living;
			dataTracker.set(IMPACT_YAW, getYaw());
			dataTracker.set(IMPACT_PITCH, getPitch());
			living.damage(DamageSources.get(getWorld(), DamageSources.MAGNET, this, getOwner()), 3.5f);
		}
	}
	
	@Override
	public void onPlayerCollision(PlayerEntity player) {}
	
	public boolean isInGround()
	{
		return inGround || victim != null;
	}
	
	public LivingEntity getVictim()
	{
		return victim;
	}
	
	public float getStrain()
	{
		return dataTracker.get(STRAIN);
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
	
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	public float getFlash()
	{
		return flash;
	}
	
	public void setFlash(float f)
	{
		flash = f;
	}
	
	@Override
	public void handleStatus(byte status) {
		if (status != EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES)
			return;
		for (int i = 0; i < 16; i++)
		{
			Vec3d pos = getPos().addRandom(random, 0.1f);
			Vec3d vel = Vec3d.ZERO.addRandom(random, 0.25f);
			getWorld().addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, asItemStack()), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
		}
		//TODO: break sound
	}
}
