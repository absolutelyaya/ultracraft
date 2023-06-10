package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedModelMixin<T extends LivingEntity> extends AnimalModel<T>
{
	@Shadow @Final public ModelPart head;
	@Shadow @Final public ModelPart body;
	@Shadow @Final public ModelPart leftLeg;
	@Shadow @Final public ModelPart rightLeg;
	@Shadow @Final public ModelPart leftArm;
	@Shadow @Final public ModelPart rightArm;
	
	@Shadow @Final public ModelPart hat;
	boolean justSlid;
	
	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"), cancellable = true)
	void onSetAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci)
	{
		if(!UltracraftClient.applyEntityPoses)
			return;
		if(livingEntity.getPose().equals(ClassTinkerers.getEnum(EntityPose.class, "SLIDE")))
		{
			if(livingEntity.equals(MinecraftClient.getInstance().getCameraEntity()) && !MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson())
				return; //prevent arms from getting fucked up by not applying the pose in first person
			head.resetTransform();
			body.resetTransform();
			leftLeg.resetTransform();
			rightLeg.resetTransform();
			leftArm.resetTransform();
			rightArm.resetTransform();
			
			head.yaw = i * MathHelper.RADIANS_PER_DEGREE;
			head.pitch = j * MathHelper.RADIANS_PER_DEGREE;
			head.translate(new Vector3f(0f, 14f, 8.5f));
			body.translate(new Vector3f(0f, 13f, 8f));
			leftArm.translate(new Vector3f(0f, 13f, 7f));
			rightArm.translate(new Vector3f(0f, 12f, 7f));
			leftLeg.translate(new Vector3f(1f, 9f, 0f));
			rightLeg.translate(new Vector3f(1f, 9f, -0.5f));
			
			body.rotate(new Vector3f(-45f, -7.5f, 0f).mul(MathHelper.RADIANS_PER_DEGREE));
			if(livingEntity.getMainArm().equals(Arm.RIGHT))
			{
				leftArm.rotate(new Vector3f(32.5f, 22.5f, 0f).mul(MathHelper.RADIANS_PER_DEGREE));
				rightArm.rotate(new Vector3f(-67.6f, 12.53f, -0.44f).mul(MathHelper.RADIANS_PER_DEGREE).add(new Vector3f(head.pitch, head.yaw, 0f)));
			}
			else
			{
				rightArm.rotate(new Vector3f(32.5f, 22.5f, 0f).mul(MathHelper.RADIANS_PER_DEGREE));
				leftArm.rotate(new Vector3f(-67.6f, 12.53f, -0.44f).mul(MathHelper.RADIANS_PER_DEGREE).add(new Vector3f(head.pitch, head.yaw, 0f)));
			}
			leftLeg.rotate(new Vector3f(-82.49f, 2.47f, 0.32f).mul(MathHelper.RADIANS_PER_DEGREE));
			rightLeg.rotate(new Vector3f(-75.86f, -7.11f, -2.36f).mul(MathHelper.RADIANS_PER_DEGREE));
			
			hat.copyTransform(head);
			
			justSlid = true;
			ci.cancel();
		}
		else if(justSlid)
		{
			head.resetTransform();
			body.resetTransform();
			leftLeg.resetTransform();
			rightLeg.resetTransform();
			leftArm.resetTransform();
			rightArm.resetTransform();
			justSlid = false;
		}
		//TODO: add Dash Pose... pose.
		//TODO: add Slam Pose..? could be epic
	}
	
	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
	void onSetArmAngle(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci)
	{
		if(!UltracraftClient.applyEntityPoses)
			return;
		if(livingEntity.getMainHandStack().getItem() instanceof AbstractWeaponItem w && w.shouldAim())
		{
			if(livingEntity.getMainArm().equals(Arm.LEFT))
			{
				leftArm.resetTransform();
				leftArm.rotate(new Vector3f(-90f - ((LivingEntityAccessor)livingEntity).getRecoil(), 0f, 0f)
									   .mul(MathHelper.RADIANS_PER_DEGREE).add(new Vector3f(head.pitch, head.yaw * 0.6f, 0f)));
			}
			else
			{
				rightArm.resetTransform();
				rightArm.rotate(new Vector3f(-90f - ((LivingEntityAccessor)livingEntity).getRecoil(), 0f, 0f)
										.mul(MathHelper.RADIANS_PER_DEGREE).add(new Vector3f(head.pitch, head.yaw * 0.6f, 0f)));
			}
		}
	}
}
