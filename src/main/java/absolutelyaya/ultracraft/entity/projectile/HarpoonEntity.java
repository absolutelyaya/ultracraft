package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.demon.HideousMassEntity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import absolutelyaya.ultracraft.registry.StatusEffectRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class HarpoonEntity extends AbstractSkewerEntity implements IIgnoreSharpshooter
{
	protected static final TrackedData<Vector3f> START_POSITION = DataTracker.registerData(HarpoonEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	protected static final TrackedData<Boolean> RETURNING = DataTracker.registerData(HarpoonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<ItemStack> STACK = DataTracker.registerData(HarpoonEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	
	public HarpoonEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(START_POSITION, new Vector3f());
		dataTracker.startTracking(RETURNING, false);
		dataTracker.startTracking(STACK, ItemStack.EMPTY);
	}
	
	public static HarpoonEntity spawn(LivingEntity owner, Vec3d pos, Vec3d vel)
	{
		HarpoonEntity harpoon = new HarpoonEntity(EntityRegistry.HARPOON, owner.getWorld());
		harpoon.setOwner(owner);
		harpoon.setPosition(pos);
		harpoon.dataTracker.set(START_POSITION, pos.toVector3f());
		harpoon.setVelocity(vel);
		return harpoon;
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
		if(((getOwner() != null && !getOwner().isPlayer()) || getOwner() == null) && age > 600)
			despawn();
		if(getOwner() != null && getOwner().isPlayer() && distanceTo(getOwner()) > 32)
		{
			getOwner().playSound(SoundRegistry.SKEWER_DISOWN, 1, 1);
			if(getWorld().isClient)
				UltracraftClient.HITSCAN_HANDLER.removeMoving(getUuid());
			owner = null;
		}
		if(victim == null && dataTracker.get(GROUND_TIME) > 20 && getOwner() != null && !getOwner().isPlayer())
			setReturning(true);
		else if(victim != null && dataTracker.get(GROUND_TIME) > 200 && getOwner() != null)
			setReturning(true);
		if(dataTracker.get(RETURNING))
		{
			if(!isRemoved() && getOwner() == null)
			{
				discard();
				return;
			}
			Vec3d start = new Vec3d(getStartPosition());
			Vec3d dir = getPos().subtract(start).normalize();
			setPosition(getPos().add(dir.multiply(-Math.min(1.6f, getPos().distanceTo(start)))));
			if(start.distanceTo(getPos()) < 0.1f)
			{
				if(getOwner() != null && getOwner().isPlayer() && !((PlayerEntity)getOwner()).isCreative())
					((PlayerEntity)getOwner()).giveItemStack(dataTracker.get(STACK));
				despawn();
			}
		}
		if(victim != null && !dataTracker.get(RETURNING))
		{
			if(!victim.isAlive())
			{
				victim = null;
				setReturning(true);
				return;
			}
			victim.addStatusEffect(new StatusEffectInstance(StatusEffectRegistry.IMPALED, 10, 1), this);
		}
	}
	
	@Override
	public void setOwner(@Nullable Entity entity)
	{
		owner = entity;
		if(entity != null)
			pickupType = entity instanceof PlayerEntity player && !player.isCreative() ? PickupPermission.ALLOWED : PickupPermission.CREATIVE_ONLY;
	}
	
	protected void despawn()
	{
		getWorld().addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, asItemStack()),
				getX(), getY(), getZ(), 0f, 0f, 0f);
		if(!getWorld().isClient())
			discard();
	}
	
	@Override
	public Vec3d getLeashPos(float delta)
	{
		float f = MathHelper.RADIANS_PER_DEGREE;
		return getLerpedPos(delta).add(new Vec3d(0f, 0.2f, -1.5f).rotateX(getPitch() * f).rotateY(getYaw() * f));
	}
	
	public Vector3f getStartPosition()
	{
		if(getOwner() != null)
			return getOwner().getLeashPos(0f).toVector3f();
		return dataTracker.get(START_POSITION);
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		if(dataTracker.get(RETURNING) || victim != null)
			return;
		if(entityHitResult.getEntity() instanceof LivingEntity living)
			living.damage(DamageSources.get(getWorld(), DamageSources.HARPOON, this, getOwner()), 3.5f);
		super.onEntityHit(entityHitResult);
	}
	
	public void setReturning(boolean b)
	{
		if(victim != null)
			victim = null;
		dataTracker.set(RETURNING, b);
		if(b && !dataTracker.get(STACK).isEmpty() && getOwner() instanceof LivingEntity living)
		{
			ItemStack stack = dataTracker.get(STACK);
			stack.damage(25, living, e -> despawn());
			if(stack.getDamage() == stack.getMaxDamage())
				despawn();
		}
	}
	
	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet)
	{
		super.onSpawnPacket(packet);
		if(getOwner() != null)
		{
			UltracraftClient.HITSCAN_HANDLER.addConnector(delta -> {
						if(getOwner() == null)
						{
							UltracraftClient.HITSCAN_HANDLER.removeMoving(getUuid());
							return getLeashPos(delta);
						}
						return getOwner().getLeashPos(delta);
					}, this::getLeashPos, getUuid(),
					new Vec2f(0.01f, 0.05f), 0.1f, 0x000000, 1);
		}
	}
	
	@Override
	public void remove(RemovalReason reason)
	{
		if(getOwner() instanceof HideousMassEntity mass)
			mass.setHasHarpoon(true);
		super.remove(reason);
	}
	
	@Override
	public void onRemoved()
	{
		if(getOwner() != null)
			UltracraftClient.HITSCAN_HANDLER.removeMoving(getUuid());
		super.onRemoved();
	}
	
	@Override
	public boolean hasNoGravity()
	{
		return dataTracker.get(RETURNING);
	}
	
	public void setStack(ItemStack ammoStack)
	{
		dataTracker.set(STACK, ammoStack);
	}
}
