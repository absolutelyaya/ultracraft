package absolutelyaya.ultracraft.client.rendering.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.husk.StrayEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.model.data.EntityModelData;

public class StrayModel extends GeoModel<StrayEntity>
{
	@Override
	public Identifier getModelResource(StrayEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/stray.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(StrayEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/stray.png");
	}
	
	@Override
	public Identifier getAnimationResource(StrayEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entities/stray.animation.json");
	}
	
	@Override
	public void setCustomAnimations(StrayEntity animatable, long instanceId, AnimationState<StrayEntity> animationState)
	{
		super.setCustomAnimations(animatable, instanceId, animationState);
		CoreGeoBone head = this.getAnimationProcessor().getBone("head");
		
		float f = ((float) Math.PI / 180F);
		if(MinecraftClient.getInstance().isPaused())
			return;
		
		EntityModelData extraData = (EntityModelData)animationState.getExtraData().get(DataTickets.ENTITY_MODEL_DATA);
		if(head != null)
		{
			head.setRotX(extraData.headPitch() * f);
			head.setRotY(extraData.netHeadYaw() * f);
		}
	}
}
