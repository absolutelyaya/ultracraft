package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.components.player.IWingedPlayerComponent;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.InstancedAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;

import java.util.List;

public class MagnetEntity extends AbstractSkewerEntity implements GeoEntity, IIgnoreSharpshooter
{
	protected static final TrackedData<Float> STRAIN = DataTracker.registerData(MagnetEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected static final TrackedData<Integer> NAILS = DataTracker.registerData(MagnetEntity.class, TrackedDataHandlerRegistry.INTEGER);
	
	protected final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	
	float flash, flashTimer;
	
	public MagnetEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
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
			playSound(SoundRegistry.NAILGUN_MAGNET_BEEP, 0.5f, 1.85f);
		}
		if(isRemoved() || (!isInGround() && victim == null))
			return;
		List<NailEntity> nails = getWorld().getEntitiesByType(TypeFilter.instanceOf(NailEntity.class), getBoundingBox().expand(8), n -> true);
		List<MagnetEntity> magnets = getWorld().getEntitiesByType(TypeFilter.instanceOf(MagnetEntity.class), getBoundingBox().expand(8),
				m -> m.isInGround() || m.victim != null);
		Vec3d pos = getPos();
		for (MagnetEntity magnet : magnets)
			pos = pos.add(magnet.getPos());
		pos = pos.multiply(1f / (magnets.size() + 1));
		for (NailEntity nail : nails)
			nail.setVelocity(nail.getVelocity().lerp(pos.add(0, 1, 0).subtract(nail.getPos()).normalize(),
					Math.max(1f - nail.distanceTo(this) / 6f, 0)));
		if(isInGround() || victim != null)
		{
			float strain = nails.size() / 42f / Math.max(magnets.size(), 1f);
			dataTracker.set(GROUND_TIME, groundTime + strain);
			dataTracker.set(STRAIN, strain);
		}
		if(getWorld().isThundering() && age % 40 == 0 && random.nextFloat() < 0.01f + nails.size() * 0.01f)
		{
			LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, getWorld());
			lightning.setPosition(getPos());
			getWorld().spawnEntity(lightning);
		}
	}
	
	protected void despawn()
	{
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
		}
		super.despawn();
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		if(victim != null)
			return;
		if(entityHitResult.getEntity() instanceof LivingEntity living)
			living.damage(DamageSources.get(getWorld(), DamageSources.MAGNET, this, getOwner()), 3.5f);
		super.onEntityHit(entityHitResult);
	}
	
	@Override
	public void onPlayerCollision(PlayerEntity player) {}
	
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
	public boolean damage(DamageSource source, float amount)
	{
		if(isInGround() && source.isIn(DamageTypeTags.BREAK_MAGNET))
			despawn();
		return super.damage(source, amount);
	}
	
	@Override
	void onPunchBroken()
	{
		despawn();
	}
}
