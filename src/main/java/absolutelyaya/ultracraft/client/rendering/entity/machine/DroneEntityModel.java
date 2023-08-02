package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.machine.DroneEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
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
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/drone.png");
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
			root.setRotX(root.getRotX() + extraData.headPitch() * MathHelper.RADIANS_PER_DEGREE);
			root.setRotY(extraData.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
		}
	}
}
