package absolutelyaya.ultracraft.client.rendering.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import absolutelyaya.ultracraft.entity.husk.GreaterFilthEntity;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.model.data.EntityModelData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class GreaterFilthModel extends GeoModel<GreaterFilthEntity>
{
	@Override
	public Identifier getModelResource(GreaterFilthEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/greaterfilth.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(GreaterFilthEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, object.isRare() ? "textures/entity/blue_filth.png" : "textures/entity/filth.png");
	}
	
	@Override
	public Identifier getAnimationResource(GreaterFilthEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entities/greaterfilth.animation.json");
	}
	
	@Override
	public void setCustomAnimations(GreaterFilthEntity animatable, long instanceId, AnimationState<GreaterFilthEntity> animationState)
	{
		super.setCustomAnimations(animatable, instanceId, animationState);
		CoreGeoBone head = this.getAnimationProcessor().getBone("Head");
		
		float f = ((float) Math.PI / 180F);
		if(MinecraftClient.getInstance().isPaused())
			return;
		
		
		EntityModelData extraData = (EntityModelData)animationState.getExtraData().get(DataTickets.ENTITY_MODEL_DATA);
		if(head != null && !animatable.isHeadFixed())
		{
			head.setRotX(head.getRotX() + extraData.headPitch() * f);
			head.setRotY(extraData.netHeadYaw() * f);
		}
	}
}
