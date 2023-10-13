package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.machine.V2Entity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class V2Model extends GeoModel<V2Entity>
{
	@Override
	public Identifier getModelResource(V2Entity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/v2.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(V2Entity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/v2.png");
	}
	
	@Override
	public Identifier getAnimationResource(V2Entity animatable)
	{
		return /*new Identifier(Ultracraft.MOD_ID, "animations/entities/swordsmachine.animation.json")*/ null;
	}
	
	@Override
	public void setCustomAnimations(V2Entity animatable, long instanceId, AnimationState<V2Entity> animationState)
	{
		super.setCustomAnimations(animatable, instanceId, animationState);
		CoreGeoBone head = this.getAnimationProcessor().getBone("head");
		
		float f = ((float) Math.PI / 180F);
		if(MinecraftClient.getInstance().isPaused())
			return;
		
		EntityModelData extraData = (EntityModelData)animationState.getExtraData().get(DataTickets.ENTITY_MODEL_DATA);
		if(head != null && animatable.getAnimation() != 1)
		{
			head.setRotX(extraData.headPitch() * f);
			head.setRotY(extraData.netHeadYaw() * f);
		}
	}
}
