package absolutelyaya.ultracraft.client.rendering.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.machine.SwordmachineEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class SwordmachineModel extends GeoModel<SwordmachineEntity>
{
	@Override
	public Identifier getModelResource(SwordmachineEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entities/swordmachine.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(SwordmachineEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/swordmachine.png");
	}
	
	@Override
	public Identifier getAnimationResource(SwordmachineEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entities/swordmachine.animation.json");
	}
	
	@Override
	public void setCustomAnimations(SwordmachineEntity animatable, long instanceId, AnimationState<SwordmachineEntity> animationState)
	{
		super.setCustomAnimations(animatable, instanceId, animationState);
		CoreGeoBone head = this.getAnimationProcessor().getBone("head");
		
		float f = ((float) Math.PI / 180F);
		if(MinecraftClient.getInstance().isPaused())
			return;
		
		
		EntityModelData extraData = (EntityModelData)animationState.getExtraData().get(DataTickets.ENTITY_MODEL_DATA);
		if(head != null /*&& animatable.getAnimation() != 1*/)
		{
			head.setRotX(head.getRotX() + extraData.headPitch() * f);
			head.setRotY(extraData.netHeadYaw() * f);
		}
	}
}
