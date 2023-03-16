package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.demon.CerberusEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class CerberusModel extends GeoModel<CerberusEntity>
{
	@Override
	public Identifier getModelResource(CerberusEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/cerberus.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(CerberusEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/cerberus.png");
	}
	
	@Override
	public Identifier getAnimationResource(CerberusEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entities/cerberus.animation.json");
	}
	
	@Override
	public void setCustomAnimations(CerberusEntity animatable, long instanceId, AnimationState<CerberusEntity> animationState)
	{
		super.setCustomAnimations(animatable, instanceId, animationState);
		CoreGeoBone head = this.getAnimationProcessor().getBone("head");
		
		float f = ((float) Math.PI / 180F);
		if(MinecraftClient.getInstance().isPaused())
			return;
		
		
		EntityModelData extraData = (EntityModelData)animationState.getExtraData().get(DataTickets.ENTITY_MODEL_DATA);
		if(head != null && animatable.getAnimation() != 1)
		{
			head.setRotX(head.getRotX() + extraData.headPitch() * f);
			head.setRotY(extraData.netHeadYaw() * f);
		}
	}
}
