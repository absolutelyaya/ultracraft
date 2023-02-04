package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.client.entity.demon.MaliciousFaceRenderer;
import absolutelyaya.ultracraft.client.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.client.entity.projectile.HellBulletRenderer;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;

public class UltracraftClient
{
	public static void init()
	{
		//EntityRenderers
		EntityRendererRegistry.register(EntityRegistry.FILTH, FilthRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MALICIOUS_FACE, MaliciousFaceRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.HELL_BULLET, HellBulletRenderer::new);
		
		SoundRegistry.register();
	}
}
