package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.client.rendering.block.entity.PedestalBlockEntityRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.demon.MaliciousFaceRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.HellBulletRenderer;
import absolutelyaya.ultracraft.client.rendering.item.PierceRevolverRenderer;
import absolutelyaya.ultracraft.particle.MaliciousChargeParticle;
import absolutelyaya.ultracraft.registry.*;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class UltracraftClient
{
	public static ClientHitscanHandler HITSCAN_HANDLER;
	
	public static void init()
	{
		ParticleRegistry.init();
		
		//EntityRenderers
		EntityRendererRegistry.register(EntityRegistry.FILTH, FilthRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MALICIOUS_FACE, MaliciousFaceRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HELL_BULLET, HellBulletRenderer::new);
		//Particles
		ParticleProviderRegistry.register(ParticleRegistry.MALICIOUS_CHARGE.get(), MaliciousChargeParticle.MaliciousChargeParticleFactory::new);
		
		BlockEntityRendererRegistry.register(BlockEntityRegistry.PEDESTAL.get(), PedestalBlockEntityRenderer::new);
		
		ModelPredicateRegistry.registerModels();
		
		HITSCAN_HANDLER = new ClientHitscanHandler();
		
		TickEvent.PLAYER_POST.register((client) -> HITSCAN_HANDLER.tick());
		
		GeoItemRenderer.registerItemRenderer(ItemRegistry.PIERCE_REVOLVER.get(), new PierceRevolverRenderer());
	}
}
