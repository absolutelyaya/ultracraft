package absolutelyaya.ultracraft.util;

import absolutelyaya.ultracraft.accessor.EntityAccessor;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.entity.other.InterruptableCharge;
import absolutelyaya.ultracraft.entity.projectile.EjectedCoreEntity;
import absolutelyaya.ultracraft.entity.projectile.ThrownCoinEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2f;

import java.util.List;

public class AutoAimUtil
{
	public static Vector2f dirToPolar(Vec3d dir)
	{
		float horizontalMagnitude = new Vector2f((float)dir.x, (float)dir.z).length();
		return new Vector2f((float)MathHelper.atan2(-dir.y, horizontalMagnitude), (float)MathHelper.atan2(dir.x, dir.z)).mul(MathHelper.DEGREES_PER_RADIAN);
	}
	
	public static Entity getTarget(Entity source, LivingEntity attacker, World world, Vec3d origin)
	{
		return getTarget(source, world, origin, getPotentialTargets(source, attacker, world));
	}
	
	static boolean isMarksmanHittable(Entity e)
	{
		return (!(e instanceof PlayerEntity p && !isPlayerHittable(p)) && ((e instanceof LivingEntity && ((LivingEntityAccessor)e).isRicochetHittable() && ((LivingEntity) e).getHealth() > 0)) || e instanceof EjectedCoreEntity || e instanceof InterruptableCharge);
	}
	
	static boolean isPlayerHittable(PlayerEntity p)
	{
		return p.getServer().isPvpEnabled() && !p.isCreative() && !p.isSpectator();
	}
	
	public static Entity getTarget(Entity source, World world, Vec3d origin, List<Entity> targets)
	{
		if (targets.size() > 0)
		{
			int highestPriority = 0;
			Entity closest = null;
			float closestDistance = Float.MAX_VALUE;
			for (Entity e : targets)
			{
				int priority = ((EntityAccessor)e).getTargetPriority(source);
				if(priority < highestPriority)
					continue;
				float dist = (float)(origin.distanceTo(((EntityAccessor)e).getRelativeTargetPoint()) +
													 ((source instanceof ThrownCoinEntity coin && e.equals(coin.getLastTarget())) ? 10f : 0f));
				if (dist < closestDistance && !(source.isPlayer() && e.isPlayer() && !world.getServer().isPvpEnabled()))
				{
					closestDistance = dist;
					closest = e;
					highestPriority = priority;
				}
			}
			return closest;
		}
		return null;
	}
	
	public static List<Entity> getPotentialTargets(Entity source, LivingEntity attacker, World world)
	{
		boolean isSourceCoin = source instanceof ThrownCoinEntity;
		return world.getOtherEntities(attacker, source.getBoundingBox().expand(32f),
						e -> !e.equals(source) && (isSourceCoin && isMarksmanHittable(e)) || (!isSourceCoin && ((EntityAccessor)e).isTargettable()) && !e.isTeammate(attacker));
	}
}
