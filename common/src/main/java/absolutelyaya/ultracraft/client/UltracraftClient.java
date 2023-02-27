package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.UltraHudRenderer;
import absolutelyaya.ultracraft.client.rendering.block.entity.PedestalBlockEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.demon.MaliciousFaceRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.feature.WingsModel;
import absolutelyaya.ultracraft.client.rendering.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.HellBulletRenderer;
import absolutelyaya.ultracraft.client.rendering.item.PierceRevolverRenderer;
import absolutelyaya.ultracraft.particle.DashParticle;
import absolutelyaya.ultracraft.particle.MaliciousChargeParticle;
import absolutelyaya.ultracraft.registry.*;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class UltracraftClient
{
	public static final EntityModelLayer WINGS_LAYER = new EntityModelLayer(new Identifier(Ultracraft.MOD_ID, "wings"), "main");
	public static ClientHitscanHandler HITSCAN_HANDLER;
	static boolean FreezeOption = true;
	static boolean HiVelMode = false;
	
	static UltraHudRenderer hudRenderer;
	
	public static void init()
	{
		ParticleRegistry.init();
		
		//EntityRenderers
		EntityRendererRegistry.register(EntityRegistry.FILTH, FilthRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MALICIOUS_FACE, MaliciousFaceRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HELL_BULLET, HellBulletRenderer::new);
		//Particles
		ParticleProviderRegistry.register(ParticleRegistry.MALICIOUS_CHARGE.get(), MaliciousChargeParticle.MaliciousChargeParticleFactory::new);
		ParticleProviderRegistry.register(ParticleRegistry.DASH.get(), DashParticle.DashParticleFactory::new);
		//Entity model layers
		EntityModelLayerRegistry.registerModelLayer(WINGS_LAYER, WingsModel::getTexturedModelData);
		
		BlockEntityRendererRegistry.register(BlockEntityRegistry.PEDESTAL.get(), PedestalBlockEntityRenderer::new);
		
		ModelPredicateRegistry.registerModels();
		
		HITSCAN_HANDLER = new ClientHitscanHandler();
		
		TickEvent.PLAYER_POST.register((client) -> HITSCAN_HANDLER.tick());
		
		GeoItemRenderer.registerItemRenderer(ItemRegistry.PIERCE_REVOLVER.get(), new PierceRevolverRenderer());
		
		hudRenderer = new UltraHudRenderer();
		WorldRenderEvents.END.register((context) -> hudRenderer.render(context.tickDelta(), context.camera()));
	}
	
	//if no Server override return client setting
	public static boolean isFreezeEnabled()
	{
		if(Ultracraft.FreezeOption.equals(Ultracraft.Option.FREE))
			return FreezeOption;
		else
			return Ultracraft.FreezeOption.equals(Ultracraft.Option.FORCE_ON);
	}
	
	public static boolean isHiVelEnabled()
	{
		if(Ultracraft.HiVelOption.equals(Ultracraft.Option.FREE))
			return HiVelMode;
		else
			return Ultracraft.HiVelOption.equals(Ultracraft.Option.FORCE_ON);
	}
	
	public static void toggleHiVelEnabled()
	{
		if(Ultracraft.HiVelOption.equals(Ultracraft.Option.FREE))
			HiVelMode = !HiVelMode;
		else
			MinecraftClient.getInstance().player.sendMessage(
					Text.translatable("message.ultracraft.hi-vel-forced",
							Ultracraft.HiVelOption.equals(Ultracraft.Option.FORCE_ON) ? "Enabled" : "Disabled"), true);
	}
}
