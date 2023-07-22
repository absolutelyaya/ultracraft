package absolutelyaya.ultracraft.client.rendering.item;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.item.MachineSwordItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MachineSwordRenderer extends GeoItemRenderer<MachineSwordItem>
{
	final MinecraftClient client;
	
	public MachineSwordRenderer()
	{
		super(new DefaultedItemGeoModel<>(new Identifier(Ultracraft.MOD_ID, "machinesword")));
		client = MinecraftClient.getInstance();
	}
	
	@Override
	public Identifier getTextureLocation(MachineSwordItem animatable)
	{
		return switch(animatable.getType(getCurrentItemStack()))
		{
			case NORMAL -> new Identifier(Ultracraft.MOD_ID, "textures/item/machinesword.png");
			case TUNDRA -> new Identifier(Ultracraft.MOD_ID, "textures/item/machinesword_tundra.png");
			case AGONY -> new Identifier(Ultracraft.MOD_ID, "textures/item/machinesword_agony.png");
		};
	}
	
	@Override
	public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay)
	{
		super.render(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
	}
}
