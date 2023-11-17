package absolutelyaya.ultracraft.client.rendering.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.model.data.EntityModelData;

public class FilthModel extends GeoModel<FilthEntity>
{
	@Override
	public Identifier getModelResource(FilthEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/filth.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(FilthEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, object.isRare() ? "textures/entity/blue_filth.png" : "textures/entity/filth.png");
	}
	
	@Override
	public Identifier getAnimationResource(FilthEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entities/filth.animation.json");
	}
	
	@Override
	public void setCustomAnimations(FilthEntity animatable, long instanceId, AnimationState<FilthEntity> animationState)
	{
		super.setCustomAnimations(animatable, instanceId, animationState);
		CoreGeoBone head = this.getAnimationProcessor().getBone("Head");
		CoreGeoBone chest = this.getAnimationProcessor().getBone("TorsoUpper");
		
		if(chest != null)
			chest.setRotY((float)Math.toRadians(Math.cos((double)animatable.age * 3.25) * Math.PI * 0.8));
		
		float f = ((float) Math.PI / 180F);
		if(MinecraftClient.getInstance().isPaused())
			return;
		
		
		EntityModelData extraData = (EntityModelData)animationState.getExtraData().get(DataTickets.ENTITY_MODEL_DATA);
		if(head != null && !animatable.isInAttackAnimation())
		{
			head.setRotX(head.getRotX() + extraData.headPitch() * f);
			head.setRotY(extraData.netHeadYaw() * f);
			head.setRotZ((float)Math.toRadians(Math.cos((double)animatable.age * 3.25) * Math.PI * 1.2));
		}
	}
}
