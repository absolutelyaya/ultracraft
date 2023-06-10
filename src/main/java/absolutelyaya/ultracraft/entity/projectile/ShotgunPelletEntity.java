package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShotgunPelletEntity extends HellBulletEntity implements ProjectileEntityAccessor
{
	BlockPos hitPos = null;
	boolean chosenOne;
	float damage = 0.5f;
	
	public ShotgunPelletEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	protected ShotgunPelletEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.SHOTGUN_PELLET, owner, world);
	}
	
	public static ShotgunPelletEntity spawn(LivingEntity owner, World world, boolean chosen)
	{
		ShotgunPelletEntity pellet = new ShotgunPelletEntity(owner, world);
		pellet.chosenOne = chosen;
		return pellet;
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
		if(age == 5 && chosenOne) //after projboost time window, set speed to normal pellets
			setVelocity(getVelocity().normalize().multiply(1.5f));
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
	
	public void increaseDamage(float val)
	{
		damage += val;
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		Entity entity = entityHitResult.getEntity();
		if(!entity.getClass().equals(ignore) || ((ProjectileEntityAccessor)this).isParried())
			entity.damage(DamageSources.get(world, DamageSources.SHOTGUN, getOwner()), damage);
	}
	
	@Override
	protected void onBlockHit(BlockHitResult blockHitResult)
	{
		super.onBlockHit(blockHitResult);
		hitPos = blockHitResult.getBlockPos();
	}
	
	public void handleStatus(byte status)
	{
		if (status == 3)
		{
			if(hitPos != null)
				world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, world.getBlockState(hitPos)), true,
						getX(), getY(), getZ(), 0, 0, 0);
		}
	}
	
	@Override
	public void onParriedCollision(HitResult hitResult)
	{
		Entity owner = getOwner();
		Vec3d pos = hitResult.getPos();
		ExplosionHandler.explosion(owner, world, pos, DamageSources.get(world, DamageSources.PROJBOOST, parrier), 3.5f, 2.3f, 3f, true);
	}
	
	@Override
	public void onRemoved()
	{
		super.onRemoved();
		if(world.isClient)
			UltracraftClient.TRAIL_RENDERER.removeTrail(uuid);
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		if(isRemoved())
			return;
		if(world.isClient)
			UltracraftClient.TRAIL_RENDERER.removeTrail(uuid);
		super.onCollision(hitResult);
	}
	
	@Override
	public boolean isBoostable()
	{
		return super.isBoostable() && chosenOne;
	}
	
	@Override
	public void setParried(boolean val, PlayerEntity parrier)
	{
		if(parrier == getOwner())
			this.parrier = parrier;
	}
	
	@Override
	public boolean isParriable()
	{
		return age < 4;
	}
}
