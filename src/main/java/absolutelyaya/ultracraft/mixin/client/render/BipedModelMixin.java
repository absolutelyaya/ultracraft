package absolutelyaya.ultracraft.mixin.client.render;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.item.AbstractNailgunItem;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.item.SwordsmachinePlushieItem;
import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
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
	@Shadow @Final public ModelPart leftArm;
	@Shadow @Final public ModelPart rightArm;
	
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
			leftArm.resetTransform();
			rightArm.resetTransform();
			head.yaw = i * MathHelper.RADIANS_PER_DEGREE;
			head.pitch = j * MathHelper.RADIANS_PER_DEGREE;
			leftArm.translate(new Vector3f(0f, 13f, 7f));
			rightArm.translate(new Vector3f(0f, 12f, 7f));
		
			body.rotate(new Vector3f(-45f, -7.5f, 0f).mul(MathHelper.RADIANS_PER_DEGREE));
			if(livingEntity.getMainArm().equals(Arm.RIGHT))
				rightArm.rotate(new Vector3f(-67.6f, 12.53f, -0.44f).mul(MathHelper.RADIANS_PER_DEGREE).add(new Vector3f(head.pitch, head.yaw, 0f)));
			else
				leftArm.rotate(new Vector3f(-67.6f, 12.53f, -0.44f).mul(MathHelper.RADIANS_PER_DEGREE).add(new Vector3f(head.pitch, head.yaw, 0f)));
			justSlid = true;
			ci.cancel();
		}
		else if(justSlid)
		{
			head.resetTransform();
			leftArm.resetTransform();
			rightArm.resetTransform();
			justSlid = false;
		}
	}
	
	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;animateArms(Lnet/minecraft/entity/LivingEntity;F)V"))
	void onSetArmAngle(T living, float f, float g, float h, float headYaw, float headPitch, CallbackInfo ci)
	{
		Item heldItem = living.getMainHandStack().getItem();
		if(heldItem instanceof SwordsmachinePlushieItem)
		{
			Vector3f angles;
			boolean rightHanded = living.getMainArm().equals(Arm.RIGHT);
			if(rightHanded || (!rightHanded && living.getMainHandStack().isEmpty()))
			{
				leftArm.resetTransform();
				angles = new Vector3f(-55, 45, 0).mul(MathHelper.RADIANS_PER_DEGREE);
				leftArm.setAngles(angles.x, angles.y, angles.z);
			}
			if(living.getMainHandStack().isEmpty())
			{
				rightArm.resetTransform();
				angles = new Vector3f(-45, -30, 0).mul(MathHelper.RADIANS_PER_DEGREE);
				rightArm.setAngles(angles.x, angles.y, angles.z);
			}
		}
		if(heldItem instanceof AbstractWeaponItem w && w.shouldAim())
			genericAimPose(living);
		if(heldItem instanceof AbstractNailgunItem)
			heavyAimPose(living);
	}
	
	void genericAimPose(LivingEntity livingEntity)
	{
		if(livingEntity.getMainArm().equals(Arm.LEFT))
		{
			leftArm.resetTransform();
			Vector3f angles = new Vector3f(-90f - ((LivingEntityAccessor)livingEntity).getRecoil(), 0f, 0f).mul(MathHelper.RADIANS_PER_DEGREE)
									  .add(new Vector3f(head.pitch, head.yaw, 0f));
			leftArm.setAngles(angles.x, angles.y, angles.z);
		}
		else
		{
			rightArm.resetTransform();
			Vector3f angles = new Vector3f(-90f - ((LivingEntityAccessor)livingEntity).getRecoil(), 0f, 0f).mul(MathHelper.RADIANS_PER_DEGREE)
									  .add(new Vector3f(head.pitch, head.yaw, 0f));
			rightArm.setAngles(angles.x, angles.y, angles.z);
		}
	}
	
	void heavyAimPose(LivingEntity livingEntity)
	{
		boolean flipped = livingEntity.getMainArm().equals(Arm.LEFT);
		ModelPart main = flipped ? leftArm : rightArm;
		ModelPart off = flipped ? rightArm : leftArm;
		Vector3f angles = new Vector3f(-65f - ((LivingEntityAccessor)livingEntity).getRecoil(), flipped ? 20f : -20f, 0f).mul(MathHelper.RADIANS_PER_DEGREE)
								  .add(new Vector3f(head.pitch, head.yaw, 0f));
		main.setAngles(angles.x, angles.y, angles.z);
		angles = new Vector3f(-90f - ((LivingEntityAccessor)livingEntity).getRecoil(), flipped ? -25f : 25f, 0f).mul(MathHelper.RADIANS_PER_DEGREE)
								  .add(new Vector3f(head.pitch, head.yaw, 0f));
		off.setAngles(angles.x, angles.y, angles.z);
	}
}
