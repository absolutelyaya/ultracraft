package absolutelyaya.ultracraft.client.rendering.entity.feature;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.components.IArmComponent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

public class ArmFeature<T extends PlayerEntity, M extends PlayerEntityModel<T>> extends FeatureRenderer<T, M>
{
	static final Identifier FEEDBACKER_SLIM = new Identifier(Ultracraft.MOD_ID, "textures/entity/arms/feedbacker_slim.png");
	static final Identifier KNUCKLEBLASTER_SLIM = new Identifier(Ultracraft.MOD_ID, "textures/entity/arms/knuckleblaster_slim.png");
	static final Identifier FEEDBACKER = new Identifier(Ultracraft.MOD_ID, "textures/entity/arms/feedbacker.png");
	static final Identifier KNUCKLEBLASTER = new Identifier(Ultracraft.MOD_ID, "textures/entity/arms/knuckleblaster.png");
	
	public ArmFeature(FeatureRendererContext<T, M> context)
	{
		super(context);
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
	{
		IArmComponent arms = UltraComponents.ARMS.get(entity);
		byte activeArm = arms.getActiveArm();
		PlayerEntityModel<T> model = getContextModel();
		boolean noneEquipped = activeArm == -1;
		if(noneEquipped)
			return;
		boolean slim = entity instanceof AbstractClientPlayerEntity clientPlayer && clientPlayer.getModel().equals("slim");
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(getTexture(activeArm, slim)));
		int overlay = LivingEntityRenderer.getOverlay(entity, 0f);
		if(entity.getMainArm().equals(Arm.RIGHT))
		{
			model.leftArm.render(matrices, consumer, light, overlay);
			model.leftSleeve.render(matrices, consumer, light, overlay);
		}
		else
		{
			model.rightArm.render(matrices, consumer, light, overlay);
			model.rightSleeve.render(matrices, consumer, light, overlay);
		}
		//TODO: hide normal arm
	}
	
	public static Identifier getTexture(byte arm, boolean slim)
	{
		if(arm == 1)
			return slim ? KNUCKLEBLASTER_SLIM : KNUCKLEBLASTER;
		else
			return slim ? FEEDBACKER_SLIM : FEEDBACKER;
	}
}
