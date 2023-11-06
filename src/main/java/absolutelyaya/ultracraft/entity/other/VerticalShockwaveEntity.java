package absolutelyaya.ultracraft.entity.other;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VerticalShockwaveEntity extends ShockwaveEntity
{
	public VerticalShockwaveEntity(EntityType<?> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	boolean shouldDamage(Entity entity)
	{
		Vec3d right = getRotationVector().rotateY(90 * MathHelper.RADIANS_PER_DEGREE).normalize();
		if(Math.abs(right.dotProduct(entity.getPos().subtract(getPos()))) > 1.75f)
			return false;
		float dist = distanceTo(entity);
		return entity.isAlive() && dist < getRadius() + 1f && dist > getRadius() - 3f && !entity.getClass().equals(ignored) && !hits.contains(entity);
	}
	
	@Override
	public EntityDimensions getDimensions(EntityPose pose)
	{
		return EntityDimensions.changing(getRadius() * 2.0f, getRadius() * 2.0f);
	}
	
	@Override
	protected Box calculateBoundingBox()
	{
		return getDimensions(EntityPose.STANDING).getBoxAt(getPos().subtract(0f, getRadius(), 0f));
	}
}
