package absolutelyaya.ultracraft.client.entity.husk;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.husk.FilthEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class FilthModel extends AnimatedGeoModel<FilthEntity>
{
	@Override
	public Identifier getModelResource(FilthEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "geo/entity/filth.geo.json");
	}
	
	@Override
	public Identifier getTextureResource(FilthEntity object)
	{
		return new Identifier(Ultracraft.MOD_ID, "textures/entity/filth.png");
	}
	
	@Override
	public Identifier getAnimationResource(FilthEntity animatable)
	{
		return new Identifier(Ultracraft.MOD_ID, "animations/entity/filth.animation.json");
	}
	
	@SuppressWarnings({"unchecked"})
	@Override
	public void setLivingAnimations(FilthEntity entity, Integer uniqueID, AnimationEvent customPredicate)
	{
		super.setLivingAnimations(entity, uniqueID, customPredicate);
		IBone head = this.getAnimationProcessor().getBone("Head");
		IBone chest = this.getAnimationProcessor().getBone("TorsoUpper");
		
		if(chest != null)
			chest.setRotationY((float)Math.toRadians(Math.cos((double)entity.age * 3.25) * Math.PI * 0.8));
		
		float f = ((float) Math.PI / 180F);
		if(MinecraftClient.getInstance().isPaused())
			return;
		
		EntityModelData extraData = (EntityModelData)customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
		if(head != null && entity.getAnimation() != 1)
		{
			head.setRotationX(head.getRotationX() + extraData.headPitch * f);
			head.setRotationY(extraData.netHeadYaw * f);
			head.setRotationZ((float)Math.toRadians(Math.cos((double)entity.age * 3.25) * Math.PI * 1.2));
		}
	}
}
