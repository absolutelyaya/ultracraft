package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.client.rendering.entity.demon.MaliciousFaceRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.client.rendering.entity.projectile.HellBulletRenderer;
import absolutelyaya.ultracraft.client.rendering.item.PierceRevolverRenderer;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class UltracraftClient
{
	public static ClientHitscanHandler HITSCAN_HANDLER;
	
	public static void init()
	{
		//EntityRenderers
		EntityRendererRegistry.register(EntityRegistry.FILTH, FilthRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MALICIOUS_FACE, MaliciousFaceRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HELL_BULLET, HellBulletRenderer::new);
		
		SoundRegistry.register();
		
		HITSCAN_HANDLER = new ClientHitscanHandler();
		
		ClientTickEvents.START_CLIENT_TICK.register((client) -> HITSCAN_HANDLER.tick());
		
		GeoItemRenderer.registerItemRenderer(ItemRegistry.PIERCE_REVOLVER.get(), new PierceRevolverRenderer());
	}
}
