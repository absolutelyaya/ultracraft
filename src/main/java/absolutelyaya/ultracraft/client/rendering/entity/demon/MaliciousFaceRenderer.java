package absolutelyaya.ultracraft.client.rendering.entity.demon;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.entity.demon.MaliciousFaceEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class MaliciousFaceRenderer extends MobEntityRenderer<MaliciousFaceEntity, MaliciousFaceModel>
{
	public MaliciousFaceRenderer(EntityRendererFactory.Context context)
	{
		super(context, new MaliciousFaceModel(context.getPart(UltracraftClient.MALICIOUS_LAYER)), 1f);
	}
	
	@Override
	public Identifier getTexture(MaliciousFaceEntity entity)
	{
		return new Identifier(Ultracraft.MOD_ID, entity.isCracked() ? "textures/entity/malicious_face_cracked.png" : "textures/entity/malicious_face.png");
	}
	
	@Override
	public void render(MaliciousFaceEntity mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		RenderSystem.enableBlend();
		super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
	}
	
	@Nullable
	@Override
	protected RenderLayer getRenderLayer(MaliciousFaceEntity entity, boolean showBody, boolean translucent, boolean showOutline)
	{
		return RenderLayer.getEntityTranslucent(getTexture(entity));
	}
}
