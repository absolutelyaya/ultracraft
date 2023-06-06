package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.ExplosionHandler;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.ProjectileEntityAccessor;
import absolutelyaya.ultracraft.damage.DamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity implements ProjectileEntityAccessor
{
	@Shadow @Nullable private Entity owner;
	PlayerEntity parrier;
	boolean parried, frozen;
	Vec3d preFreezeVel;
	
	public ProjectileEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	@Inject(method = "onBlockHit", at = @At("HEAD"))
	void onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci)
	{
		collide(blockHitResult);
	}
	
	@Inject(method = "onEntityHit", at = @At("HEAD"))
	void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci)
	{
		if(parried == entityHitResult.getEntity().equals(owner) && parrier != null)
			parrier.heal(6);
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
		if(parried)
		{
			onParriedCollision(hitResult);
			parried = false;
		}
	}
	
	@Override
	public void onParriedCollision(HitResult hitResult)
	{
		Vec3d pos = hitResult.getPos();
		if(owner == null)
		{
			ExplosionHandler.explosion(null, world, pos, DamageSources.get(world, DamageSources.PARRYAOE, parrier), 5f, 1f, 3f, true);
			return;
		}
		Entity hit = null;
		if(hitResult.getType().equals(HitResult.Type.ENTITY))
			hit = ((EntityHitResult)hitResult).getEntity();
		if(owner.equals(hit))
			owner.damage(DamageSources.get(world, DamageSources.PARRY, parrier), 15);
		ExplosionHandler.explosion(owner.equals(hit) ? hit : null, world, pos, DamageSources.get(world, DamageSources.PARRYAOE, parrier), 5f, 1f, 3f, true);
	}
	
	@Override
	public void setParried(boolean val, PlayerEntity parrier)
	{
		parried = val;
		this.parrier = parrier;
	}
	
	@Override
	public boolean isParried()
	{
		return parried;
	}
	
	@Override
	public boolean isParriable()
	{
		return true;
	}
	
	@Override
	public boolean isHitscanHittable()
	{
		return false;
	}
}
