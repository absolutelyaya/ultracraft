package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);
		Entity entity = entityHitResult.getEntity();
		if(!entity.getClass().equals(ignore) || ((ProjectileEntityAccessor)this).isParried())
			entity.damage(DamageSource.thrownProjectile(this, this.getOwner()), 6f);
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		super.onCollision(hitResult);
		if(hitResult instanceof EntityHitResult ehr && ehr.getEntity().getClass().equals(ignore) && !((ProjectileEntityAccessor)this).isParried())
			return;
		if (!this.world.isClient)
		{
			this.world.sendEntityStatus(this, (byte)3);
			this.kill();
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
		if(world.isClient && Ultracraft.isTimeFrozen())
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
		
		if(age > 400)
		{
			for (int i = 0; i < 5; i++)
				world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, getStack()),
						getX(), getY(), getZ(), 0f, 0f, 0f);
			remove(RemovalReason.DISCARDED);
		}
	}
	
	protected boolean canHit(Entity entity)
	{
		boolean val = super.canHit(entity);
		return val || (isOwner(entity) && ((ProjectileEntityAccessor)this).isParried());
	}
}
