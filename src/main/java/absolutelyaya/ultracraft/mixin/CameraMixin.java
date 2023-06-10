package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin
{
	@Shadow public abstract Vec3d getPos();
	
	@Shadow public abstract float getYaw();
	
	@Shadow @Final private Vector3f horizontalPlane;
	
	@Shadow @Final private Vector3f verticalPlane;
	
	@Shadow @Final private Vector3f diagonalPlane;
	
	@Shadow protected abstract void setPos(Vec3d pos);
	
	@Shadow private Vec3d pos;
	
	@Inject(method = "update", at = @At("TAIL"))
	void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci)
	{
		if(!(focusedEntity instanceof WingedPlayerEntity winged))
			return;
		float f = UltracraftClient.getConfigHolder().get().slideCamOffset / 100f;
		if(thirdPerson && f > 0f)
		{
			boolean flip = ((PlayerEntity)winged).getMainArm().equals(Arm.LEFT);
			if(winged.isWingsActive() && ((PlayerEntity)winged).isSprinting())
			{
				Vec3d offset = rotationize(new Vec3d(1.5f * f, f, -1.5f * f * (flip ? -1 : 1)));
				HitResult hitResult = area.raycast(new RaycastContext(getPos(), getPos().add(offset), RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, focusedEntity));
				if(!hitResult.getType().equals(HitResult.Type.MISS))
					offset = hitResult.getPos().subtract(getPos()).add(rotationize(new Vec3d(0f, 0f, 0.2f * (flip ? -1 : 1))));
				setPos(new Vec3d(pos.x + offset.x, pos.y + offset.y, pos.z + offset.z));
			}
		}
	}
	
	Vec3d rotationize(Vec3d vec) //aka apply rotation
	{
		double x = horizontalPlane.x() * vec.x + verticalPlane.x() * vec.y + diagonalPlane.x() * vec.z;
		double y = horizontalPlane.y() * vec.x + verticalPlane.y() * vec.y + diagonalPlane.y() * vec.z;
		double z = horizontalPlane.z() * vec.x + verticalPlane.z() * vec.y + diagonalPlane.z() * vec.z;
		return new Vec3d(x, y, z);
	}
}
