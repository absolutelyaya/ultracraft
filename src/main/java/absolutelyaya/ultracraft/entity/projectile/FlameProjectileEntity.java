package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.List;

public class FlameProjectileEntity extends ThrownItemEntity implements ProjectileEntityAccessor
{
	boolean griefing = true;
	
	public FlameProjectileEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	protected FlameProjectileEntity(LivingEntity owner, World world)
	{
		super(EntityRegistry.FLAME, owner, world);
	}
	
	public static FlameProjectileEntity spawn(LivingEntity owner, World world)
	{
		return new FlameProjectileEntity(owner, world);
	}
	
	@Override
	protected Item getDefaultItem()
	{
		return Items.FIRE_CHARGE;
	}
	
	@Override
	public boolean hasNoGravity()
	{
		return true;
	}
	
	void damageEntity(Entity target)
	{
		if(!isOwner(target) && !(getOwner() instanceof AbstractUltraHostileEntity && target instanceof AbstractUltraHostileEntity))
		{
			if(target instanceof LivingEntity entity && entity.damage(DamageSources.get(getWorld(), DamageSources.FLAMETHROWER, this, getOwner()), 2f))
				entity.setFireTicks(getOwner() instanceof PlayerEntity ? 100 : 50);
		}
	}
	
	@Override
	protected void onBlockHit(BlockHitResult blockHitResult)
	{
		super.onBlockHit(blockHitResult);
		Vec3i side = blockHitResult.getSide().getVector();
		Vec3d v = getVelocity();
		if(side.getX() != 0)
			v = v.multiply(0, 1, 1);
		else if(side.getY() != 0)
			v = v.multiply(1, 0, 1);
		else if(side.getZ() != 0)
			v = v.multiply(1, 1, 0);
		setVelocity(v);
	}
	
	@Override
	public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed)
	{
		boolean b = super.updateMovementInFluid(tag, speed);
		if(b && tag.equals(FluidTags.WATER))
		{
			getWorld().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), 0, 0.1, 0);
			kill();
		}
		return b;
	}
	
	double randomParticleOffset(double offset)
	{
		return random.nextDouble() * offset - offset / 2;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		setBoundingBox(getBoundingBox().expand(age / (getOwner() instanceof PlayerEntity ? 30f : 60f)));
		if(age % 3 == 0)
		{
			Box box = getBoundingBox();
			Vec3d pos = getPos().add(randomParticleOffset(box.getXLength()), randomParticleOffset(box.getYLength()),
					randomParticleOffset(box.getZLength()));
			Vec3d v = getVelocity().multiply(0.66f).add(random.nextDouble() * 0.05 - 0.025, random.nextDouble() * 0.05 - 0.025,
					random.nextDouble() * 0.05 - 0.025);
			getWorld().addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, v.x, v.y, v.z);
		}
		List<LivingEntity> collide = getWorld().getNonSpectatingEntities(LivingEntity.class, getBoundingBox());
		for(LivingEntity e : collide)
			damageEntity(e);
		
		setVelocity(getVelocity().multiply(0.9f));
		if(getVelocity().length() < 0.2f)
			kill();
		
		World world = getWorld();
		if(age > 2 && !world.isClient && griefing && world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK) &&
				    world.getGameRules().getBoolean(GameruleRegistry.FLAMETHROWER_GRIEF) && world.getBlockState(getBlockPos()).isAir() && random.nextInt(5) == 0)
			world.setBlockState(getBlockPos(), Block.postProcessState(Blocks.FIRE.getDefaultState(), world, getBlockPos()));
	}
	
	@Override
	public boolean isOnFire()
	{
		return false;
	}
	
	@Override
	public EntityDimensions getDimensions(EntityPose pose)
	{
		return super.getDimensions(pose);
	}
	
	public void setGriefing(boolean b)
	{
		griefing = b;
	}
	
	@Override
	public void setParried(boolean val, PlayerEntity parrier)
	{
	
	}
	
	@Override
	public boolean isParried()
	{
		return false;
	}
	
	@Override
	public boolean isParriable()
	{
		return false;
	}
	
	@Override
	public void onParriedCollision(HitResult hitResult)
	{
	
	}
	
	@Override
	public boolean isHitscanHittable(byte type)
	{
		return false;
	}
	
	@Override
	public boolean isBoostable()
	{
		return false;
	}
	
	@Override
	public PlayerEntity getParrier()
	{
		return null;
	}
	
	@Override
	public void setParrier(PlayerEntity p)
	{
	
	}
}
