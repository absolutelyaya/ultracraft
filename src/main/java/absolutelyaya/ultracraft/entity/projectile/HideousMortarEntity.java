package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class HideousMortarEntity extends HellBulletEntity implements ProjectileEntityAccessor
{
	LivingEntity target;
	
	public HideousMortarEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
		difficultySpeed = false;
	}
	
	protected HideousMortarEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.MORTAR, owner, world);
		difficultySpeed = false;
	}
	
	public static HideousMortarEntity spawn(World world, Vec3d pos, LivingEntity owner, LivingEntity target)
	{
		HideousMortarEntity mortar = new HideousMortarEntity(owner, world);
		mortar.setPosition(pos);
		mortar.setTarget(target);
		mortar.setVelocity(0, 1.35, 0);
		mortar.setIgnored(owner.getClass());
		world.spawnEntity(mortar);
		return mortar;
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		super.onCollision(hitResult);
		ExplosionHandler.explosion(getOwner(), getWorld(), hitResult.getPos(), getDamageSources().explosion(this, getOwner()), 15f, 4f, 4f, true);
	}
	
	public static HideousMortarEntity spawn(LivingEntity owner, World world)
	{
		return new HideousMortarEntity(owner, world);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(age == 1 && !isRemoved())
			if(getWorld().isClient)
				UltracraftClient.TRAIL_RENDERER.createTrail(uuid, this::getPoint, new Vector4f(1f, 1f, 0f, 0.4f), 10);
		Vec3d lastVel = getVelocity();
		if(target != null)
		{
			Vector2d pos2d = new Vector2d(getX(), getZ());
			Vector2d targetPos2d = new Vector2d(target.getX(), target.getZ());
			setVelocity(target.getPos().subtract(getPos()).multiply(1, 0, 1).normalize()
								.multiply(0.15f * Math.min(pos2d.distance(targetPos2d) / 6f, 1.5f)).add(0f, lastVel.y, 0f));
		}
		if(getVelocity().y > -1.5)
			setVelocity(getVelocity().subtract(0, 0.0275f, 0));
	}
	
	Pair<Vector3f, Vector3f> getPoint()
	{
		float yVel = (float)getVelocity().normalize().y;
		float xAngle = (float)Math.toRadians(yVel * 90);
		float yAngle = (float)Math.toRadians(Math.abs(yVel) * MinecraftClient.getInstance().gameRenderer.getCamera().getYaw());
		Vector3f left =	getPos().toVector3f().add(new Vector3f(0f, 0.5f, 0f).rotateX(xAngle).rotateY(yAngle));
		Vector3f right = getPos().toVector3f().add(new Vector3f(0f, -0.5f, 0f).rotateX(xAngle).rotateY(yAngle));
		return new Pair<>(left, right);
	}
	
	@Override
	protected Item getDefaultItem()
	{
		return ItemRegistry.CERBERUS_BALL;
	}
	
	public void setTarget(LivingEntity target)
	{
		this.target = target;
	}
}
