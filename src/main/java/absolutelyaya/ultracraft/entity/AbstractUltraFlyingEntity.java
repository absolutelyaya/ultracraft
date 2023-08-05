package absolutelyaya.ultracraft.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class AbstractUltraFlyingEntity extends AbstractUltraHostileEntity
{
	final float drag;
	
	protected AbstractUltraFlyingEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		this.drag = 0.91f;
	}
	
	@Override
	public void travel(Vec3d movementInput)
	{
		if (canMoveVoluntarily() || isLogicalSideForUpdatingMovement())
		{
			updateVelocity(0.02f, movementInput);
			move(MovementType.SELF, getVelocity());
			setVelocity(getVelocity().multiply(drag));
		}
	}
	
	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource)
	{
		return false;
	}
	
	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
	}
	
	public float getDistanceToGround()
	{
		BlockHitResult hit = getWorld().raycast(new RaycastContext(getPos(), getPos().add(0, -25, 0),
				RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
		return getBlockY() - (float)hit.getPos().y;
	}
}
