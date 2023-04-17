package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.DamageSources;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShotgunPelletEntity extends HellBulletEntity implements ProjectileEntityAccessor
{
	PlayerEntity parrier;
	
	public ShotgunPelletEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	protected ShotgunPelletEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.SHOTGUN_PELLET, owner, world);
	}
	
	public static ShotgunPelletEntity spawn(LivingEntity owner, World world)
	{
		return new ShotgunPelletEntity(owner, world);
	}
	
	@Override
	protected ItemStack getItem()
	{
		return ItemRegistry.CERBERUS_BALL.getDefaultStack();
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(age == 1 && !isRemoved())
			if(world.isClient)
				UltracraftClient.TRAIL_RENDERER.createTrail(uuid, this::getPoint, new Vector4f(1f, 1f, 0f, 0.4f), 5);
	}
	
	Pair<Vector3f, Vector3f> getPoint()
	{
		float yVel = (float)getVelocity().normalize().y;
		float xAngle = (float)Math.toRadians(yVel * 90);
		float yAngle = (float)Math.toRadians(Math.abs(yVel) * MinecraftClient.getInstance().gameRenderer.getCamera().getYaw());
		Vector3f left =	getPos().toVector3f().add(new Vector3f(0f, 0.1f, 0f).rotateX(xAngle).rotateY(yAngle));
		Vector3f right = getPos().toVector3f().add(new Vector3f(0f, -0.1f, 0f).rotateX(xAngle).rotateY(yAngle));
		return new Pair<>(left, right);
	}
	
	@Override
	protected int getMaxAge()
	{
		return 200;
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		Entity entity = entityHitResult.getEntity();
		if(!entity.getClass().equals(ignore) || ((ProjectileEntityAccessor)this).isParried())
			entity.damage(DamageSource.thrownProjectile(this, this.getOwner()), 2f);
	}
	
	@Override
	public void setParried(boolean val, PlayerEntity parrier)
	{
		if(parrier == getOwner())
			this.parrier = parrier;
	}
	
	@Override
	public boolean isParried()
	{
		return parrier != null;
	}
	
	@Override
	public boolean isParriable()
	{
		return age < 2;
	}
	
	@Override
	public void onParriedCollision(HitResult hitResult)
	{
		Entity owner = getOwner();
		Vec3d pos = hitResult.getPos();
		Entity hit = null;
		if(hitResult.getType().equals(HitResult.Type.ENTITY))
			hit = ((EntityHitResult)hitResult).getEntity();
		owner.damage(DamageSources.getParriedProjectile(parrier, this), 5);
		world.createExplosion(owner.equals(hit) ? hit : null,
				DamageSources.getParryCollateral(parrier), null, pos, 0.5f, false, World.ExplosionSourceType.NONE);
	}
	
	@Override
	public void onRemoved()
	{
		super.onRemoved();
		UltracraftClient.TRAIL_RENDERER.removeTrail(uuid);
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		super.onCollision(hitResult);
		UltracraftClient.TRAIL_RENDERER.removeTrail(uuid);
	}
}