package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.client.entity.demon.MaliciousFaceRenderer;
import absolutelyaya.ultracraft.client.entity.husk.FilthRenderer;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;

public class UltracraftClient
{
	public static void init()
	{
		//EntityRenderers
		EntityRendererRegistry.register(EntityRegistry.FILTH, FilthRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.MALICIOUS_FACE, MaliciousFaceRenderer::new);
	}
}
