package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class HellBulletEntity extends ThrownItemEntity
{
	Class<? extends LivingEntity> ignore;
	private boolean shot;
	
	public HellBulletEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	protected HellBulletEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.HELL_BULLET, owner, world);
	}
	
	protected HellBulletEntity(EntityType<? extends HellBulletEntity> type, LivingEntity owner, World world)
	{
		super(type, owner, world);
	}
	
	public static HellBulletEntity spawn(LivingEntity owner, World world)
	{
		return new HellBulletEntity(owner, world);
	}
	
	@Override
	protected Item getDefaultItem()
	{
		return ItemRegistry.HELL_BULLET;
	}
	
	@Override
	public boolean hasNoGravity()
	{
		return true;
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		super.onEntityHit(entityHitResult);
		Entity entity = entityHitResult.getEntity();
		float amount = 6f;
		if(entity instanceof AbstractUltraHostileEntity)
			amount *= 0.25f;
		if(!entity.getClass().equals(ignore) && !((ProjectileEntityAccessor)this).isParried())
			entity.damage(getDamageSources().thrown(this, this.getOwner()), amount);
	}
	
	@Override
	public void setVelocity(Vec3d velocity)
	{
		if(getOwner() instanceof PlayerEntity)
			super.setVelocity(velocity);
		else
			super.setVelocity(velocity.multiply(1f + Math.max(world.getDifficulty().getId() - 1, 0) * 0.1f));
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		super.onCollision(hitResult);
		if(hitResult instanceof EntityHitResult hit && hit.getEntity().getClass().equals(ignore) && !((ProjectileEntityAccessor)this).isParried())
			return;
		if (!world.isClient && !isRemoved())
		{
			world.sendEntityStatus(this, (byte)3);
			discard();
		}
	}
	
	public void setIgnored(Class<? extends LivingEntity> ignore)
	{
		this.ignore = ignore;
	}
	
	@Override
	public void slowMovement(BlockState state, Vec3d multiplier)
	{
	
	}
	
	@Override
	public void tick()
	{
		if(isRemoved() || (world.isClient && Ultracraft.isTimeFrozen()))
			return;
		if (!shot) {
			emitGameEvent(GameEvent.PROJECTILE_SHOOT, getOwner());
			shot = true;
		}
		
		HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
		
		if (hitResult.getType() != HitResult.Type.MISS)
			onCollision(hitResult);
		checkBlockCollision();
		Vec3d vec3d = getVelocity();
		double d = getX() + vec3d.x;
		double e = getY() + vec3d.y;
		double f = getZ() + vec3d.z;
		updateRotation();
		setPosition(d, e, f);
		
		if(age > getMaxAge())
		{
			for (int i = 0; i < 5; i++)
				world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, getStack()),
						getX(), getY(), getZ(), 0f, 0f, 0f);
			if(!world.isClient())
				discard();
		}
	}
	
	protected int getMaxAge()
	{
		return 400;
	}
	
	protected boolean canHit(Entity entity)
	{
		boolean val = super.canHit(entity);
		return val || (isOwner(entity) && ((ProjectileEntityAccessor)this).isParried());
	}
}
