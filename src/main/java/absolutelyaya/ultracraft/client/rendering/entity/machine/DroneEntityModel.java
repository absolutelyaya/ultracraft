package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.machine.DroneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class DroneEntityModel extends GeoModel<DroneEntity>
{
	@Override
	public Identifier getModelResource(DroneEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/drone.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(DroneEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/drone" + (animatable.isAttacking() ? "2" : "") + ".png");
	}
	
	@Override
	public Identifier getAnimationResource(DroneEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entities/drone.animation.json");
	}
	
	@Override
	public void setCustomAnimations(DroneEntity animatable, long instanceId, AnimationState<DroneEntity> animationState)
	{
		super.setCustomAnimations(animatable, instanceId, animationState);
		CoreGeoBone root = this.getAnimationProcessor().getBone("root");
		EntityModelData extraData = (EntityModelData)animationState.getExtraData().get(DataTickets.ENTITY_MODEL_DATA);
		if(root != null)
		{
			float f = ((float) Math.PI / 180F);
			root.setRotX(extraData.headPitch() * f);
			if(animatable.getTarget() != null)
				root.setRotY((animatable.getYaw() - animatable.getHeadYaw()) * -f);
			else
				root.setRotY(extraData.netHeadYaw() * f);
			root.setRotZ(0f);
			if(animatable.isFalling())
			{
				float deltaTick = MinecraftClient.getInstance().getTickDelta();
				Vec3d rot = animatable.getFallRot().normalize().multiply(animatable.getFallingTicks() + deltaTick);
				root.setRotX((float)(root.getRotX() + rot.x * 0.25));
				root.setRotY((float)(root.getRotY() + rot.y * 0.25));
				root.setRotZ((float)(root.getRotZ() + rot.z * 0.25));
			}
		}
	}
}
