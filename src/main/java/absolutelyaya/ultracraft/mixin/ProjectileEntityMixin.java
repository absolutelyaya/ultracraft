package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ChainParryAccessor;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.entity.projectile.ShotgunPelletEntity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity implements ProjectileEntityAccessor, ChainParryAccessor
{
	@Shadow @Nullable private Entity owner;
	
	@Shadow protected abstract boolean canHit(Entity entity);
	
	@Shadow private boolean leftOwner;
	
	@Shadow protected abstract void onEntityHit(EntityHitResult entityHitResult);
	
	private static final TrackedData<Integer> PARRIES = DataTracker.registerData(ProjectileEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected PlayerEntity parrier;
	boolean frozen;
	Vec3d preFreezeVel;
	
	public ProjectileEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	@Inject(method = "<init>", at = @At("TAIL"))
	void onInit(EntityType<?> entityType, World world, CallbackInfo ci)
	{
		dataTracker.startTracking(PARRIES, 0);
	}
	
	@Inject(method = "onBlockHit", at = @At("HEAD"))
	void onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci)
	{
		collide(blockHitResult);
	}
	
	@Inject(method = "onEntityHit", at = @At("HEAD"))
	void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci)
	{
		collide(entityHitResult);
	}
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	void onTick(CallbackInfo ci)
	{
		if(Ultracraft.isTimeFrozen())
		{
			if(!frozen)
			{
				preFreezeVel = getVelocity();
				setVelocity(0f, 0f, 0f);
				frozen = true;
			}
			ci.cancel();
		}
		else if(frozen)
		{
			setVelocity(preFreezeVel);
			frozen = false;
		}
		
		if(!leftOwner)
		{
			List<Entity> entities = getWorld().getOtherEntities(owner, getBoundingBox().stretch(this.getVelocity()), this::canHit);
			if(entities.size() > 0)
				onEntityHit(new EntityHitResult(entities.get(0)));
		}
	}
	
	@Inject(method = "updateRotation()V", at = @At("HEAD"), cancellable = true)
	void OnUpdateRotation(CallbackInfo ci)
	{
		if(frozen)
			ci.cancel();
	}
	
	@Override
	public void setVelocity(Vec3d velocity)
	{
		if(frozen)
			preFreezeVel = velocity;
		else
			super.setVelocity(velocity);
	}
	
	void collide(HitResult hitResult)
	{
		if(isParried())
		{
			onParriedCollision(hitResult);
			dataTracker.set(PARRIES, 0);
		}
	}
	
	@Override
	public void onParriedCollision(HitResult hitResult)
	{
		if(getParryCount() <= 0)
			return;
		int parries = getParryCount() - 1;
		float damageMult = 1f + parries * 0.2f;
		float rangeMult = 1f + parries * 0.1f;
		Vec3d pos = hitResult.getPos();
		if(owner == null)
		{
			ExplosionHandler.explosion(null, getWorld(), pos, DamageSources.get(getWorld(), DamageSources.PARRYAOE, parrier),
					5f * damageMult, 1f, 3f * rangeMult, true);
			return;
		}
		Entity hit = null;
		if(hitResult.getType().equals(HitResult.Type.ENTITY))
			hit = ((EntityHitResult)hitResult).getEntity();
		if(owner.equals(hit))
			owner.damage(DamageSources.get(getWorld(), DamageSources.PARRY, parrier), 15 * damageMult);
		ExplosionHandler.explosion(owner.equals(hit) ? hit : null, getWorld(), pos, DamageSources.get(getWorld(), DamageSources.PARRYAOE, parrier),
				5f * damageMult, 1f, 3f * rangeMult, true);
	}
	
	@Override
	public void setParried(boolean val, PlayerEntity parrier)
	{
		dataTracker.set(PARRIES, dataTracker.get(PARRIES) + 1);
		this.parrier = parrier;
		age = 0;
	}
	
	@Override
	public boolean isParried()
	{
		return dataTracker.get(PARRIES) > 0;
	}
	
	@Override
	public boolean isParriable()
	{
		return true;
	}
	
	public PlayerEntity getParrier()
	{
		return parrier;
	}
	
	@Override
	public boolean isBoostable()
	{
		return switch(getWorld().getGameRules().get(GameruleRegistry.PROJ_BOOST).get())
		{
			case ALLOW_ALL -> true;
			case ENTITY_TAG -> getType().isIn(EntityRegistry.PROJBOOSTABLE);
			case LIMITED -> (Object) this instanceof ShotgunPelletEntity;
			case DISALLOW -> false;
		} && age < 4;
	}
	
	@Override
	public boolean isHitscanHittable(byte type)
	{
		return type == ServerHitscanHandler.SHARPSHOOTER;
	}
	
	@Override
	public int getParryCount()
	{
		return dataTracker.get(PARRIES);
	}
	
	@Override
	public void setParryCount(int val)
	{
		dataTracker.set(PARRIES, val);
	}
}
